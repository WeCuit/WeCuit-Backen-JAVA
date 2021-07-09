package cn.wecuit.backen.service.api.impl;

import cn.wecuit.backen.exception.BaseException;
import cn.wecuit.backen.service.api.ICardService;
import cn.wecuit.backen.utils.ApiUtil;
import cn.wecuit.backen.utils.CardUtil;
import cn.wecuit.backen.utils.HTTP.HttpUtil2;
import cn.wecuit.backen.utils.JsonUtil;
import cn.wecuit.backen.utils.URLUtil;
import cn.wecuit.backen.service.impl.BaseServiceImpl;
import cn.wecuit.backen.utils.HTTP.HttpUtilEntity;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author jiyec
 * @Date 2021/5/2 15:18
 * @Version 1.0
 **/
public class CardServiceImpl extends BaseServiceImpl implements ICardService {
    @Override
    public void loginAction() throws IOException, ParseException {
        String cookie = request.getParameter("cookie");

        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", "TGC=" + cookie);

        HttpUtil2 http = new HttpUtil2(new HashMap<String, Object>() {{
            put("redirection", 0);
        }});
        HttpUtilEntity httpUtilEntity = http.doGetEntity("https://sso.cuit.edu.cn/authserver/login?service=http%3a%2f%2fykt.cuit.edu.cn%3a12491%2flogin.aspx", headers);
        if(httpUtilEntity.getStatusCode() != 302) throw new BaseException(12401, "SSO未登录");
        String location = httpUtilEntity.getHeaders().get("Location");

        // http://ykt.cuit.edu.cn:12491/login.aspx?ticket=ST-18**********w-localhost
        httpUtilEntity = http.doGetEntity(location, headers);
        if(httpUtilEntity.getStatusCode() != 302) throw new BaseException(12401, "异常");
        location = httpUtilEntity.getHeaders().get("Location");
        Map<String, String> cookies = httpUtilEntity.getCookies();
        StringBuilder temp = new StringBuilder();
        cookies.forEach((k,v)->{
            temp.append(k).append("=").append(v).append(";");
        });
        headers.put("cookie", temp.toString());

        // http://ykt.cuit.edu.cn:12491/login.aspx
        httpUtilEntity = http.doGetEntity(location, headers);
        location = httpUtilEntity.getHeaders().get("Location");
        if(!location.contains("getUserInfoById")) throw new BaseException(1, "失败");

        Map<String, String> urlQuery = URLUtil.getURLQuery(location);
        String idNo = urlQuery.get("IDNo");
        byte[] decode = Base64.getDecoder().decode(idNo);
        decode = Base64.getDecoder().decode(decode);
        decode = Base64.getDecoder().decode(decode);
        idNo = new String(decode);

        String finalIdNo = idNo;
        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("status", 2000);
            put("errorCode", 2000);
            put("data", new HashMap<String, String>(){{
                put("AccNum", finalIdNo);
            }});
        }}));
    }

    @Override
    public void getAccWalletAction() throws IOException, NoSuchAlgorithmException, ParseException {
        String accNum = request.getParameter("AccNum");
        Map<String, String> param = new LinkedHashMap<>();
        param.put("AccNum", accNum);
        param.put("ContentType", "json");
        Map<String, String> sign = CardUtil.genSign(param, "AccWallet");
        param.putAll(sign);

        HttpUtil2 http = new HttpUtil2(new HashMap<String, Object>() {{
            put("redirection", 0);
        }});
        String result = http.doGet("http://ykt.cuit.edu.cn:12490/QueryAccWallet.aspx", param);

        Map map = JsonUtil.string2Obj(result, Map.class);
        map.put("status", 2000);
        map.put("errorCode", 2000);
        String code = (String)map.get("Code");
        if("1".equals(code)){
            map.put("status", 2000);
            map.put("errorCode", 2000);
        }else{
            map.put("status", code);
            map.put("errorCode", code);
        }

        response.getWriter().print(JsonUtil.obj2String(map));
    }

    @Override
    public void getDealRecAction() throws NoSuchAlgorithmException, IOException, ParseException {
        Map<String, String> parameter = ApiUtil.getParameter(request.getParameterMap());
        Map<String, String> sign = CardUtil.genSign(parameter, "DealRec");
        parameter.put("ContentType", "json");
        parameter.putAll(sign);

        HttpUtil2 http = new HttpUtil2(new HashMap<String, Object>() {{
            put("redirection", 0);
        }});
        String result = http.doGet("http://ykt.cuit.edu.cn:12490/QueryDealRec.aspx", parameter);
        Map map = JsonUtil.string2Obj(result, Map.class);
        String code = (String)map.get("Code");
        if("1".equals(code)){
            map.put("status", 2000);
            map.put("errorCode", 2000);
        }else{
            map.put("status", code);
            map.put("errorCode", code);
        }

        response.getWriter().print(JsonUtil.obj2String(map));
    }
}
