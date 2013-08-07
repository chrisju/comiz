#include <string.h>
#include <stdarg.h>
#include <stdlib.h>
#include "lua.h"

#define LOGE printf

#include "lauxlib.h"
#include "lualib.h"
char LUA_ERROR_STR[256];
char* luaerror (lua_State *L, const char *fmt, ...)
{
    va_list argp;
    va_start(argp, fmt);
    vsprintf(LUA_ERROR_STR, fmt, argp);
    va_end(argp);
    lua_close(L);
    return LUA_ERROR_STR;
}
// TODO lua_atpanic


void load (const char *luafile, int *width, int *height)
{
    lua_State *L = lua_open();
    luaL_openlibs(L);

    if (luaL_loadfile(L, luafile) || lua_pcall(L, 0, 0, 0))
        LOGE( "cannot run configuration file: %s",
              lua_tostring(L, -1));

    lua_getglobal(L, "width");
    lua_getglobal(L, "height");
    if (!lua_isnumber(L, -2))
        LOGE("`width' should be a number\n");
    if (!lua_isnumber(L, -1))
        LOGE("`height' should be a number\n");
    *width = (int)lua_tonumber(L, -2);
    *height = (int)lua_tonumber(L, -1);

    lua_close(L);
}

void file2str(const char *luafile, const char *datafile, char *outstr)
{
    //初始化lua
    lua_State *L = lua_open();
    luaL_openlibs(L);

    //传入全局变量
    lua_pushstring(L, datafile);
    lua_setglobal(L, "datafile");

    //运行lua文件
    if (luaL_loadfile(L, luafile) || lua_pcall(L, 0, 0, 0))
        LOGE( "cannot run configuration file: %s",
              lua_tostring(L, -1));

    //获取结果
    lua_getglobal(L, "s");
    if (!lua_isstring(L, -1))
        LOGE("s should be a string\n");
    strcpy(outstr, lua_tostring(L, -1));

    lua_close(L);
}

void file2tab(const char *luafile, const char *datafile, char ***pouttab, int *plen)
{
    lua_State *L = lua_open();
    luaL_openlibs(L);

    lua_pushstring(L, datafile);
    lua_setglobal(L, "datafile");

    if (luaL_loadfile(L, luafile) || lua_pcall(L, 0, 0, 0))
        LOGE( "cannot run configuration file: %s",
              lua_tostring(L, -1));

    int len,i;
    lua_getglobal(L, "tablen");
    lua_getglobal(L, "tab");

    if (!lua_isnumber(L, -2))
        LOGE("`tablen' should be a number\n");
    len = (int)lua_tonumber(L, -2);

    if (!lua_istable(L, -1))
        LOGE("tab is not a valid table");
    //len = luaL_getn(L, -1);
    char **outtab = (char**)malloc(4*(len));
    for(i=0;i<len;i++){
        outtab[i]=(char*)malloc(512);

        lua_rawgeti(L, -1, i+1);

        if (!lua_isstring(L, -1))
            LOGE("s should be a string\n");
        strcpy(outtab[i], lua_tostring(L, -1));

        lua_pop(L, 1);
    }
    *pouttab = outtab;
    *plen = len;

    lua_close(L);
}

void lua2tab(const char *luastring, const char *datafile, char ***pouttab, int *plen)
{
    lua_State *L = lua_open();
    luaL_openlibs(L);

    lua_pushstring(L, datafile);
    lua_setglobal(L, "datafile");

    if (luaL_loadstring(L, luastring) || lua_pcall(L, 0, 0, 0))
        LOGE( "cannot run lua file: %s",
              lua_tostring(L, -1));

    int len,i;
    lua_getglobal(L, "tablen");
    lua_getglobal(L, "tab");

    if (!lua_isnumber(L, -2))
        LOGE("`tablen' should be a number\n");
    len = (int)lua_tonumber(L, -2);

    if (!lua_istable(L, -1))
        LOGE("tab is not a valid table");
    //len = luaL_getn(L, -1);
    char **outtab = (char**)malloc(4*len);
    for(i=0;i<len;i++){
        outtab[i]=(char*)malloc(512);

        lua_rawgeti(L, -1, i+1);

        if (!lua_isstring(L, -1))
            LOGE("s should be a string\n");
        strcpy(outtab[i], lua_tostring(L, -1));

        lua_pop(L, 1);
    }
    *pouttab = outtab;
    *plen = len;

    lua_close(L);
}

void luafunc2tab(const char *luastring, const char *func, char ***pouttab, int *plen)
{
    lua_State *L = lua_open();
    luaL_openlibs(L);

    if (luaL_loadstring(L, luastring) || lua_pcall(L, 0, 0, 0))
        LOGE( "cannot run lua file: %s",
              lua_tostring(L, -1));

    /* push functions and arguments */
    lua_getglobal(L, func); /* function to be called */

    /* do the call */
    if (lua_pcall(L, 0, 2, 0) != 0)
        LOGE("error running function : %s",
              lua_tostring(L, -1));

    int len,i;
    /* retrieve result */
    if (!lua_isnumber(L, -2))
        LOGE("function must return a number");
    len = lua_tonumber(L, -2);

    if (!lua_istable(L, -1))
        LOGE("tab is not a valid table");
    //len = luaL_getn(L, -1);
    char **outtab = (char**)malloc(4*len);
    for(i=0;i<len;i++){
        outtab[i]=(char*)malloc(512);

        lua_rawgeti(L, -1, i+1);

        if (!lua_isstring(L, -1))
            LOGE("s should be a string\n");
        strcpy(outtab[i], lua_tostring(L, -1));

        lua_pop(L, 1);
    }

    lua_pop(L, 1); /* pop returned value */

    *pouttab = outtab;
    *plen = len;

    lua_close(L);
}

void luafunc2tab1(const char *luastring, const char *func, const char *strparam1, char ***pouttab, int *plen)
{
    lua_State *L = lua_open();
    luaL_openlibs(L);

    if (luaL_loadstring(L, luastring) || lua_pcall(L, 0, 0, 0))
        LOGE( "cannot run lua file: %s",
              lua_tostring(L, -1));

    /* push functions and arguments */
    lua_getglobal(L, func); /* function to be called */
    lua_pushstring(L, strparam1); /* push 1st argument */

    /* do the call */
    if (lua_pcall(L, 1, 2, 0) != 0)
        LOGE("error running function : %s",
              lua_tostring(L, -1));

    int len,i;
    /* retrieve result */
    if (!lua_isnumber(L, -2))
        LOGE("function must return a number");
    len = lua_tonumber(L, -2);

    if (!lua_istable(L, -1))
        LOGE("tab is not a valid table");
    //len = luaL_getn(L, -1);
    char **outtab = (char**)malloc(4*len);
    for(i=0;i<len;i++){
        outtab[i]=(char*)malloc(512);

        lua_rawgeti(L, -1, i+1);

        if (!lua_isstring(L, -1))
            LOGE("s should be a string\n");
        strcpy(outtab[i], lua_tostring(L, -1));

        lua_pop(L, 1);
    }

    lua_pop(L, 1); /* pop returned value */

    *pouttab = outtab;
    *plen = len;

    lua_close(L);
}

char * mylua_getglobalstring(lua_State *L, const char* name, char* outstr)
{
    lua_getglobal(L, name);
    if (!lua_isstring(L, -1))
        LOGE("should be a string\n");
    strcpy(outstr, lua_tostring(L, -1));
    lua_pop(L, 1);
    return outstr;
}

