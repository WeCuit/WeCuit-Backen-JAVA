package cn.wecuit.backen.service.api;

import cn.wecuit.backen.service.IBaseService;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public interface ITestService extends IBaseService {
    void testAction() throws IOException, ParseException, InterruptedException;
}
