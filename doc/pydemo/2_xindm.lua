-- site config file

function getconfig()
    local tab = {}
    table.insert(tab,'xindm.cn')
    table.insert(tab,'gbk')  -- page's encoding
    table.insert(tab,'0')  -- 1:默认隐藏 0:默认显示
    table.insert(tab,'0')  -- 1:仿人行为 0:默认行为
    table.insert(tab,'gbk')  -- search's encoding
    table.insert(tab,'http://xindm.cn/')  -- homepage
    return #tab,tab
end

function getsearchparam(keyword)
    local tab = {}
    local t = {["show"]="title,btitle",["keyboard"]=keyword}
    table.insert(tab,'http://www.xindm.cn/e/search/index.php')
    table.insert(tab,encode(t))
    table.insert(tab,'POST')
    return #tab,tab
end

function getcomics(datafile)
    local tab = {}
    io.input(datafile)
    local s=io.read("*all")
    for url,name,author in string.gmatch(s, 'class="recommendedpic1 center".-href="(.-)".-title="(.-)".-作者:(.-)<.-</div>') do
        name = (strip(name))
        author = (strip(author))
        url = (strip(url))
        table.insert(tab,name .. '||' .. author .. '||' .. '' .. '||' .. url )
    end
    return #tab,tab
end

function getparts(datafile)
    local tab = {}
    io.input(datafile)
    local s=io.read("*all")
    for block in string.gmatch(s,'class="subsrbelist center".-</table>(.-)</table>') do
        for url,name in string.gmatch(block, '<li>.-href="(.-)".->.->(.-)<.-</li>') do
            name = (strip(name))
            url = (strip(url))
            table.insert(tab,name .. '||' .. url) --no need revert
        end
    end
    return #tab,tab
end

function getpics(datafile)
    local tab = {}
    local host = "http://mh2.xindm.cn"
    io.input(datafile)
    local s=io.read("*all")
    local block = string.match(s,'var%sArrayPhoto.-%((.-)%)')
    for url in string.gmatch(block, '"(.-)"') do
        url = strip(url)
        table.insert(tab,host .. url)
    end
    return #tab,tab
end

--------------- navigate ---------------------
function gethomecomics(datafile)
    local tab = {}
    local host = 'http://xindm.cn'
    io.input(datafile)
    local s=io.read("*all")
    --1
    local block = string.match(s,'<table.-class="hot_black"(.-)</table>')
    for url,name in string.gmatch(block, '<tr>.-<a href="(.-)"%starget="_blank">.->(.-)</span>.-<span.-</tr>') do
        name = (strip(name))
        author = ''
        update = ''
        url = (strip(url))
        if not startswith(url,'http://') then
            url = host .. url
        end
        table.insert(tab,name .. '||' .. author .. '||' .. update .. '||' .. url )
    end
    --2
    block = string.match(s,'<div%sclass="flash%-tag%sr"><ul>(.-)</ul>')
    for url,name in string.gmatch(block, '<li%sid=.-<a%shref="(.-)"%stitle="(.-)".-</li>') do
        name = (strip(name))
        author = ''
        update = ''
        url = (strip(url))
        if not startswith(url,'http://') then
            url = host .. url
        end
        table.insert(tab,name .. '||' .. author .. '||' .. update .. '||' .. url )
    end
    --3
    block = string.match(s,'class="comicswitchtext1"(.-)</td>')
    for url,name in string.gmatch(block, '<div><a href="(.-)".-title="(.-)".-</div>') do
        name = (strip(name))
        author = ''
        update = ''
        url = (strip(url))
        if not startswith(url,'http://') then
            url = host .. url
        end
        table.insert(tab,name .. '||' .. author .. '||' .. update .. '||' .. url )
    end
    --4
    block = string.match(s,'class="main1box"(.-)</div>')
    for url,name in string.gmatch(block, '<li>.-title="(.-)".-href="(.-)".-</li>') do
        name = (strip(name))
        author = ''
        update = ''
        url = (strip(url))
        if not startswith(url,'http://') then
            url = host .. url
        end
        table.insert(tab,name .. '||' .. author .. '||' .. update .. '||' .. url )
    end
    return #tab,tab
end

function getcats(datafile)
    local tab = {}
    local host = "http://mh2.xindm.cn"
    io.input(datafile)
    local s=io.read("*all")
    local block = string.match(s,'var%sArrayPhoto.-%((.-)%)')
    for url in string.gmatch(block, '"(.-)"') do
        url = strip(url)
        table.insert(tab,host .. url)
    end
    return #tab,tab
end

function getcatcomics(datafile)
    local tab = {}
    io.input(datafile)
    local s=io.read("*all")
    for url,name,author in string.gmatch(s, 'class="recommendedpic1 center".-href="(.-)".-title="(.-)".-作者:(.-)<.-</div>') do
        name = (strip(name))
        author = (strip(author))
        url = (strip(url))
        table.insert(tab,name .. '||' .. author .. '||' .. '' .. '||' .. url )
    end
    return #tab,tab
end
--------------- end navigate -----------------
