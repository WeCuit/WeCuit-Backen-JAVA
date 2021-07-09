package cn.wecuit.backen.servlet;

import cn.wecuit.backen.exception.BaseException;
import cn.wecuit.backen.service.impl.BaseServiceImpl;
import cn.wecuit.backen.utils.ApiUtil;
import cn.wecuit.backen.utils.JsonUtil;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 拦截所有API请求，进行路由处理
 *
 */
@WebServlet(name = "ApiServlet", value = "/Api/*")
public class ApiServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public ApiServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pkg = null;
        Map<String, Object> ja = new HashMap<>();
        boolean statusOK = false;

        // 拦截API请求处理过程中产生的异常，确保客户端收到的数据格式为JSON。
        try {
            String uri = request.getServletPath() + ((null == request.getPathInfo())?"":request.getPathInfo());
            System.out.println(uri);

            // generate route
            List<Map<String, String>> parsedURI = ApiUtil.parseURI(uri);
            if(null == parsedURI) throw new RuntimeException("URI解析失败");

            Map<String, String> params = parsedURI.get(1);

            Map<String, String> route = parsedURI.get(0);

            // 服务包
            String basePkg = "cn.wecuit.backen.service";
            // 构造试图请求的类
            pkg = basePkg + "." + route.get("subPkg") + ".impl." + route.get("service") + "ServiceImpl";

            // 从这里开始使用反射来自动创建类，以及调用方法
            // transfer method by reflection
            // 尝试加载请求的类
            Class<? extends BaseServiceImpl> clazz = (Class<? extends BaseServiceImpl>) Class.forName(pkg);
            // 尝试获取 请求的类中 含有map参数的构造方法
            Constructor<?> construct = clazz.getConstructor();
            // 创建实例对象
            Object obj = construct.newInstance();

            // 尝试获取 初始化方法
            Method method = clazz.getMethod("init", HttpServletRequest.class, HttpServletResponse.class, Map.class);
            // 尝试调用 初始化方法
            method.invoke(obj, request, response, params);

            // 尝试获取 指定 "无参" 方法
            method = clazz.getMethod(route.get("action") + "Action");
            // 尝试调用 指定无参方法
            method.invoke(obj);

            statusOK = true;
        } catch (ClassNotFoundException e) {
            // TODO: errCode
            e.printStackTrace();

            ja.put("errCode", 10404);
            ja.put("errMsg", pkg + " - 未找到指定类");

        } catch (NoSuchMethodException e){

            ja.put("errCode", 10404);
            ja.put("errMsg", e.getLocalizedMessage() + "方法未找到");
        }catch (InvocationTargetException e){

            // 方式二
            Throwable cause = e.getCause();
            if (cause instanceof BaseException) {
                BaseException cause1 = (BaseException) cause;

                ja.put("errorCode", cause1.getCode());
                ja.put("errMsg", cause1.getMessage());
            }else{
                e.printStackTrace();
                ja.put("errCode", 10500);
                ja.put("errMsg", e.getCause().getMessage() + "未知异常");
            }

        }catch (Exception e) {
            e.printStackTrace();

            ja.put("errCode", 10500);
            ja.put("errMsg", e.getMessage() + "未知异常");
        }finally {
            if(!statusOK)response.getWriter().print(JsonUtil.obj2String(ja));
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
