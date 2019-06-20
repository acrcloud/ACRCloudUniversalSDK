#ifndef __ACRCLOUD_ENGINE_H
#define __ACRCLOUD_ENGINE_H 

#ifdef __cplusplus
extern "C" {
#endif

#ifdef WIN32 //windows platform
 
#ifdef ACR_USER_MODULE_EXPORTS
#define ACR_API __declspec(dllexport)
#else
#define ACR_API __declspec(dllimport)
#endif
 
#ifndef ACR_CALL_TYPE
#define ACR_CALL_TYPE  	__stdcall  
#endif
 
#else //linux platform
 
#ifndef ACR_API
#define ACR_API __attribute__ ((visibility ("default")))
#endif
 
#ifndef ACR_CALL_TYPE
#define ACR_CALL_TYPE
#endif
 
#endif


/**
 *
 *  create audio fingerprint from PCM audio buffer(RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit) 
 *
 *  @param pcm_buffer: query audio buffer[ (little-endian) data, WAVE audio, Microsoft PCM, 16 bit]
 *  @param pcm_buffer_len: the length of pcm_buffer 
 *  @param nchannels:  channels
 *  @param sample_rate: sample rate of pcm_buffer
 *  @param fp_buffer: fingerprint of pcm_buffer, you must free this buffer by acr_free.
 *  @param fp_buffer_len: the length of fp_buffer
 *  @param is_fixed_point: fixed point, default 0[float]
 *  @param resample_quality: 0/1/2, default 1
 *
**/
ACR_API void acr_create_audio_fingerprint_from_pcm(
    char* pcm_buffer, 
    int pcm_buffer_len, 
    int nchannels, 
    int sample_rate, 
    char** fp_buffer, 
    int* fp_buffer_len, 
    int is_fixed_point, 
    int resample_quality);



/**
 *
 *  create humming fingerprint from PCM audio buffer(RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit) 
 *
 *  @param pcm_buffer: query audio buffer[ (little-endian) data, WAVE audio, Microsoft PCM, 16 bit]
 *  @param pcm_buffer_len: the length of pcm_buffer 
 *  @param nchannels:  channels
 *  @param sample_rate: sample rate of pcm_buffer
 *  @param fp_buffer: fingerprint of pcm_buffer, you must free this buffer by acr_free. 
 *  @param fp_buffer_len: the length of fp_buffer
 *  @param is_fixed_point: fixed point, default 0
 *  @param resample_quality: 0/1/2, default 1
 *
**/
ACR_API void acr_create_humming_fingerprint_from_pcm(
    char* pcm_buffer, 
    int pcm_buffer_len, 
    int nchannels, 
    int sample_rate, 
    char** fp_buffer, 
    int* fp_buffer_len, 
    int is_fixed_point, 
    int resample_quality);


/**
 * free buffer that other function return.
**/
ACR_API void acr_free(void* buffer);


#ifdef __cplusplus
}
#endif

#endif
