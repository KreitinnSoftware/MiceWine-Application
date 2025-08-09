#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <stdbool.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <string.h>
#include <math.h>

#define log(prio, ...) __android_log_print(ANDROID_LOG_ ## prio, "ControllerNative", __VA_ARGS__)

typedef struct {
    bool isConnected;
    uint8_t buttons;
    uint8_t buttonsB;
    uint8_t dpadStatus;
    uint8_t axisLX;
    uint8_t axisLY;
    uint8_t axisRX;
    uint8_t axisRY;
    uint8_t axisLT;
    uint8_t axisRT;
} ControllerState;

static ControllerState connectedVirtualControllers[4] = {
        { false, 0, 0, 0, 127, 127, 127, 127, 0, 0 },
        { false, 0, 0, 0, 127, 127, 127, 127, 0, 0 },
        { false, 0, 0, 0, 127, 127, 127, 127, 0, 0 },
        { false, 0, 0, 0, 127, 127, 127, 127, 0, 0 }
};

#define CLIENT_PORT 7941
#define BUFFER_SIZE 44

#define REQUEST_GET_CONNECTION 1
#define REQUEST_GET_CONTROLLER_STATE 2
#define REQUEST_GET_CONTROLLER_STATE_DINPUT 3

static volatile int inputServerRunning = 0;

static bool enableXInput = true;
static bool enableDInput = true;

static pthread_t controller_thread_id;
static int sockfd;

static void thread_signal_handler(__unused int signum) {
    pthread_exit(NULL);
    if (sockfd >= 0) {
        close(sockfd);
        sockfd = -1;
    }
}

void *controller_update_thread(__unused void *param) {
    struct sigaction sa;
    sa.sa_handler = thread_signal_handler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    sigaction(SIGUSR2, &sa, NULL);

    struct sockaddr_in serverAddr, clientAddr;
    socklen_t addrLen = sizeof(clientAddr);
    uint8_t buffer[BUFFER_SIZE];

    sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd < 0) {
        log(ERROR, "Error on creating server.");
        pthread_exit(NULL);
    }

    int optval = 1;
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(optval)) < 0) {
        log(ERROR, "Failed to set SO_REUSEADDR");
        close(sockfd);
        pthread_exit(NULL);
    }

    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(CLIENT_PORT);

    if (bind(sockfd, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
        log(ERROR, "Error on binding.");
        close(sockfd);
        pthread_exit(NULL);
    }

    while (inputServerRunning) {
        memset(buffer, 0, BUFFER_SIZE);
        addrLen = sizeof(clientAddr);
        ssize_t len = recvfrom(sockfd, buffer, BUFFER_SIZE, 0, (struct sockaddr*)&clientAddr, &addrLen);

        if (len < 0) {
            continue;
        }

        if (buffer[0] == REQUEST_GET_CONNECTION) {
            sendto(sockfd, buffer, BUFFER_SIZE, 0, (struct sockaddr*)&clientAddr, addrLen);
        } else if (buffer[0] == REQUEST_GET_CONTROLLER_STATE) {
            for (int i = 0; i < 4; i++) {
                buffer[0 + (i * 11)] = REQUEST_GET_CONTROLLER_STATE;
                buffer[1 + (i * 11)] = (connectedVirtualControllers[i].isConnected || i == 0) && enableXInput;
                buffer[2 + (i * 11)] = connectedVirtualControllers[i].buttons;
                buffer[3 + (i * 11)] = connectedVirtualControllers[i].buttonsB;
                buffer[4 + (i * 11)] = connectedVirtualControllers[i].dpadStatus;
                buffer[5 + (i * 11)] = connectedVirtualControllers[i].axisLX;
                buffer[6 + (i * 11)] = connectedVirtualControllers[i].axisLY;
                buffer[7 + (i * 11)] = connectedVirtualControllers[i].axisRX;
                buffer[8 + (i * 11)] = connectedVirtualControllers[i].axisRY;
                buffer[9 + (i * 11)] = connectedVirtualControllers[i].axisLT;
                buffer[10 + (i * 11)] = connectedVirtualControllers[i].axisRT;
            }

            sendto(sockfd, buffer, BUFFER_SIZE, 0, (struct sockaddr*)&clientAddr, addrLen);
        } else if (buffer[0] == REQUEST_GET_CONTROLLER_STATE_DINPUT) {
            for (int i = 0; i < 4; i++) {
                buffer[0 + (i * 11)] = REQUEST_GET_CONTROLLER_STATE_DINPUT;
                buffer[1 + (i * 11)] = (connectedVirtualControllers[i].isConnected || i == 0) && enableDInput;
                buffer[2 + (i * 11)] = connectedVirtualControllers[i].buttons;
                buffer[3 + (i * 11)] = connectedVirtualControllers[i].buttonsB;
                buffer[4 + (i * 11)] = connectedVirtualControllers[i].dpadStatus;
                buffer[5 + (i * 11)] = connectedVirtualControllers[i].axisLX;
                buffer[6 + (i * 11)] = connectedVirtualControllers[i].axisLY;
                buffer[7 + (i * 11)] = connectedVirtualControllers[i].axisRX;
                buffer[8 + (i * 11)] = connectedVirtualControllers[i].axisRY;
                buffer[9 + (i * 11)] = connectedVirtualControllers[i].axisLT;
                buffer[10 + (i * 11)] = connectedVirtualControllers[i].axisRT;
            }

            sendto(sockfd, buffer, BUFFER_SIZE, 0, (struct sockaddr*)&clientAddr, addrLen);
        }

        usleep(2000);
    }

    pthread_exit(NULL);
}

