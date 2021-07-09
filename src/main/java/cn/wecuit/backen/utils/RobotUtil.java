package cn.wecuit.backen.utils;

import cn.wecuit.robot.RobotMain;
import lombok.extern.slf4j.Slf4j;

/**
 * 机器人启动关闭控制
 *
 * @Author jiyec
 * @Date 2021/5/5 9:45
 * @Version 1.0
 **/
@Slf4j
public class RobotUtil {

    public static Long id = 0L;
    private static String pass = null;

    public static void init(Long id, String pass){
        RobotUtil.id = id;
        RobotUtil.pass = pass;
    }

    public static void start(){
        if(id != 0L && pass != null)
            RobotMain.init(id, pass);
        else
            log.info("机器人账号数据似乎未初始化，无法启动机器人！");
    }

    public static void stop(){
        RobotMain.logout();
    }

}