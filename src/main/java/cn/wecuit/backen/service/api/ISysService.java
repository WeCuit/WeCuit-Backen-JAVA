package cn.wecuit.backen.service.api;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public interface ISysService {
    void getConfigAction() throws IOException;
    void getUserInfoV2Action() throws IOException, ParseException;
}
