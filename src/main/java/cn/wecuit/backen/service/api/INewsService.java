package cn.wecuit.backen.service.api;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface INewsService {
    void doPullAction();
    void getTagsV2Action() throws IOException;
    void getListAction() throws IOException;
    void getContentAction() throws IOException, ParseException, NoSuchAlgorithmException;
}
