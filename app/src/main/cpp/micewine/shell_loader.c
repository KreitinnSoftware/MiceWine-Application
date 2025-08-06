#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <string.h>

#define log(prio, ...) __android_log_print(ANDROID_LOG_ ## prio, "ShellLoaderNative", __VA_ARGS__)

static jclass emulationActivityClass = NULL;
static jmethodID appendLogsMethodID = NULL;

void initAppendLogsCache(JNIEnv *env) {
    if (emulationActivityClass == NULL) {
        jclass localClass = (*env)->FindClass(env, "com/micewine/emu/activities/EmulationActivity");
        if (localClass == NULL) {
            return;
        }

        emulationActivityClass = (*env)->NewGlobalRef(env, localClass);
        (*env)->DeleteLocalRef(env, localClass);

        appendLogsMethodID = (*env)->GetStaticMethodID(env, emulationActivityClass, "appendLogs", "(Ljava/lang/String;)V");
        if (appendLogsMethodID == NULL) {
            return;
        }
    }
}

void appendLog(JNIEnv *env, const char *text) {
    if (emulationActivityClass == NULL || appendLogsMethodID == NULL) {
        initAppendLogsCache(env);
        if (emulationActivityClass == NULL || appendLogsMethodID == NULL) {
            return;
        }
    }

    jstring jText = (*env)->NewStringUTF(env, text);
    (*env)->CallStaticVoidMethod(env, emulationActivityClass, appendLogsMethodID, jText);
    (*env)->DeleteLocalRef(env, jText);
}

JNIEXPORT void JNICALL
Java_com_micewine_emu_core_ShellLoader_runCommand(JNIEnv *env, jobject cls, jstring command, jboolean log) {
    const char* parsedCommand;
    int pipe_in[2];
    int pipe_out[2];
    pid_t pid;

    parsedCommand = (*env)->GetStringUTFChars(env, command, NULL);

    if (log == JNI_TRUE) {
        log(DEBUG, "Trying to exec '%s'", parsedCommand);
    }

    if (pipe(pipe_in) == -1 || pipe(pipe_out) == -1) {
        perror("pipe");
        (*env)->ReleaseStringUTFChars(env, command, parsedCommand);
        return;
    }

    pid = fork();
    if (pid == -1) {
        perror("fork");
        (*env)->ReleaseStringUTFChars(env, command, parsedCommand);
        return;
    }

    if (pid == 0) {
        close(pipe_in[1]);
        close(pipe_out[0]);

        dup2(pipe_in[0], STDIN_FILENO);
        close(pipe_in[0]);

        dup2(pipe_out[1], STDOUT_FILENO);
        dup2(pipe_out[1], STDERR_FILENO);
        close(pipe_out[1]);

        execl("/system/bin/sh", "sh", NULL);

        perror("execl");
        exit(EXIT_FAILURE);
    } else {
        close(pipe_in[0]);
        close(pipe_out[1]);

        const char* terminator = "\nexit\n";
        size_t cmd_len = strlen(parsedCommand);
        size_t term_len = strlen(terminator);
        size_t size = cmd_len + term_len;

        char fullCmd[size + 1];

        snprintf(fullCmd, size + 1, "%s%s", parsedCommand, terminator);
        write(pipe_in[1], fullCmd, size);

        if (log == JNI_TRUE) {
            char buffer[1024];
            ssize_t n;

            while ((n = read(pipe_out[0], buffer, sizeof(buffer) - 1)) > 0) {
                buffer[n] = '\0';
                log(DEBUG, "%s", buffer);
                appendLog(env, buffer);
            }
        }

        close(pipe_out[0]);

        waitpid(pid, NULL, 0);
    }

    (*env)->ReleaseStringUTFChars(env, command, parsedCommand);
}

JNIEXPORT jstring JNICALL
Java_com_micewine_emu_core_ShellLoader_runCommandWithOutput(JNIEnv *env, jobject cls, jstring command, jboolean stdErrLog) {
    const char* parsedCommand;
    int pipe_in[2];
    int pipe_out[2];
    pid_t pid;

    parsedCommand = (*env)->GetStringUTFChars(env, command, NULL);

    if (pipe(pipe_in) == -1 || pipe(pipe_out) == -1) {
        perror("pipe");
        (*env)->ReleaseStringUTFChars(env, command, parsedCommand);
        return (*env)->NewStringUTF(env, "");
    }

    pid = fork();
    if (pid == -1) {
        perror("fork");
        (*env)->ReleaseStringUTFChars(env, command, parsedCommand);
        return (*env)->NewStringUTF(env, "");
    }

    if (pid == 0) {
        close(pipe_in[1]);
        close(pipe_out[0]);

        dup2(pipe_in[0], STDIN_FILENO);
        close(pipe_in[0]);

        dup2(pipe_out[1], STDOUT_FILENO);

        if (stdErrLog == JNI_TRUE) {
            dup2(pipe_out[1], STDERR_FILENO);
        }

        close(pipe_out[1]);

        execl("/system/bin/sh", "sh", NULL);

        perror("execl");
        exit(EXIT_FAILURE);
    } else {
        close(pipe_in[0]);
        close(pipe_out[1]);

        const char* terminator = "\nexit\n";
        size_t cmd_len = strlen(parsedCommand);
        size_t term_len = strlen(terminator);
        size_t size = cmd_len + term_len;

        char fullCmd[size + 1];

        snprintf(fullCmd, size + 1, "%s%s", parsedCommand, terminator);
        write(pipe_in[1], fullCmd, size);

        char r_buffer[10240] = {0};
        char buffer[1024];
        ssize_t n;

        while ((n = read(pipe_out[0], buffer, sizeof(buffer) - 1)) > 0) {
            buffer[n] = '\0';
            if (strlen(r_buffer) + n < sizeof(r_buffer)) {
                strcat(r_buffer, buffer);
            }
        }

        close(pipe_out[0]);
        waitpid(pid, NULL, 0);
        (*env)->ReleaseStringUTFChars(env, command, parsedCommand);

        return (*env)->NewStringUTF(env, r_buffer);
    }
}
