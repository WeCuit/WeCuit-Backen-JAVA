package cn.wecuit.backen.service.api.impl;

import cn.wecuit.backen.service.api.IToolService;
import cn.wecuit.backen.utils.HexUtil;
import cn.wecuit.backen.utils.JsonUtil;
import cn.wecuit.backen.utils.RSAUtils;
import cn.wecuit.backen.exception.BaseException;
import cn.wecuit.backen.service.impl.BaseServiceImpl;
import cn.wecuit.backen.utils.HTTP.HttpUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import java.util.Map;

/**
 * 工具类
 *
 * @author jiyec
 */
public class ToolServiceImpl extends BaseServiceImpl implements IToolService {

    /**
     * 验证码识别
     *
     * @throws Exception
     */
    public void captchaDecodeV2Action() throws Exception {
        // 获取POST 原始数据流
        ServletInputStream is = request.getInputStream();
        if(request.getContentLength() <= 0)throw new BaseException(20500, "请求异常");

        // =======请求合法性验证START====
        int start = request.getContentLength() / 3;
        int end = request.getContentLength() / 2;
        while(end - start > 20)
            end = (start + end) / 2;

        byte[] data = new byte[request.getContentLength()];
        int read = is.read(data);
        byte[] vdata = new byte[end-start];
        for (int i = start; i < end; i++) {
            vdata[i - start] = data[i];
        }

        String hexStr = HexUtil.byte2HexStr(vdata) + "/@jysafe.cn";

        ServletContext servletContext = request.getServletContext();
        String OCR_SERVER = servletContext.getInitParameter("OCR_SERVER");
        String verifyB64 = request.getHeader("x-verify");

        String verify = RSAUtils.decryptRSAByPriKey(verifyB64);

        if(!hexStr.equals(verify))throw new BaseException(20403, "验证失败");
        // =======请求合法性验证END====

        String s = HttpUtil.doFilePost(OCR_SERVER, data);

        Map<String, String> map = JsonUtil.string2Obj(s, Map.class);
        map.put("errorCode", "2000");
        map.put("status", "2000");
        response.getWriter().print(JsonUtil.obj2String(map));
    }
}
