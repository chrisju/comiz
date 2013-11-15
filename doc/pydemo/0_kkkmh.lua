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
