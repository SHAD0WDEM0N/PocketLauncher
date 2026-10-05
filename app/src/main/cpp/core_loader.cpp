#include "core_loader.h"

#include <dlfcn.h>
#include <android/log.h>

#define LOG_TAG "PocketCoreLoader"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

CoreLoader::~CoreLoader() {
    unload();
}

template <typename T>
bool CoreLoader::bind(T& target, const char* symbol) {
    dlerror();
    target = reinterpret_cast<T>(dlsym(handle_, symbol));
    const char* error = dlerror();

    if (error != nullptr || target == nullptr) {
        last_error_ = std::string("Missing libretro symbol: ") + symbol;
        LOGE("%s", last_error_.c_str());
        return false;
    }

    return true;
}

bool CoreLoader::load(const std::string& path) {
    unload();
    last_error_.clear();

    LOGI("Loading libretro core: %s", path.c_str());

    handle_ = dlopen(path.c_str(), RTLD_NOW | RTLD_LOCAL);
    if (!handle_) {
        const char* error = dlerror();
        last_error_ = error ? error : "dlopen failed";
        LOGE("Core load failed: %s", last_error_.c_str());
        return false;
    }

    if (!bind(retro_api_version_, "retro_api_version") ||
        !bind(retro_get_system_info_, "retro_get_system_info") ||
        !bind(retro_init_, "retro_init") ||
        !bind(retro_deinit_, "retro_deinit")) {
        unload();
        return false;
    }

    const unsigned version = retro_api_version_();
    if (version != RETRO_API_VERSION) {
        last_error_ = "Unsupported libretro API version " + std::to_string(version);
        LOGE("%s", last_error_.c_str());
        unload();
        return false;
    }

    retro_system_info info{};
    retro_get_system_info_(&info);
    LOGI("Loaded core: %s %s",
         info.library_name ? info.library_name : "Unknown",
         info.library_version ? info.library_version : "");

    return true;
}

void CoreLoader::unload() {
    if (handle_) {
        dlclose(handle_);
    }

    handle_ = nullptr;
    retro_api_version_ = nullptr;
    retro_get_system_info_ = nullptr;
    retro_init_ = nullptr;
    retro_deinit_ = nullptr;
}

unsigned CoreLoader::apiVersion() const {
    return retro_api_version_ ? retro_api_version_() : 0;
}

std::string CoreLoader::libraryName() const {
    if (!retro_get_system_info_) return "";

    retro_system_info info{};
    retro_get_system_info_(&info);
    return info.library_name ? info.library_name : "";
}

std::string CoreLoader::libraryVersion() const {
    if (!retro_get_system_info_) return "";

    retro_system_info info{};
    retro_get_system_info_(&info);
    return info.library_version ? info.library_version : "";
}

std::string CoreLoader::validExtensions() const {
    if (!retro_get_system_info_) return "";

    retro_system_info info{};
    retro_get_system_info_(&info);
    return info.valid_extensions ? info.valid_extensions : "";
}
