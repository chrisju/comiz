-- zz's utils

function strip(s) -- the .- is quite efficient
    return string.match(s,"^%s*(.-)%s*$") or ""
end

-- startswith("hello world", "hel") -> true
function startswith(sbig, slittle, start)
    if not start then start = 1 end
    return string.sub(sbig, start, start+string.len(slittle)-1) == slittle
end

-- endswith("hello world", "world") -> true
-- endswith("hello world", {"world","blah"}) -> true
function endswith(sbig, slittle)
    if type(slittle) == "table" then
        for k,v in ipairs(slittle) do
      if string.sub(sbig, string.len(sbig) - string.len(v) + 1) == v then 
        return true
      end
    end
    return false
  end
  return string.sub(sbig, string.len(sbig) - string.len(slittle) + 1) == slittle
end

-- contains("hello world", "llo wor") -> true
-- contains("hello world", {"llo wor","blah"}) -> true
function contains(sbig, slittle)
  if type(slittle) == "table" then
    for k,v in ipairs(slittle) do
      if string.find(sbig, v, 1, true) == v then 
        return true
      end
    end
    return false
  end
  return string.find(sbig, slittle, 1, true)
end

function split(s,pat)
    pat = pat or '%s+'
    local st, g = 1, string.gmatch(s,"()("..pat..")")
    local function getter(segs, seps, sep, cap1, ...)
        st = sep and seps + #sep
        return string.sub(s,segs, (seps or 0) - 1), cap1 or sep, ...
    end
    return function() if st then return getter(st, g()) end end
end

function escapenonascii (str)
    local s = ''
    for i=1,string.len(str) do
        local c = string.byte(str,i)
        if c > 0x7F then
            s = s .. string.format ("%%%02X",c )
        else
            s = s .. string.char(c)
        end
    end
    return s
end

function decodeunicode(s) -- like abc\u5996\u8ac3987
    local i=1
    local r=''
    while i<=string.len(s) do
        if startswith(s,"\\u",i) then
            v = tonumber(string.sub(s,i+2,i+5), 16)
            r = r .. unicode2utf8(v)
            i = i + 6
        else
            r = r .. string.sub(s,i,i)
            i = i + 1
        end
    end
    return r
end

function unicode2utf8(v)
    local result = ''
    local w,x,y,z = 0,0,0,0
    local function modulo(a, b)
        return a - math.floor(a/b) * b
    end
    if v ~= 0 and v ~= nil then
        if v <= 0x7F then -- same as ASCII
            result = result .. string.char(v)
        elseif v >= 0x80 and v <= 0x7FF then -- 2 bytes
            --[[
            y = (v & 0x0007C0) >> 6
            z = v & 0x00003F
            ]]--
            y = math.floor(modulo(v, 0x000800) / 64)
            z = modulo(v, 0x000040)
            result = result .. string.char(0xC0 + y, 0x80 + z)
        elseif (v >= 0x800 and v <= 0xD7FF) or (v >= 0xE000 and v <= 0xFFFF) then -- 3 bytes
            --[[
            x = (v & 0x00F000) >> 12
            y = (v & 0x000FC0) >> 6
            z = v & 0x00003F
            ]]--
            x = math.floor(modulo(v, 0x010000) / 4096)
            y = math.floor(modulo(v, 0x001000) / 64)
            z = modulo(v, 0x000040)
            result = result .. string.char(0xE0 + x, 0x80 + y, 0x80 + z)
        elseif (v >= 0x10000 and v <= 0x10FFFF) then -- 4 bytes
            --[[
            w = (v & 0x1C0000) >> 18
            x = (v & 0x03F000) >> 12
            y = (v & 0x000FC0) >> 6
            z = v & 0x00003F
            ]]--
            w = math.floor(modulo(v, 0x200000) / 262144)
            x = math.floor(modulo(v, 0x040000) / 4096)
            y = math.floor(modulo(v, 0x001000) / 64)
            z = modulo(v, 0x000040)
            result = result .. string.char(0xF0 + w, 0x80 + x, 0x80 + y, 0x80 + z)
        end
    end
    return result
end

function hex2str(s)
    local r = ""
    for i=1,string.len(s),2 do
        local c = string.sub(s,i,i+1)
        r = r .. string.format('%c',tonumber(c,16))
    end
    return r
end

function escape (s)
    s = string.gsub(s, "([,&=+%c])", function (c)
        return string.format("%%%02X", string.byte(c))
    end)
    s = string.gsub(s, " ", "+")
    return s
end

function encode (t)
    local s = ""
    for k,v in pairs(t) do
        s = s .. "&" .. escape(escapenonascii(k)) .. "=" .. escape(escapenonascii(v))
    end
    return string.sub(s, 2)     -- remove first `&'
end
