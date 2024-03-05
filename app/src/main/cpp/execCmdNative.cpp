#include <jni.h>
#include <cstdio>
#include <iostream>
#include <memory>
#include <stdexcept>
#include "includes/calc.h"
#include <array>
#include <string>


//expected comand to execute
extern "C" {
    JNIEXPORT jstring JNICALL
    Java_com_micewine_emu_nativeLoader_NativeLoader_shellExecCmd(JNIEnv *env, jobject obj, jstring command) {
        const char* comando = env->GetStringUTFChars(command, nullptr);
        std::array<char, 12800> buffer;
        std::string result;

        std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(comando, "r"), pclose);

        env->ReleaseStringUTFChars(command, comando);

        if (!pipe) {
            return env->NewStringUTF("Erro ao abrir o pipe.");
        }

        while (fgets(buffer.data(), buffer.size(), pipe.get()) != nullptr) {
            result += buffer.data();
        }

        return env->NewStringUTF(result.c_str());
    }
}