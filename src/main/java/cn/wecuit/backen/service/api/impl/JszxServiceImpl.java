package cn.wecuit.backen.service.api.impl;

import cn.wecuit.backen.service.api.IJszxService;
import cn.wecuit.backen.utils.CCUtil;
import cn.wecuit.backen.utils.HTTP.HttpUtil2;
import cn.wecuit.backen.utils.JsonUtil;
import cn.wecuit.backen.utils.RSAUtils;
import cn.wecuit.backen.exception.BaseException;
import cn.wecuit.backen.service.impl.BaseServiceImpl;
import cn.wecuit.backen.utils.HTTP.HttpUtilEntity;
import org.apache.hc.core5.http.ParseException;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author jiyec
 * @Date 2021/4/29 18:29
 * @Version 1.0
 **/
public class JszxServiceImpl extends BaseServiceImpl implements IJszxService {

    /**
     * 获取打卡列表
     * @throws IOException
     */
    @Override
    public void getCheckInListV2Action() throws IOException, ParseException {
        String cookie = request.getParameter("cookie");
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.0 Safari/537.36 Edg/84.0.521.0");
        headers.put("Cookie", cookie);

        HttpUtil2 http = new HttpUtil2(new HashMap<String, Object>() {{
            put("redirection", 0);
        }});
        HttpUtilEntity resp = http.doGetEntity("http://jszx-jxpt.cuit.edu.cn/Jxgl/Xs/netks/sj.asp?jkdk=Y", headers, "gb2312");
        if(resp.getStatusCode() != 200)
            throw new BaseException(20401, "计算中心还未登录");

        String html = resp.getBody();
        Map<String, List<Map<String, String>>> checkInList = CCUtil.parseCheckInList(html);
        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("errorCode", 2000);
            put("list", checkInList);
        }}));
    }

    /**
     * 计算中心登录操作
     *
     * @throws Exception
     */
    @Override
    public void loginRSAv1Action() throws Exception {
        // 字符较长，使用流式传输
        ServletInputStream inputStream = request.getInputStream();

        byte[] data = new byte[request.getContentLength()];
        inputStream.read(data);

        String info = new String(data);
        Map<String, String> uInfo = JsonUtil.string2Obj(info, Map.class);

        String userId = uInfo.get("userId");
        if(userId.length() > 15)
            userId = RSAUtils.decryptRSAByPriKey(userId);
        String userPass = RSAUtils.decryptRSAByPriKey(uInfo.get("userPass"));

        // 登录操作
        String loginCookie = CCUtil.login(userId, userPass);

        // 响应体
        Map<String, Object> ret = new HashMap<>();
        ret.put("errorCode", 2000);
        ret.put("status", 2000);
        ret.put("cookie", loginCookie);

        response.getWriter().print(JsonUtil.obj2String(ret));
    }

    /**
     * 获取打卡内容
     *
     */
    @Override
    public void getCheckInEditV2Action() throws IOException, ParseException {
        String cookie = request.getParameter("cookie");
        String link = request.getParameter("link");

        HttpUtil2 http = new HttpUtil2(new HashMap<String, Object>() {{
            put("redirection", 0);
        }});
        String reqUrl = "http://jszx-jxpt.cuit.edu.cn/Jxgl/Xs/netks/sjDb.asp?" + link;
        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", cookie);
        HttpUtilEntity httpUtilEntity = http.doGetEntity(reqUrl, headers);
        String location = httpUtilEntity.getHeaders().get("Location");
        httpUtilEntity = http.doGetEntity("http://jszx-jxpt.cuit.edu.cn/Jxgl/Xs/netks/" + location, headers, "GB2312");
        if(302 == httpUtilEntity.getStatusCode())throw new BaseException(20401, "未登录");

        String html = httpUtilEntity.getBody();
        Map<String, Object> form = CCUtil.parseCheckInContent(html);
        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("errorCode", 2000);
            put("form", form);
        }}));
    }

    /**
     * 提交打卡
     */
    @Override
    public void doCheckInV3Action() throws IOException, ParseException {
        ServletInputStream is = request.getInputStream();
        byte[] content = new byte[request.getContentLength()];
        is.read(content);

        String post = new String(content);
        LinkedHashMap postMap = JsonUtil.string2Obj(post, LinkedHashMap.class);
        String cookie = (String)postMap.get("JSZXCookie");

        Map<String, String> form = (LinkedHashMap<String, String>)postMap.get("form");
        form = CCUtil.genPostBody(form, "?" + (String)postMap.get("link"));

        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", cookie);
        headers.put("referer", "http://jszx-jxpt.cuit.edu.cn/");

        HttpUtil2 http = new HttpUtil2(new HashMap<String, Object>() {{
            put("redirection", 0);
        }});
        String url = "http://jszx-jxpt.cuit.edu.cn/Jxgl/Xs/netks/editSjRs.asp";
        HttpUtilEntity httpUtilEntity = http.doPostEntity(url, form, headers, "GB2312");
        if(200 != httpUtilEntity.getStatusCode())throw new BaseException(20401, "未登录");

        String html = httpUtilEntity.getBody();
        Pattern compile = Pattern.compile(">打卡时间：(.*?)</");
        Matcher matcher = compile.matcher(html);
        StringBuilder time = new StringBuilder();
        if(matcher.find()){
            time.append(matcher.group(1));
        }

        if(html.contains("提交打卡成功！")) {
            Map<String, Object> newForm = CCUtil.parseCheckInContent(html);
            response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
                put("errorCode", 2000);
                put("errMsg", time.toString());
                put("form", newForm);
            }}));
        }else{
            response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
                put("errorCode", 2005);
                put("errMsg", "失败了╮(╯▽╰)╭");
            }}));
        }

    }

    /**
     * OFFICE
     */
    @Override
    public void office_prepareAction() throws IOException, ParseException {
        Map<String, String> headers = new HashMap<String, String>(){{
            put("referer", "http://login.cuit.edu.cn:81/Login/xLogin/Login.asp");
        }};
        HttpUtil2 http = new HttpUtil2();
        String html = http.doGet2("http://login.cuit.edu.cn:81/Login/xLogin/Login.asp", headers);
        Pattern compile = Pattern.compile("<input type=\"hidden\" name=\"codeKey\" value=\"(\\d+)\"");
        Matcher matcher = compile.matcher(html);
        String codeKey = "";
        if(matcher.find())
            codeKey = matcher.group(1);

        compile = Pattern.compile("<span style=\"color:#0000FF;\">(.*?)</span");
        matcher = compile.matcher(html);
        String syncTime = "";
        if(matcher.find())
            syncTime = matcher.group(1);
        Map<String, String> cookieMap = http.getCookie();
        StringBuilder cookie = new StringBuilder();
        cookieMap.forEach((k,v)->{
            cookie.append(k + "=" + v + ";");
        });

        String finalCodeKey = codeKey;
        String finalSyncTime = syncTime;
        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("status", 2000);
            put("errorCode", 2000);
            put("cookie", cookie.toString());
            put("codeKey", finalCodeKey);
            put("syncTime", finalSyncTime);
        }}));
    }

    @Override
    public void office_getCaptchaAction() throws IOException, ParseException {
        String cookie = request.getParameter("cookie");
        String codeKey = request.getParameter("codeKey");

        HashMap<String, String> headers = new HashMap<String, String>(){{
            put("cookie", cookie);
            put("referer", "http://login.cuit.edu.cn:81/Login/xLogin/Login.asp");
        }};
        HttpUtil2 http = new HttpUtil2();
        byte[] body = http.getContent("http://login.cuit.edu.cn:81/Login/xLogin/yzmDvCode.asp?k=" + codeKey, null, headers, "UTF-8");

        ServletContext servletContext = request.getServletContext();
        String OCR_SERVER = servletContext.getInitParameter("OCR_SERVER");
        String s = http.doFilePost(OCR_SERVER, body);
        Map<String, String> map = JsonUtil.string2Obj(s, Map.class);

        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("status", 2000);
            put("errorCode", 2000);
            put("base64img", "data:image/png;base64, " + new String(Base64.getEncoder().encode(body)));
            put("imgCode", map.get("result"));
        }}));
    }

    @Override
    public void office_queryAction() throws IOException, ParseException {
        String cookie = request.getParameter("cookie");
        String codeKey = request.getParameter("codeKey");
        String captcha = request.getParameter("captcha");
        String nickname = request.getParameter("nickname");
        String email = request.getParameter("email");

        Map<String, String> headers = new HashMap<String, String>(){{
            put("cookie", cookie);
            put("referer", "http://login.cuit.edu.cn:81/Login/xLogin/Login.asp");
        }};
        Map<String, String> param = new LinkedHashMap<String, String>(){{
            put("WinW", "1304");
            put("WinH", "768");
            put("txtId", nickname);
            put("txtMM", email);
            put("verifycode", captcha);
            put("codeKey", codeKey);
            put("Login", "Check");
            put("IbtnEnter.x", "8");
            put("IbtnEnter.y", "26");
        }};

        HttpUtil2 http = new HttpUtil2();
        String html = http.doPost("http://login.cuit.edu.cn:81/Login/xLogin/Login.asp", param, headers, "GB2312");
        Pattern compile = Pattern.compile("class=user_main_z(.*?)</span");
        Matcher matcher = compile.matcher(html);

        StringBuilder result = new StringBuilder();

        if(matcher.find()){
            result.append(matcher.group(1));
        }
        String msg = result.substring(result.lastIndexOf(">") + 1);

        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("status", 2000);
            put("errorCode", 2000);
            put("result", msg);
        }}));
    }


}
