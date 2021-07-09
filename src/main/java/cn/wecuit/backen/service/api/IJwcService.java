package cn.wecuit.backen.service.api;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

/**
 * @Author jiyec
 * @Date 2021/5/1 19:17
 * @Version 1.0
 **/
public interface IJwcService {
    void labAllAction() throws IOException, ParseException;
    void labDetailAction() throws IOException, ParseException;
}
