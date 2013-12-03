-- site config file
--function showpic(pic,p){var domain="";if(pic.substring(2,3)=="/"){if(typeof(comics)!="undefined" && comics!='') domain=comics;if(typeof(ccomic)!="undefined") domain=ccomic(domain,pic);$("TheImg").src=domain+"/comics/"+pic.substring((p-1)*20,p*20)+".jpg";}
--else {if(typeof(mangas)!="undefined" && mangas!='') domain=mangas;if(typeof(cmanga)!="undefined") domain=cmanga(domain,pic);$("TheImg").src=domain+"/mangas/"+pic.substring((p-1)*19,p*19)+".jpg";}
--}

function getconfig()
    local tab = {}
    table.insert(tab,'6manga.com')
    table.insert(tab,'big5')  -- page's encoding
    table.insert(tab,'0')  -- 1:默认隐藏 0:默认显示
    table.insert(tab,'0')  -- 1:仿人行为 0:默认行为
    table.insert(tab,'big5')  -- search's encoding
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
