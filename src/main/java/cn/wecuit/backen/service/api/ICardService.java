package cn.wecuit.backen.service.api;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @Author jiyec
 * @Date 2021/5/2 15:18
 * @Version 1.0
 **/
public interface ICardService {
    void loginAction() throws IOException, ParseException;
    void getAccWalletAction() throws IOException, NoSuchAlgorithmException, ParseException;
    void getDealRecAction() throws NoSuchAlgorithmException, IOException, ParseException;
}
