-- site config file

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
    local t = {["key"]=keyword}
    table.insert(tab,'http://www.6manga.com/page/?Search')
    table.insert(tab,encode(t))
    table.insert(tab,'POST')
    return #tab,tab
end

function getcomics(datafile)
    local tab = {}
    local host = 'http://www.6manga.com'
    io.input(datafile)
    local s=io.read("*all")
    for block in string.gmatch(s,'id="_ctl3_dl"(.-)id="_ctl3_tb_page"') do
        for url,name,update in string.gmatch(block, '<tr id="_ctl3_dl__ctl%d_img">.-href="(.-)".-alt="(.-)".-</tr>.-<tr>.-class=Comic.-<font%sclass=num>(.-)<.-</tr>') do
            name = (strip(name))
            author = ''
            url = host .. (strip(url))
            table.insert(tab,name .. '||' .. author .. '||' .. update .. '||' .. url )
        end
    end
    return #tab,tab
end

function getparts(datafile)
    local tab = {}
    local host = 'http://www.6manga.com'
    io.input(datafile)
    local s=io.read("*all")
    for block in string.gmatch(s,'<tbody id="tr1"(.-)</tbody>') do
        for url,name in string.gmatch(block, "<td.-href='(.-)'.->([^<>]+)<.-</td>") do
            name = (strip(name))
            url = host .. (strip(url))
            table.insert(tab, 1, name .. '||' .. url)
        end
    end
    return #tab,tab
end

--function showpic(pic,p){var domain="";if(pic.substring(2,3)=="/"){if(typeof(comics)!="undefined" && comics!='') domain=comics;if(typeof(ccomic)!="undefined") domain=ccomic(domain,pic);$("TheImg").src=domain+"/comics/"+pic.substring((p-1)*20,p*20)+".jpg";}
--else {if(typeof(mangas)!="undefined" && mangas!='') domain=mangas;if(typeof(cmanga)!="undefined") domain=cmanga(domain,pic);$("TheImg").src=domain+"/mangas/"+pic.substring((p-1)*19,p*19)+".jpg";}
--}
function getpics(datafile)
    local tab = {}
    local host = "http://www.6manga.com"
    io.input(datafile)
    local s=io.read("*all")
    local block = string.match(s,"var%spic='(.-)'")
    for url in splitn(block, 20) do
        url = host .. '/comics/' .. strip(url) .. '.jpg'
        table.insert(tab, url)
    end
    return #tab,tab
end
