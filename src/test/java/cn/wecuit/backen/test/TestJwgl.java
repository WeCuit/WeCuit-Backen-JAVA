package cn.wecuit.backen.test;

import cn.wecuit.backen.utils.FileUtil;
import cn.wecuit.backen.utils.JwglUtil;
import org.junit.Test;

/**
 * @Author jiyec
 * @Date 2021/6/5 18:01
 * @Version 1.0
 **/
public class TestJwgl {
    @Test
    public void testA(){
        String file = this.getClass().getResource("/temp.html").getFile();
        String s = FileUtil.ReadFile(file);
        JwglUtil.courseHandle(s);
    }
}
