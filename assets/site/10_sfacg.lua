-- site config file

function getconfig()
    local tab = {}
    table.insert(tab,'sfacg.com')
    table.insert(tab,'utf-8')  -- page's encoding
    table.insert(tab,'0')  -- 1:默认隐藏 0:默认显示
    table.insert(tab,'0')  -- 1:仿人行为 0:默认行为
    table.insert(tab,'unicode-escape')  -- search's encoding
    return #tab,tab
end

function getsearchparam(keyword)
    local tab = {}
    local t = {["key"]=(string.gsub(keyword,'\\u','%%25u'))}
    table.insert(tab,'http://s.sfacg.com/')
    table.insert(tab,encode(t))
    table.insert(tab,'GET')
    return #tab,tab
end

function getcomics(datafile)
    local tab = {}
    io.input(datafile)
    local s=io.read("*all")
    for url,name,update in string.gmatch(s, 'class="Conjunction".-href="(.-)".->(.-)<.-综合信息：(.-)/.-</ul>') do
        name = (strip(name))
        update = (strip(update))
        url = (strip(url))
        table.insert(tab,name .. '||' .. '' .. '||' .. update .. '||' .. url )
    end
    return #tab,tab
end

function getparts(datafile)
    local tab = {}
    io.input(datafile)
    local s=io.read("*all")
    for block in string.gmatch(s,'class="serialise_list Blue_link2"(.-)</ul>') do
        for url,name in string.gmatch(block, '<li>.-href="(.-)".->(.-)<.-</li>') do
            name = (strip(name))
            url = (strip(url))
            table.insert(tab,name .. '||' .. url) --no need revert
        end
    end
    return #tab,tab
end

function getpics(datafile)
    local tab = {}
    io.input(datafile)
    local s=io.read("*all")
    if not contains(s,'var comicName') then
        local block = string.match(s,'"/Utility/common.js".-src="(.-)">')
        table.insert(tab,block)
    else
        local block = string.match(s,'var%spicAy(.-)$')
        for url in string.gmatch(block, 'picAy%[.-%s?=%s?"(.-)";') do
            url = strip(url)
            table.insert(tab,url)
        end
    end
    return #tab,tab
end
