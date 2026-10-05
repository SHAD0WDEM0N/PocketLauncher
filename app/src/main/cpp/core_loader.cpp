#include "core_loader.h"

#include <android/log.h>
#include <cstring>
#include <dlfcn.h>

#define LOG_TAG "PocketCoreLoader"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

CoreLoader* CoreLoader::active_ = nullptr;

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

    handle_ = dlopen(path.c_str(), RTLD_NOW | RTLD_LOCAL);
    if (!handle_) {
        const char* error = dlerror();
        last_error_ = error ? error : "dlopen failed";
        return false;
    }

    if (!bind(retro_api_version_, "retro_api_version") ||
        !bind(retro_get_system_info_, "retro_get_system_info") ||
        !bind(retro_get_system_av_info_, "retro_get_system_av_info") ||
        !bind(retro_set_environment_, "retro_set_environment") ||
        !bind(retro_set_video_refresh_, "retro_set_video_refresh") ||
        !bind(retro_set_audio_sample_, "retro_set_audio_sample") ||
        !bind(retro_set_audio_sample_batch_, "retro_set_audio_sample_batch") ||
        !bind(retro_set_input_poll_, "retro_set_input_poll") ||
        !bind(retro_set_input_state_, "retro_set_input_state") ||
        !bind(retro_init_, "retro_init") ||
        !bind(retro_deinit_, "retro_deinit") ||
        !bind(retro_load_game_, "retro_load_game") ||
        !bind(retro_unload_game_, "retro_unload_game") ||
        !bind(retro_run_, "retro_run")) {
        unload();
        return false;
    }

    if (retro_api_version_() != RETRO_API_VERSION) {
        last_error_ = "Unsupported libretro API version";
        unload();
        return false;
    }

    active_ = this;
    retro_set_environment_(environmentCallback);
    retro_set_video_refresh_(videoCallback);
    retro_set_audio_sample_(audioCallback);
    retro_set_audio_sample_batch_(audioBatchCallback);
    retro_set_input_poll_(inputPollCallback);
    retro_set_input_state_(inputStateCallback);
    retro_init_();
    core_initialised_ = true;

    retro_system_info info{};
    retro_get_system_info_(&info);
    LOGI("Loaded core: %s %s",
         info.library_name ? info.library_name : "Unknown",
         info.library_version ? info.library_version : "");
    return true;
}

void CoreLoader::unload() {
    unloadGame();

    if (core_initialised_ && retro_deinit_) {
        retro_deinit_();
    }
    core_initialised_ = false;

    if (handle_) dlclose(handle_);
    if (active_ == this) active_ = nullptr;

    handle_ = nullptr;
    retro_api_version_ = nullptr;
    retro_get_system_info_ = nullptr;
    retro_get_system_av_info_ = nullptr;
    retro_set_environment_ = nullptr;
    retro_set_video_refresh_ = nullptr;
    retro_set_audio_sample_ = nullptr;
    retro_set_audio_sample_batch_ = nullptr;
    retro_set_input_poll_ = nullptr;
    retro_set_input_state_ = nullptr;
    retro_init_ = nullptr;
    retro_deinit_ = nullptr;
    retro_load_game_ = nullptr;
    retro_unload_game_ = nullptr;
    retro_run_ = nullptr;
}

bool CoreLoader::loadGame(const std::string& romPath) {
    if (!handle_ || !core_initialised_ || !retro_load_game_) {
        last_error_ = "Core is not loaded";
        return false;
    }

    unloadGame();
    retro_game_info info{};
    info.path = romPath.c_str();
    info.data = nullptr;
    info.size = 0;
    info.meta = nullptr;

    if (!retro_load_game_(&info)) {
        last_error_ = "retro_load_game() rejected the ROM";
        return false;
    }

    game_loaded_ = true;
    frame_count_ = 0;
    {
        std::lock_guard<std::mutex> lock(frame_mutex_);
        frame_rgba_.clear();
        frame_width_ = 0;
        frame_height_ = 0;
    }

    retro_system_av_info av{};
    retro_get_system_av_info_(&av);
    LOGI("Game loaded. Base geometry: %ux%u @ %.3f fps",
         av.geometry.base_width, av.geometry.base_height, av.timing.fps);
    return true;
}

void CoreLoader::unloadGame() {
    if (game_loaded_ && retro_unload_game_) retro_unload_game_();
    game_loaded_ = false;
    frame_count_ = 0;
    std::lock_guard<std::mutex> lock(frame_mutex_);
    frame_rgba_.clear();
    frame_width_ = 0;
    frame_height_ = 0;
}

bool CoreLoader::runFrame() {
    if (!game_loaded_ || !retro_run_) return false;
    retro_run_();
    return true;
}

