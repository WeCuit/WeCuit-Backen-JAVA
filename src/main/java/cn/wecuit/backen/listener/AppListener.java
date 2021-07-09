package cn.wecuit.backen.listener;

import cn.wecuit.backen.utils.TaskUtil;
import cn.wecuit.mybatis.entity.MyBatis;
import cn.wecuit.backen.utils.RSAUtils;
import cn.wecuit.backen.utils.RobotUtil;
import cn.wecuit.backen.utils.TencentUtil;
import cn.wecuit.robot.provider.WSeg;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.quartz.SchedulerException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.InputStream;


@WebListener
@Slf4j
public class AppListener implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {

    public AppListener() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("contextInitialized");
        ServletContext sc = sce.getServletContext();

        // 初始化小程序配置
        TencentUtil.initAppid(sc.getInitParameter("WX_APPID"), sc.getInitParameter("QQ_APPID"));
        TencentUtil.initSecret(sc.getInitParameter("WX_SECRET"), sc.getInitParameter("QQ_SECRET"));

        WSeg.setToken(sc.getInitParameter("HANLP_TOKEN"));

        // 初始化RSA配置
        try {
            RSAUtils.init(sc.getInitParameter("RSA_PRI_KEY"), sc.getInitParameter("RSA_PUB_KEY"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 初始化数据目录，以及其它信息
        /* This method is called when the servlet context is initialized(when the Web application is deployed). */

        /**
         * servletContext.getResource("/").getPath() ---- /D:/Project/WeCuit/target/WeCuit-1.0-SNAPSHOT/
         * servletContext.getRealPath("/")           ---- D:\Project\WeCuit\target\WeCuit-1.0-SNAPSHOT\
         * System.getProperty("java.io.tmpdir")      ---- D:\tomcat\apache-tomcat-9.0.43\temp
         */
        final String tempPath = System.getProperty("java.io.tmpdir") + "/WeCuit";
        final String CACHE_PATH = tempPath + "/cache";
        final String LOG_PATH = tempPath + "/log";
        final String SESSION_PATH = tempPath + "/session";
        sc.setInitParameter("CACHE_PATH", CACHE_PATH);
        sc.setInitParameter("LOG_PATH", LOG_PATH);
        sc.setInitParameter("SESSION_PATH", SESSION_PATH);

        // 初始化MyBatis
        try {
            InputStream inputStream = this.getClass().getResource("/mybatis/mybatis-config.xml").openStream();
            MyBatis.setSqlSessionFactory(new SqlSessionFactoryBuilder().build(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 机器人初始化
        String qqid = sc.getInitParameter("QQID");
        String qqpass = sc.getInitParameter("QQPASS");
        // if(qqid!=null && qqpass!=null)
        //     RobotUtil.init(Long.parseLong(qqid), qqpass);

        try {
            TaskUtil.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        /* This method is called when the servlet Context is undeployed or Application Server shuts down. */

        // 关闭机器人
        RobotUtil.stop();
        try {
            TaskUtil.stop();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        /* Session is created. */
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        /* Session is destroyed. */
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute is added to a session. */
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute is removed from a session. */
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute is replaced in a session. */
    }
}
