#ifndef POCKET_CORE_LOADER_H
#define POCKET_CORE_LOADER_H

#include <string>
#include "libretro.h"

class CoreLoader {
public:
    CoreLoader() = default;
    ~CoreLoader();

    bool load(const std::string& path);
    void unload();

    bool isLoaded() const { return handle_ != nullptr; }
    unsigned apiVersion() const;
    std::string libraryName() const;
    std::string libraryVersion() const;
    std::string validExtensions() const;
    std::string lastError() const { return last_error_; }

private:
    void* handle_ = nullptr;
    std::string last_error_;

    using retro_api_version_t = unsigned (*)();
    using retro_get_system_info_t = void (*)(struct retro_system_info*);
    using retro_init_t = void (*)();
    using retro_deinit_t = void (*)();

    retro_api_version_t retro_api_version_ = nullptr;
    retro_get_system_info_t retro_get_system_info_ = nullptr;
    retro_init_t retro_init_ = nullptr;
    retro_deinit_t retro_deinit_ = nullptr;

    template <typename T>
    bool bind(T& target, const char* symbol);
};

#endif
