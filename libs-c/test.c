#include <stdio.h>
#include <math.h>
#include <stdlib.h>

#include "resample.h"

int main(int argc, const char *argv[])
{
    FILE* fp_wav = fopen(argv[1] ,"rb");
    if (fp_wav == NULL)
        return 2;

    fseek(fp_wav,0L,SEEK_END);
    int size = ftell(fp_wav);
    fseek(fp_wav,44,SEEK_SET);//skip 44 for wav

    char* pcm_buffer = (char*)malloc(size);
    int pcm_len = fread(pcm_, sizeof(char), size, fp_wav);
    fclose(fp_wav);

    char *fp_buffer = NULL;
    int fp_buffer_len = 0;
    acr_create_audio_fingerprint_from_pcm(pcm_buffer, pcm_len, 1, 8000, &fp_buffer, &fp_buffer_len, 0, 1);

    printf("pcm_len=%d, fp_buffer_len=%d\n", pcm_len, fp_buffer_len);

    acr_free(fp_buffer);
}
