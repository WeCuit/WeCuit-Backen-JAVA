package cn.wecuit.backen.service.api;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

/**
 * @Author jiyec
 * @Date 2021/5/15 15:59
 * @Version 1.0
 **/
public interface TheolService {
    void courseListAction() throws IOException, ParseException;
    void loginAction() throws IOException, ParseException;
    void dirTreeAction() throws IOException, ParseException;
    void folderListAction() throws IOException, ParseException;
    void downloadFileAction() throws IOException, ParseException;
}
