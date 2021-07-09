package cn.wecuit.robot.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author jiyec
 * @Date 2021/6/10 15:05
 * @Version 1.0
 **/
public class B {
    public static void init(){
        List<String> list = new ArrayList<>();
        list.add("cn.wecuit.robot.test.D");
        list.forEach(s->{
            try {
                Class<?> aClass = Class.forName(s);
                D d = (D) aClass.newInstance();
                d.E();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }
}
