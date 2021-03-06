package cn.wecuit.backen.service.api.impl;

import cn.wecuit.backen.utils.HTTP.HttpUtil2;
import cn.wecuit.backen.utils.JsonUtil;
import cn.wecuit.backen.utils.JwcUtil;
import cn.wecuit.backen.service.api.IJwcService;
import cn.wecuit.backen.service.impl.BaseServiceImpl;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author jiyec
 * @Date 2021/5/1 19:18
 * @Version 1.0
 **/
public class JwcServiceImpl extends BaseServiceImpl implements IJwcService {

    /**
     * 根据条件查询实验室
     *
     * @throws IOException
     */
    @Override
    public void labAllAction() throws IOException, ParseException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> parameter = new HashMap<>();
        parameterMap.forEach((k, v)->{
            parameter.put(k,v[0]);
        });

        HttpUtil2 http = new HttpUtil2(new HashMap<String, Object>() {{
            put("redirection", 0);
        }});
        String html = http.doGet("http://jxgl.cuit.edu.cn/Jxgl/Js/sysYxgl/Cx/sysHz.asp", parameter, "gb2312");

        //数据解析
        Map<String, Object> ret = JwcUtil.LAB_ListHtml2json(html);

        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("errorCode", 2000);
            put("data", ret);
        }}));
    }

    /**
     * 根据条件查询实验室具体安排
     *
     * @throws IOException
     */
    @Override
    public void labDetailAction() throws IOException, ParseException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> parameter = new HashMap<>();
        parameterMap.forEach((k, v)->{
            parameter.put(k,v[0]);
        });
        /*
            cxZtO: 8
            cxZcO:
            Kkxq: 20202                  ----学期
            Yx: 0                        ----院系
            Rw: 1                        ----承担任务
            Sys:                         ----实验室名称（可输入名称的一部分）
            Fj: HSZXB215                 ----房间（可只输入房间编号的左边部分）
            Jxb:                         ----教学班（可只输入班简名的左边部分）
            Zjjs:                        ----教师（可只输入姓名的左边部分）
            Jxkc:                        ----课程（可只输入课程名称的左边部分）
            cxZt: 8                      ----时间
            cxZc:                        ----周次
            Lb: 1                        ----类别
        */
        HttpUtil2 http = new HttpUtil2(new HashMap<String, Object>() {{
            put("redirection", 0);
        }});
        String html = http.doGet("http://jxgl.cuit.edu.cn/Jxgl/Js/sysYxgl/Cx/sysKb.asp", parameter, "gb2312");
        html = html.replace("请输入实验室名称", "").replace("<br>", "").replace("　　", " ");

        // 数据解析
        Map<String, Object> ret = JwcUtil.LAB_DetailHtml2json(html);

        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("errorCode", 2000);
            put("data", ret);
        }}));
    }
}
