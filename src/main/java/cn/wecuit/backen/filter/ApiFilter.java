package cn.wecuit.backen.filter;

import javax.servlet.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebFilter(
        filterName = "ApiFilter",
        urlPatterns = {
                "/Api/*"
        }
)
public class ApiFilter implements Filter {
    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 设置响应请求编码
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        // 禁用SESSION（TODO: 非jsp页面似乎不必要？待确认。）
        // HttpServletRequest HttpRequest = new HttpSessionForbidden((HttpServletRequest) request);

        chain.doFilter(request, response);  // 放行
    }
}
