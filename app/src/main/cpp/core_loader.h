#ifndef POCKET_CORE_LOADER_H
#define POCKET_CORE_LOADER_H

#include <atomic>
#include <cstdint>
#include <mutex>
#include <string>
#include <vector>
#include "libretro.h"

class CoreLoader {
public:
    CoreLoader() = default;
    ~CoreLoader();

    bool load(const std::string& path);
    void unload();

    bool loadGame(const std::string& romPath);
    void unloadGame();
    bool runFrame();
    bool loadSaveRam(const std::string& path);
    bool saveSaveRam(const std::string& path);

    bool isLoaded() const { return handle_ != nullptr; }
    bool isGameLoaded() const { return game_loaded_; }
    unsigned apiVersion() const;
    std::string libraryName() const;
    std::string libraryVersion() const;
    std::string validExtensions() const;
    std::string lastError() const { return last_error_; }

    unsigned frameWidth() const;
    unsigned frameHeight() const;
    uint64_t frameCount() const;
    std::vector<uint32_t> copyFrameRgba() const;

    void setInputMask(uint32_t mask);
    double audioSampleRate() const;
    double videoFps() const;
    std::vector<int16_t> drainAudio();

private:
    void* handle_ = nullptr;
    std::string last_error_;
    bool core_initialised_ = false;
    bool game_loaded_ = false;
    enum retro_pixel_format pixel_format_ = RETRO_PIXEL_FORMAT_0RGB1555;

    mutable std::mutex frame_mutex_;
    std::vector<uint32_t> frame_rgba_;
    unsigned frame_width_ = 0;
    unsigned frame_height_ = 0;
    uint64_t frame_count_ = 0;

    std::atomic<uint32_t> input_mask_{0};
    mutable std::mutex audio_mutex_;
    std::vector<int16_t> audio_pcm_;
    double audio_sample_rate_ = 0.0;
    double video_fps_ = 60.0;

    using retro_api_version_t = unsigned (*)();
    using retro_get_system_info_t = void (*)(struct retro_system_info*);
    using retro_get_system_av_info_t = void (*)(struct retro_system_av_info*);
    using retro_set_environment_t = void (*)(retro_environment_t);
    using retro_set_video_refresh_t = void (*)(retro_video_refresh_t);
    using retro_set_audio_sample_t = void (*)(retro_audio_sample_t);
    using retro_set_audio_sample_batch_t = void (*)(retro_audio_sample_batch_t);
    using retro_set_input_poll_t = void (*)(retro_input_poll_t);
    using retro_set_input_state_t = void (*)(retro_input_state_t);
    using retro_init_t = void (*)();
    using retro_deinit_t = void (*)();
    using retro_load_game_t = bool (*)(const struct retro_game_info*);
    using retro_unload_game_t = void (*)();
    using retro_run_t = void (*)();
    using retro_get_memory_data_t = void* (*)(unsigned);
    using retro_get_memory_size_t = size_t (*)(unsigned);

    retro_api_version_t retro_api_version_ = nullptr;
    retro_get_system_info_t retro_get_system_info_ = nullptr;
    retro_get_system_av_info_t retro_get_system_av_info_ = nullptr;
    retro_set_environment_t retro_set_environment_ = nullptr;
    retro_set_video_refresh_t retro_set_video_refresh_ = nullptr;
    retro_set_audio_sample_t retro_set_audio_sample_ = nullptr;
    retro_set_audio_sample_batch_t retro_set_audio_sample_batch_ = nullptr;
    retro_set_input_poll_t retro_set_input_poll_ = nullptr;
    retro_set_input_state_t retro_set_input_state_ = nullptr;
    retro_init_t retro_init_ = nullptr;
    retro_deinit_t retro_deinit_ = nullptr;
    retro_load_game_t retro_load_game_ = nullptr;
    retro_unload_game_t retro_unload_game_ = nullptr;
    retro_run_t retro_run_ = nullptr;
    retro_get_memory_data_t retro_get_memory_data_ = nullptr;
    retro_get_memory_size_t retro_get_memory_size_ = nullptr;

    template <typename T>
    bool bind(T& target, const char* symbol);

    void onVideoRefresh(const void* data, unsigned width, unsigned height, size_t pitch);
    void onAudioSample(int16_t left, int16_t right);
    size_t onAudioBatch(const int16_t* data, size_t frames);
    int16_t onInputState(unsigned port, unsigned device, unsigned index, unsigned id);
    bool onEnvironment(unsigned cmd, void* data);

    static CoreLoader* active_;
    static bool environmentCallback(unsigned cmd, void* data);
    static void videoCallback(const void* data, unsigned width, unsigned height, size_t pitch);
    static void audioCallback(int16_t left, int16_t right);
    static size_t audioBatchCallback(const int16_t* data, size_t frames);
    static void inputPollCallback();
    static int16_t inputStateCallback(unsigned port, unsigned device, unsigned index, unsigned id);
};

#endif
