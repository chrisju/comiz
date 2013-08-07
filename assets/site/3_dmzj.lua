-- site config file
--
-- can't getpics, js functionly encrypted

function getconfig()
    local tab = {}
    table.insert(tab,'dmzj.com')
    table.insert(tab,'utf-8')  -- page's encoding
    table.insert(tab,'1')  -- 1:默认隐藏 0:默认显示
    table.insert(tab,'0')  -- 1:仿人行为 0:默认行为
    table.insert(tab,'utf-8')  -- search's encoding
    return #tab,tab
end

function getsearchparam(keyword)
    local tab = {}
    local t = {["s"]=keyword}
    table.insert(tab,'http://www.dmzj.com/tags/search.shtml')
    table.insert(tab,encode(t))
    table.insert(tab,'GET')
    return #tab,tab
end


function getcomics(datafile)
    local tab = {}
    local host = 'http://www.dmzj.com/tags/'
    io.input(datafile)
    local s=io.read("*all")
    local block = string.match(s,'class="tcaricature_block tcaricature_block2".->(.-)</div><!--tcaricature_new2-->')
 --   for update,name,author,url in string.gmatch(block, '{.-"last_update_chapter_name".-"(.-)".-"comic_name":"(.-)".-"comic_author":"(.-)".-"comic_url":"\.\.(.-)".-}') do
    for url,name,author,update in string.gmatch(block, '<ul>.-href="(.-)".-tilte="(.-)".-作者:(.-)</div>.-tartget="_blank">(.-)</a>.-</ul>') do
        name = decodeunicode(strip(name))
        author = decodeunicode(strip(author))
        url = decodeunicode(strip(url))
        update = decodeunicode(strip(update))
        table.insert(tab,name .. '||' .. author .. '||' .. update .. '||' .. host .. strip(url))
    end
    return #tab,tab
end

function getparts(datafile)
    local tab = {}
    local host = "http://www.dmzj.com/"
    io.input(datafile)
    local s=io.read("*all")
    for block in string.gmatch(s,'<div.-"cartoon_online_border".->(.-)<%s-/div%s->') do
        for name,url in string.gmatch(block, '<li>.-"(.-)".-"(.-)".-</li>') do
            name = decodeunicode(strip(name))
            url = decodeunicode(strip(url))
            table.insert(tab,1,name .. '||' .. host .. url)
        end
    end
    return #tab,tab
end

function getpics(datafile)
    local tab = {}
    local host = "http://www.dmzj.com/"
    io.input(datafile)
    local s=io.read("*all")
    local block = string.match(s,'var%spages(.-)var%s')
    for url in string.gmatch(block, '"(.-)"') do
        url = strip(url)
        url = string.gsub(url,"\\/","/")
        url = decodeunicode(url)
        url = escapenonascii(url)
        if string.sub(url,1,1) == '/' then
            url = string.sub(url,2)
        end
        table.insert(tab,host .. url)
    end
    return #tab,tab
end