JNIEXPORT void JNICALL
Java_com_micewine_emu_controller_ControllerUtils_startInputServer(__unused JNIEnv *env, __unused jobject cls) {
    if (inputServerRunning) return;
    inputServerRunning = true;

    pthread_create(&controller_thread_id, NULL, controller_update_thread, NULL);
}

JNIEXPORT void JNICALL
Java_com_micewine_emu_controller_ControllerUtils_stopInputServer(__unused JNIEnv *env, __unused jobject cls) {
    inputServerRunning = false;
    pthread_kill(controller_thread_id, SIGUSR2);
    pthread_join(controller_thread_id, NULL);
}

JNIEXPORT jint JNICALL
Java_com_micewine_emu_controller_ControllerUtils_connectController(__unused JNIEnv *env, __unused jobject cls) {
    for (int i = 0; i < 4; i++) {
        if (!connectedVirtualControllers[i].isConnected) {
            log(DEBUG, "Connected Controller on Port %i", i);
            connectedVirtualControllers[i].isConnected = true;
            return i;
        }
    }

    return -1;
}

JNIEXPORT void JNICALL
Java_com_micewine_emu_controller_ControllerUtils_disconnectController(__unused JNIEnv *env, __unused jobject cls, jint index) {
    if (index == -1 || index > 3) return;
    log(DEBUG, "Disconnected Controller on Port %i", index);
    connectedVirtualControllers[index].isConnected = false;
}

JNIEXPORT void JNICALL
Java_com_micewine_emu_controller_ControllerUtils_updateButtonsStateNative(__unused JNIEnv *env, __unused jobject cls, jint index, jint buttons, jint buttonsB) {
    if (index == -1 || index > 3) return;
    connectedVirtualControllers[index].buttons = buttons;
    connectedVirtualControllers[index].buttonsB = buttonsB;
}

static inline uint8_t float_to_u8_255(float f) {
    if (f < -1.0F) {
        f = -1.0F;
    } else if (f > 1.0F) {
        f = 1.0F;
    }

    float scaled = (f + 1.0F) * 127.5F;
    int rounded = (int) roundf(scaled);

    if (rounded < 0) {
        rounded = 0;
    } else if (rounded > 255) {
        rounded = 255;
    }

    return (uint8_t) rounded;
}

JNIEXPORT void JNICALL
Java_com_micewine_emu_controller_ControllerUtils_updateAxisStateNative(__unused JNIEnv *env, jobject __unused cls, jint index, jfloat lx, jfloat ly, jfloat rx, jfloat ry, jfloat lt, jfloat rt, jbyte dpadStatus) {
    if (index == -1 || index > 3) return;
    connectedVirtualControllers[index].axisLX = float_to_u8_255(lx);
    connectedVirtualControllers[index].axisLY = float_to_u8_255(-ly);
    connectedVirtualControllers[index].axisRX = float_to_u8_255(rx);
    connectedVirtualControllers[index].axisRY = float_to_u8_255(-ry);
    connectedVirtualControllers[index].axisLT = float_to_u8_255(lt * 2.F - 1.F);
    connectedVirtualControllers[index].axisRT = float_to_u8_255(rt * 2.F - 1.F);
    connectedVirtualControllers[index].dpadStatus = dpadStatus;
}