bool CoreLoader::onEnvironment(unsigned cmd, void* data) {
    switch (cmd) {
        case RETRO_ENVIRONMENT_SET_PIXEL_FORMAT: {
            if (!data) return false;
            auto format = *reinterpret_cast<enum retro_pixel_format*>(data);
            if (format != RETRO_PIXEL_FORMAT_XRGB8888 &&
                format != RETRO_PIXEL_FORMAT_RGB565 &&
                format != RETRO_PIXEL_FORMAT_0RGB1555) {
                return false;
            }
            pixel_format_ = format;
            return true;
        }
        case RETRO_ENVIRONMENT_GET_CAN_DUPE:
            if (data) *reinterpret_cast<bool*>(data) = true;
            return true;
        case RETRO_ENVIRONMENT_SET_SUPPORT_NO_GAME:
            return true;
        case RETRO_ENVIRONMENT_GET_SYSTEM_DIRECTORY:
        case RETRO_ENVIRONMENT_GET_SAVE_DIRECTORY:
            if (data) *reinterpret_cast<const char**>(data) = nullptr;
            return true;
        default:
            return false;
    }
}

void CoreLoader::onVideoRefresh(const void* data, unsigned width, unsigned height, size_t pitch) {
    if (!data || width == 0 || height == 0) return;

    std::vector<uint32_t> rgba(static_cast<size_t>(width) * height);

    for (unsigned y = 0; y < height; ++y) {
        const uint8_t* row = static_cast<const uint8_t*>(data) + static_cast<size_t>(y) * pitch;
        for (unsigned x = 0; x < width; ++x) {
            uint8_t r = 0, g = 0, b = 0;

            if (pixel_format_ == RETRO_PIXEL_FORMAT_XRGB8888) {
                uint32_t p;
                std::memcpy(&p, row + x * 4, sizeof(p));
                r = static_cast<uint8_t>((p >> 16) & 0xff);
                g = static_cast<uint8_t>((p >> 8) & 0xff);
                b = static_cast<uint8_t>(p & 0xff);
            } else {
                uint16_t p;
                std::memcpy(&p, row + x * 2, sizeof(p));
                if (pixel_format_ == RETRO_PIXEL_FORMAT_RGB565) {
                    r = static_cast<uint8_t>(((p >> 11) & 0x1f) * 255 / 31);
                    g = static_cast<uint8_t>(((p >> 5) & 0x3f) * 255 / 63);
                    b = static_cast<uint8_t>((p & 0x1f) * 255 / 31);
                } else {
                    r = static_cast<uint8_t>(((p >> 10) & 0x1f) * 255 / 31);
                    g = static_cast<uint8_t>(((p >> 5) & 0x1f) * 255 / 31);
                    b = static_cast<uint8_t>((p & 0x1f) * 255 / 31);
                }
            }

            rgba[static_cast<size_t>(y) * width + x] =
                0xff000000u | (static_cast<uint32_t>(r) << 16) |
                (static_cast<uint32_t>(g) << 8) | b;
        }
    }

    {
        std::lock_guard<std::mutex> lock(frame_mutex_);
        frame_rgba_.swap(rgba);
        frame_width_ = width;
        frame_height_ = height;
        ++frame_count_;
    }
}

unsigned CoreLoader::frameWidth() const {
    std::lock_guard<std::mutex> lock(frame_mutex_);
    return frame_width_;
}

unsigned CoreLoader::frameHeight() const {
    std::lock_guard<std::mutex> lock(frame_mutex_);
    return frame_height_;
}

uint64_t CoreLoader::frameCount() const {
    std::lock_guard<std::mutex> lock(frame_mutex_);
    return frame_count_;
}

std::vector<uint32_t> CoreLoader::copyFrameRgba() const {
    std::lock_guard<std::mutex> lock(frame_mutex_);
    return frame_rgba_;
}

unsigned CoreLoader::apiVersion() const { return retro_api_version_ ? retro_api_version_() : 0; }

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

bool CoreLoader::environmentCallback(unsigned cmd, void* data) {
    return active_ ? active_->onEnvironment(cmd, data) : false;
}

void CoreLoader::videoCallback(const void* data, unsigned width, unsigned height, size_t pitch) {
    if (active_) active_->onVideoRefresh(data, width, height, pitch);
}

void CoreLoader::audioCallback(int16_t, int16_t) {}
size_t CoreLoader::audioBatchCallback(const int16_t*, size_t frames) { return frames; }
void CoreLoader::inputPollCallback() {}
int16_t CoreLoader::inputStateCallback(unsigned, unsigned, unsigned, unsigned) { return 0; }
