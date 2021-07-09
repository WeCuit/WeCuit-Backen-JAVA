package cn.wecuit.backen.service.api;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

/**
 * @Author jiyec
 * @Date 2021/4/29 18:30
 * @Version 1.0
 **/
public interface IJszxService {
    // 获取打卡列表
    void getCheckInListV2Action() throws IOException, ParseException;
    // 计算中心登录
    void loginRSAv1Action() throws Exception;
    // 获取打卡内容
    void getCheckInEditV2Action() throws IOException, ParseException;
    // 提交打卡
    void doCheckInV3Action() throws IOException, ParseException;

    void office_prepareAction() throws IOException, ParseException;
    void office_getCaptchaAction() throws IOException, ParseException;
    void office_queryAction() throws IOException, ParseException;
}
