-- site config file

function getconfig()
    local tab = {}
    table.insert(tab,'dm5.com')
    table.insert(tab,'utf8')  -- page's encoding
    table.insert(tab,'0')  -- 1:默认隐藏 0:默认显示
    table.insert(tab,'0')  -- 1:仿人行为 0:默认行为
    table.insert(tab,'utf8')  -- search's encoding
    return #tab,tab
end

function getsearchparam(keyword)
    local tab = {}
    local t = {["title"]=keyword,["language"]=1,["t"]=0}
    table.insert(tab,'http://www.dm5.com/search?')
    table.insert(tab,encode(t))
    table.insert(tab,'GET')
    return #tab,tab
end

function getcomics(datafile)
    local tab = {}
    local host = 'http://www.dm5.com'
    io.input(datafile)
    local s=io.read("*all")
    for block in string.gmatch(s,'<div%sid="search_nrl">(.-)<div%sid="search_nrr">') do
        for update,url,name,author in string.gmatch(block, '<div%sclass="ssnrk">.-<div%sclass="ssnr_bt">.-</font>(.-)</a>.-<div%sclass="ssnr_yt">.-href="(.-)"%stitle="(.-)".-<dt>.-<a href="#">(.-)</a></span>.-</div>') do
            name = (strip(name))
            url = host .. (strip(url))
            print(name .. url)
            table.insert(tab,name .. '||' .. author .. '||' .. update .. '||' .. url )
        end
    end
    return #tab,tab
end

function getparts(datafile)
    local tab = {}
    local host = 'http://www.dm5.com'
    io.input(datafile)
    local s=io.read("*all")
    for block in string.gmatch(s,'<ul class="nr6 lan2"(.-)</ul>') do
        for url,name in string.gmatch(block, '<li.-href="(.-)"%stitle="(.-)".-</li>') do
            name = (strip(name))
            url = host .. (strip(url))
            table.insert(tab, 1, name .. '||' .. url)
        end
    end
    return #tab,tab
end

--http://www.dm5.com/m147326-p7/chapterimagefun.ashx?cid=147326&page=7&language=1&key=
--加密的:(
--eval(function(p,a,c,k,e,d){e=function(c){return(c<a?"":e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)d[e(c)]=k[c]||e(c);k=[function(e){return d[e]}];e=function(){return'\\w+'};c=1;};while(c--)if(k[c])p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c]);return p;}('l 9(){2 6=4;2 8="a";2 5="n://m.k-7-p-o.j.e/c/b/4";2 3=["/h.1","/g.1","/f.1","/v.1","/w.1","/x.1","/z.1","/y.1","/r.1","/q.1"];s(2 i=0;i<3.u;i++){3[i]=5+3[i]+\'?6=4&8=a\'}t 3}2 d;d=9();',36,36,'|jpg|var|pvaluetttt|147326|pix|cid||key|dm5imagefun|e7f7d4785f8733ab9704ef933bd45908|9827|10||com|9_9150|8_2443|7_5669||cdndm5|198|function|manhua1023|http|230|62|16_1865|15_7206|for|return|length|10_2426|11_9662|12_7134|14_1640|13_5301'.split('|'),0,{}))
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
