package cn.wecuit.backen.service.impl;

import cn.wecuit.backen.exception.BaseException;
import cn.wecuit.backen.service.IBaseService;
import cn.wecuit.backen.utils.HexUtil;
import cn.wecuit.backen.utils.RSAUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class BaseServiceImpl implements IBaseService {
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected Map<String, String> params;
    protected final String[] client = {"wx", "qq"};

    @Override
    public final void init(HttpServletRequest request, HttpServletResponse response, Map<String, String> params){
        this.request = request;
        this.response = response;
        this.params = params;
    }

    /**
     *
     * @return 0 wx | 1 qq
     */
    @Override
    public final int getClientId() {

        String referer = request.getHeader("referer");
        if(null == referer)throw new BaseException(20500, "请求异常");
        if(referer.contains("servicewechat.com"))return 0;
        else if(referer.contains("appservice.qq.com"))return 1;
        else
            throw new BaseException(20403, "不支持的客户端");
    }

    @Override
    public String getClient() {
        return client[getClientId()];
    }

    @Override
    public String genQuerySign(String path, String openid, String data) throws NoSuchAlgorithmException {
        byte[] pathData = RSAUtils.genMD5(path.getBytes(StandardCharsets.UTF_8));
        byte[] openidData = RSAUtils.genMD5(openid.getBytes(StandardCharsets.UTF_8));
        byte[] dataData = RSAUtils.genMD5(data.getBytes(StandardCharsets.UTF_8));
        String query_salt = request.getServletContext().getInitParameter("QUERY_SALT");

        String s = HexUtil.byte2HexStr(pathData) + HexUtil.byte2HexStr(openidData) + HexUtil.byte2HexStr(dataData) + query_salt;
        byte[] bytes = RSAUtils.genMD5(s.getBytes(StandardCharsets.UTF_8));

        return HexUtil.byte2HexStr(bytes);
    }

}
