package cn.wecuit.backen.service.api.impl;

import cn.wecuit.backen.service.api.ITestService;
import cn.wecuit.backen.service.impl.BaseServiceImpl;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

/**
 * 测试类
 * 用于检测服务是否能正常启动
 *
 * @author jiyec
 */
public class TestServiceImpl extends BaseServiceImpl implements ITestService {

    /**
     * 方法中不能含参，否则无法被调用
     *
     */
    @Override
    public void testAction() throws IOException, ParseException, InterruptedException {
        super.params.forEach((k, v)-> System.out.println(k));
        System.out.println("test");
        this.response.getWriter().print("成功！");
    }

}
