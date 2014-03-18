-- site config file

function getconfig()
    local tab = {}
    table.insert(tab,'kkkmh.com')
    table.insert(tab,'utf-8')  -- page's encoding
    table.insert(tab,'0')  -- 1:默认隐藏 0:默认显示
    table.insert(tab,'0')  -- 1:仿人行为 0:默认行为
    table.insert(tab,'utf-8')  -- search's encoding
    return #tab,tab
end

function getsearchparam(keyword)
    local tab = {}
    local t = {["keyword"]=keyword,["t"]="c"}
    table.insert(tab,'http://www.kkkmh.com/search.php')
    table.insert(tab,encode(t))
    table.insert(tab,'GET')
    return #tab,tab
end

function getcomics(datafile)
    local tab = {}
    local host = "http://www.kkkmh.com"
    io.input(datafile)
    local s=io.read("*all")
    for url,name,update in string.gmatch(s, 'class="intro".-href="(.-)".-title="(.-)".-<em><big.->(.-)<.-</span>') do
        table.insert(tab,strip(name) .. '||' .. '' .. '||' .. strip(update) .. '||' .. host .. strip(url))
    end
    return #tab,tab
end

function getparts(datafile)
    local tab = {}
    local host = "http://www.kkkmh.com"
    io.input(datafile)
    local s=io.read("*all")
    for block in string.gmatch(s,'class="outer"(.-)</div>') do
        for url,name in string.gmatch(block, '<li>.-href="(.-)".->(.-)</a></li>') do
            table.insert(tab,1,strip(name) .. '||' .. host .. strip(url))
        end
    end
    return #tab,tab
end

function getpics(datafile)
    local tab = {}
    local host = "http://mhauto.kkkmh.com"
    io.input(datafile)
    local s=io.read("*all")
    local block = string.match(s,'pic%[0%](.-)recentVisitUpdate')
    for url in string.gmatch(block, "'(.-)'") do
        table.insert(tab,host .. hex2str(strip(url)))
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
    local host = "http://xindm.cn"
    io.input(datafile)
    local s=io.read("*all")
    local block = string.match(s,'class="main4box"(.-)</table>')
    for url in string.gmatch(block, '<ul><li>.-</li><a%shref="(.-)">.-&raquo;</a></ul>') do
        url = host .. strip(url)
        table.insert(tab,url)
    end
    return #tab,tab
end

function getcatcomics(datafile)
    local tab = {}
    io.input(datafile)
    local s=io.read("*all")
    local block = string.match(s,'class="reminderindex%sright"(.-)class="main01box"')
    for url,name,update in string.gmatch(block, '<li>.-href="(.-)".-title="(.-)".-%[<.->(.-)</a>%].-</li>') do
        name = (strip(name))
        author = ''
        url = (strip(url))
        update = (strip(update))
        table.insert(tab,name .. '||' .. author .. '||' .. update .. '||' .. url )
    end
    return #tab,tab
end
--------------- end navigate -----------------
