package cn.wecuit.backen.service.api;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @Author jiyec
 * @Date 2021/5/10 23:16
 * @Version 1.0
 **/
public interface ISubService {
    void getTemplateIdListAction() throws IOException;
    void getStatusV2Action() throws NoSuchAlgorithmException, IOException;
    void changeStatusV2Action() throws Exception;
    void deleteV2Action() throws NoSuchAlgorithmException, IOException;
    void addCntV2Action() throws NoSuchAlgorithmException, IOException;
}
