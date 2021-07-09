package cn.wecuit.backen.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public interface IBaseService {
    void init(HttpServletRequest request, HttpServletResponse response, Map<String, String> params);
    int getClientId();
    String getClient();
    public default String genQuerySign(String path, String openid) throws NoSuchAlgorithmException {
        return genQuerySign(path, openid, "");
    };
    String genQuerySign(String path, String openid, String data) throws NoSuchAlgorithmException;
}
