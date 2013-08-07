#include <string.h>

#include <android/log.h>
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "zz", __VA_ARGS__)

char* adjust_pwd(char * old)
{
    int i=0;
    char* r=old;
    while(*old)
    {
        *old=*old++ + ++i;
    }
    return r;
}

char* xor(char* old, int len, char* pwd)
{
    int i;
    int psize = strlen(pwd);
    for(i=0;i<len;i++)
    {
        old[i] = old[i] ^ pwd[i%psize];
    }
    return old;
}

char* xor_a(char* old, int len, char* pwd_o)
{
    char * pwd = adjust_pwd(pwd_o);
    return xor(old, len, pwd);
}
