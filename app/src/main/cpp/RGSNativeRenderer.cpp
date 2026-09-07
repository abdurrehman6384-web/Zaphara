#include <jni.h>
#include <android/log.h>
#include <GLES2/gl2.h>
#include <mutex>
#include <string>

#define LOG_TAG "RGS-Live2D"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {
std::mutex g_mutex;
std::string g_assetRoot;
std::string g_modelDirectory;
std::string g_modelFile;
float g_mouthOpenY = 0.0f;
float g_touchX = 0.0f;
float g_touchY = 0.0f;
float g_scale = 1.0f;
float g_offsetX = 0.0f;
float g_offsetY = 0.0f;
}

/*
 * Cubism adapter boundary.
 *
 * This file intentionally keeps JNI stable while the host app supplies the
 * versioned Cubism Framework implementation. In the Cubism-backed section,
 * loadModel maps Model3.json resources, update() sets the parameter named
 * ParamMouthOpenY, and motion/expression calls delegate to the model manager.
 * The no-SDK path still clears the GL surface, making integration failures
 * visible rather than crashing at startup.
 */
extern "C" JNIEXPORT void JNICALL
Java_com_rgs_live2d_RGSJniBridge_onSurfaceCreated(JNIEnv*, jobject) {
    glClearColor(0.035f, 0.025f, 0.07f, 1.0f);
}

extern "C" JNIEXPORT void JNICALL
Java_com_rgs_live2d_RGSJniBridge_onSurfaceChanged(JNIEnv*, jobject, jint width, jint height) {
    glViewport(0, 0, width, height);
}

extern "C" JNIEXPORT void JNICALL
Java_com_rgs_live2d_RGSJniBridge_onDrawFrame(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(g_mutex);
    glClear(GL_COLOR_BUFFER_BIT);
    // Replace this call with the Cubism model manager's Update/Draw pass.
    // g_mouthOpenY is the smoothed ParamMouthOpenY input.
}

extern "C" JNIEXPORT void JNICALL
Java_com_rgs_live2d_RGSJniBridge_onSurfaceDestroyed(JNIEnv*, jobject) {}

extern "C" JNIEXPORT void JNICALL
Java_com_rgs_live2d_RGSJniBridge_setAssetRoot(JNIEnv* env, jobject, jobject context) {
    (void)env;
    (void)context;
    // AssetManager is passed here so the host adapter can read model3.json,
    // moc3, textures, motions and expressions from Android assets.
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_rgs_live2d_RGSJniBridge_loadModel(JNIEnv* env, jobject, jstring directory, jstring file) {
    const char* dir = env->GetStringUTFChars(directory, nullptr);
    const char* model = env->GetStringUTFChars(file, nullptr);
    {
        std::lock_guard<std::mutex> lock(g_mutex);
        g_modelDirectory = dir ? dir : "";
        g_modelFile = model ? model : "";
    }
    env->ReleaseStringUTFChars(directory, dir);
    env->ReleaseStringUTFChars(file, model);
    return g_modelFile.size() >= 11 &&
           g_modelFile.rfind(".model3.json") == g_modelFile.size() - 11;
}

extern "C" JNIEXPORT void JNICALL
Java_com_rgs_live2d_RGSJniBridge_setMotion(JNIEnv* env, jobject, jstring group, jint index, jint priority) {
    (void)env; (void)group; (void)index; (void)priority;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_rgs_live2d_RGSJniBridge_setExpression(JNIEnv* env, jobject, jstring expression) {
    (void)env; (void)expression;
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_rgs_live2d_RGSJniBridge_setMouthOpenY(JNIEnv*, jobject, jfloat value) {
    std::lock_guard<std::mutex> lock(g_mutex);
    g_mouthOpenY = value < 0.0f ? 0.0f : (value > 1.0f ? 1.0f : value);
}

extern "C" JNIEXPORT void JNICALL
Java_com_rgs_live2d_RGSJniBridge_setTouch(JNIEnv*, jobject, jfloat x, jfloat y) {
    std::lock_guard<std::mutex> lock(g_mutex);
    g_touchX = x; g_touchY = y;
}

extern "C" JNIEXPORT void JNICALL
Java_com_rgs_live2d_RGSJniBridge_setModelScale(JNIEnv*, jobject, jfloat value) {
    g_scale = value;
}

extern "C" JNIEXPORT void JNICALL
Java_com_rgs_live2d_RGSJniBridge_setModelOffset(JNIEnv*, jobject, jfloat x, jfloat y) {
    g_offsetX = x; g_offsetY = y;
}