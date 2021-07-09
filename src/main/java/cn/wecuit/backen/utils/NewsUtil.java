package cn.wecuit.backen.utils;

import cn.wecuit.robot.data.NewsStorage;
import cn.wecuit.backen.entity.News;
import cn.wecuit.backen.utils.HTTP.HttpUtil;
import cn.wecuit.robot.RobotMain;
import cn.wecuit.robot.provider.NewsProvider;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.LightApp;
import org.apache.hc.core5.http.ParseException;
import org.jsoup.nodes.Element;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Slf4j
public class NewsUtil {

    public static void pullNews(String dir, News news){
        new NewsTask(dir, news).run();
    }
    public static void pullNews(){
        String path = Objects.requireNonNull(TaskUtil.class.getResource("/")).getPath();

        String newsConfig = FileUtil.ReadFile(path + "newsConfig.json");

        News[] newsArray = JsonUtil.string2Obj(newsConfig, News[].class);

        // 获取数据缓存路径
        String cachePath = System.getProperty("java.io.tmpdir") + "/WeCuit/cache";

        // 启动多线程处理新闻
        for (News news : newsArray) {
            new NewsTask(cachePath + "/news/list", news).start();
        }
    }

    public static void newsNotice(List<String> noticeList) throws IOException {
        // 获取数据缓存路径
        String cachePath = System.getProperty("java.io.tmpdir") + "/WeCuit/cache";
        // 获取最新新闻列表
        List<Map<String, String>> latestNews = NewsUtil.getLatestNews(cachePath + "/news/list", 1);
        latestNews.forEach(news->{
            log.info("新闻：{}", news);
            String link = news.get("link");
            String title = news.get("title");
            try {
                byte[] bytes = RSAUtils.genMD5((link + title).getBytes(StandardCharsets.UTF_8));
                String md5 = HexUtil.byte2HexStr(bytes);

                if(!NewsStorage.isNewsExist(md5)) {
                    // 新的新闻
                    NewsStorage.addNews(md5);

                    // 构造小程序完毕
                    if(noticeList.size() == 0)
                        log.info("没有需要推送的群");
                    noticeList.forEach((id)->{
                        log.info("id: {}", id);
                        Group group = RobotMain.getBot().getGroup(Long.parseLong(id));
                        if(group != null) {
                            try {
                                group.sendMessage(new LightApp(NewsProvider.genLightJson(news)));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            log.info("似乎没有找到群？");
                    });
                }else{
                    log.info("该新闻已提醒");
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 获取每个学院最新的新闻
     * @param listPath 新闻列表存储目录
     * @param dayRange  最近 [dayRange] 天的新闻
     * @return
     * @throws IOException
     */
    public static List<Map<String, String>> getLatestNews(String listPath, int dayRange) throws IOException {
        File listDir = new File(listPath);
        String[] list = listDir.list();

        List<Map<String, String>> latestNews = new LinkedList<>();

        if(list == null){
            return latestNews;
        }

        for (String dir: list){
            // 学院目录
            String xyPath = listPath + "/" + dir;
            File xy = new File(xyPath);
            File[] files = xy.listFiles((dir1, name) -> name.endsWith("_1.json"));

            for(File file: files){
                // 读取内容
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                StringBuilder content = new StringBuilder();
                String line;
                while((line=bufferedReader.readLine()) != null){
                    content.append(line);
                }
                bufferedReader.close();

                // 文件内容读取完毕，开始解析并添加至列表
                List<Map<String, String>> latestList = getLatestItem(content.toString(), dayRange, dir);
                if(null != latestList)
                    latestNews.addAll(latestList);
            }
        }

        latestNews.sort((o1, o2) -> {
            if(!o2.get("date").equals(o1.get("date")))
                return o2.get("date").compareTo(o1.get("date"));
            return o2.get("link").compareTo(o1.get("link"));
        });
        return latestNews;
    }

    private static List<Map<String, String>> getLatestItem(String json, int dayRange, String source){

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM-dd");
        // String today = dateFormat1.format(new Date());
        // dateFormat.parse("").getTime();
        long nowTime = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));

        Map<String, Object> news = JsonUtil.string2Obj(json, Map.class);
        List<Map<String, String>> list = (List<Map<String, String>>)news.get("list");

        // 获取 {dayRange} 天之内的新闻
        Stream<Map<String, String>> mapStream = list.stream().filter(m -> {
            try {
                String date = m.get("date");
                Long newsTime = date.length() > 5 ? dateFormat1.parse(date).getTime()/1000 : dateFormat2.parse(date).getTime()/1000;
                if(nowTime - newsTime <= 3600L * 24 * dayRange)
                {
                    log.debug("现在时间：{} 新闻时间：{}", nowTime, newsTime);
                    m.put("name", (String)news.get("name"));
                    m.put("domain", (String)news.get("domain"));
                    m.put("source", source);
                    return true;
                }
                return false;
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            return false;
        });
        List<Map<String, String>> collect = mapStream.collect(Collectors.toList());

        return collect;
    }
}

@Slf4j
class NewsTask extends Thread{

    private final News news;
    private final String dir;

    /**
     * 构造方法
     *
     * @param dir  存储路径
     * @param news  新闻实体
     */
    public NewsTask(String dir, News news){
        this.news = news;
        this.dir = dir;
    }
    public void run(){
        String pullFun = "v" + news.getPullVer() + "_pull";

        try {
            // 反射调用指定版本的解析方法
            Method method = this.getClass().getMethod(pullFun);
            method.invoke(this);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void v1_pull(){
        String path = this.dir + "/" + news.getSource();
        File folder = new File(path);

        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }

        news.getTags().forEach(o -> {
            String name = o.get("name");

            int page = 1;

            Map<String, Object> ret;
            boolean next;
            do{
                ret = v1_list(name, page);
                next = (boolean)ret.get("next");
                ret.remove("next");
                ret.put("errorCode", 2000);
                ret.put("name", news.getName());
                FileUtil.WriteFile(path + "/" + name + "_" + page + ".json", JsonUtil.obj2String(ret));
                page++;
            }while(next);

            o.put("total", Integer.toString(page));
        });
        FileUtil.WriteFile(path + "/tags.json", JsonUtil.obj2String(news.getTags()));
    }

    private Map<String, Object> v1_list(String tag, int page){
        String uri = news.getUriExp().replace("#tag#", tag);
        uri = uri.replace("#page#", page + "");
        Map<String, Object> ret = new HashMap<>();

        try {
            ret.put("domain", new URL(uri).getHost());
            ret.put("next", false);
            List<Map<String, String>> list = new LinkedList<>();
            ret.put("list", list);

            log.info("uri: {}", uri);
            String body = HttpUtil.doGet(uri);

            Pattern compile = Pattern.compile(news.getPattern());
            Matcher matcher = compile.matcher(body);

            ret.put("next", body.contains("class=\"Next\">下页</a>"));

            Map<String, String> jo;
            while (matcher.find()){
                jo = new HashMap<>();
                jo.put("date", matcher.group(3).replaceAll("/", "-").replaceAll("\\[|]", ""));
                jo.put("title", matcher.group(2));
                jo.put("link", matcher.group(1));
                list.add(jo);
            }

            if(news.isSort())
                list.sort((o1, o2) -> {
                    if(!o2.get("date").equals(o1.get("date")))
                        return o2.get("date").compareTo(o1.get("date"));
                    return o2.get("link").compareTo(o1.get("link"));
                });
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    // 版本二
    public void v2_pull(){
        String path = this.dir + "/" + news.getSource();
        File folder = new File(path);

        if (!folder.exists() && !folder.isDirectory()) {
            System.out.println(folder.mkdirs());
        }

        news.getTags().forEach(o -> {
            String name = o.get("name");

            int i = 1;
            Map<String, Object> ret;
            String page = null;
            do{
                ret = v2_list(name, page);
                page = (String)ret.get("next");
                ret.remove("next");
                ret.put("errorCode", 2000);
                ret.put("name", news.getName());
                FileUtil.WriteFile(path + "/" + name + "_" + i + ".json", JsonUtil.obj2String(ret));
                i++;
            }while(null != page);

            o.put("total", Integer.toString(i));
        });
        FileUtil.WriteFile(path + "/tags.json", JsonUtil.obj2String(news.getTags()));
    }

    private Map<String, Object> v2_list(String tag, String page){
        String uri = news.getUriExp().replace("#tag#", tag);
        uri += (null != page ? "/" + page : ".htm");
        String link_pre = uri.replaceFirst("\\w+\\.htm", "");

        Map<String, Object> ret = new HashMap<>();

        try {
            ret.put("domain", new URL(uri).getHost());
            ret.put("next", false);
            List<Map<String, String>> list = new LinkedList<>();
            ret.put("list", list);

            log.info("uri: {}", uri);
            String body = HttpUtil.doGet(uri);

            // 处理是否有下一页
            Pattern compile = Pattern.compile("(\\d+\\.htm)\"[^<]+下页");
            Matcher matcher = compile.matcher(body);
            if(matcher.find()){
                String next = matcher.group(1);
                ret.put("next", next);
            }else{
                ret.put("next", null);
            }

            // 解析列表
            compile = Pattern.compile(news.getPattern());
            matcher = compile.matcher(body);

            Map<String, String> jo;
            while (matcher.find()){
                // 真实路径处理
                String link = link_pre + matcher.group(1);
                link = new URL(link).getPath();
                link = getRealPath(link);

                jo = new HashMap<>();
                jo.put("date", matcher.group(3).replaceAll("/", "-").replaceAll("\\[|]", ""));
                jo.put("title", matcher.group(2));
                jo.put("link", link);
                list.add(jo);
            }

            if(news.isSort())
                list.sort((o1, o2) -> {
                    if(!o2.get("date").equals(o1.get("date")))
                        return o2.get("date").compareTo(o1.get("date"));
                    return o2.get("link").compareTo(o1.get("link"));
                });
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    // 版本三
    public void v3_pull(){
        String path = this.dir + "/" + news.getSource();
        File folder = new File(path);

        if (!folder.exists() && !folder.isDirectory()) {
            System.out.println(folder.mkdirs());
        }

        news.getTags().forEach(o->{
            String name = o.get("name");
            try {
                Map<String, Object> v3_list = v3_list(name);
                v3_list.put("name", news.getName());
                FileUtil.WriteFile(path + "/" + name + "_1.json", JsonUtil.obj2String(v3_list));
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            o.put("total", "1");
        });

        FileUtil.WriteFile(path + "/tags.json", JsonUtil.obj2String(news.getTags()));
    }
    private Map<String, Object> v3_list(String tag) throws IOException, ParseException {
        log.info("uri: https://www.cuit.edu.cn/NewsList?id={}", tag);
        String html = HttpUtil.doGet("https://www.cuit.edu.cn/NewsList?id=" + tag);
        html = html.replaceAll("\r\n", "").replaceAll("\n", "").replaceAll("\r","");

        List<Map<String, String>> list = new LinkedList<>();
        Map<String, Object> ret = new HashMap<String, Object>(){{
            put("status", 2000);
            put("errorCode", 2000);
            put("domain", "www.cuit.edu.cn");
            put("list", list);
        }};

        JXDocument jxDocument = JXDocument.create(html);
        List<JXNode> jxNodes = jxDocument.selN("//*[@id=\"NewsListContent\"]/li");
        jxNodes.forEach(e->{
            // System.out.println(e.asString());
            Element element = e.asElement();
            String title = element.child(1).text();
            String link = element.child(1).attr("href");
            String date = element.child(2).text().replaceAll("/", "-").replaceAll("\\[|]", "");
            list.add(new HashMap<String, String>(){{
                put("title", title);
                put("link", link);
                put("date", date);
            }});
        });

        return ret;
    }

    // 版本四
    public void v4_pull(){
        String path = this.dir + "/" + news.getSource();
        File folder = new File(path);

        if (!folder.exists() && !folder.isDirectory()) {
            System.out.println(folder.mkdirs());
        }

        news.getTags().forEach(o -> {
            String name = o.get("name");

            int i = 1;
            Map<String, Object> ret;
            String page = null;
            int pageCnt = 0;
            do{
                ret = v4_list(name, page);
                page = (String)ret.get("next");
                ret.remove("next");
                ret.put("errorCode", 2000);
                ret.put("name", news.getName());
                FileUtil.WriteFile(path + "/" + name + "_" + i + ".json", JsonUtil.obj2String(ret));
                i++;
            }while(null != page && ++pageCnt<5);


            o.put("total", Integer.toString(i));
        });
        FileUtil.WriteFile(path + "/tags.json", JsonUtil.obj2String(news.getTags()));
    }

    private Map<String, Object> v4_list(String tag, String page){
        String uri = news.getUriExp().replace("#tag#", tag);
        uri += (null != page ? "/" + page : ".htm");
        String link_pre = uri.replaceFirst("\\w+\\.htm", "");

        Map<String, Object> ret = new HashMap<>();

        try {
            ret.put("domain", new URL(uri).getHost());
            ret.put("next", false);
            List<Map<String, String>> list = new LinkedList<>();
            ret.put("list", list);

            log.info("uri: {}", uri);
            String body = HttpUtil.doGet(uri);

            // 处理是否有下一页
            Pattern compile = Pattern.compile("(\\d+\\.htm)\"[^<]+下页");
            Matcher matcher = compile.matcher(body);
            if(matcher.find()){
                String next = matcher.group(1);
                ret.put("next", next);
            }else{
                ret.put("next", null);
            }

            // 解析列表
            compile = Pattern.compile(news.getPattern());
            matcher = compile.matcher(body);

            Map<String, String> jo;
            while (matcher.find()){
                // 真实路径处理
                String link = link_pre + matcher.group(2);
                link = new URL(link).getPath();
                link = getRealPath(link);

                jo = new HashMap<>();
                jo.put("date", matcher.group(1).replaceAll("/", "-").replaceAll("\\[|]", ""));
                jo.put("title", matcher.group(3));
                jo.put("link", link);
                list.add(jo);
            }

            if(news.isSort())
                list.sort((o1, o2) -> {
                    if(!o2.get("date").equals(o1.get("date")))
                        return o2.get("date").compareTo(o1.get("date"));
                    return o2.get("link").compareTo(o1.get("link"));
                });
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private String getRealPath( String filename){
        String split = "/";
        while(filename.contains(split + '.')){
            filename = filename.replaceAll("/\\w+/\\.\\./", "/");
            filename = filename.replaceAll("/\\./", "/");
        }
        return filename;
    }

}
