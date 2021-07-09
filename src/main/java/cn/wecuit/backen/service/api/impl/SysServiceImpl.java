package cn.wecuit.backen.service.api.impl;

import cn.wecuit.backen.service.api.ISysService;
import cn.wecuit.backen.utils.FileUtil;
import cn.wecuit.backen.utils.JsonUtil;
import cn.wecuit.backen.utils.TencentUtil;
import cn.wecuit.backen.service.impl.BaseServiceImpl;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SysServiceImpl extends BaseServiceImpl implements ISysService {
    // TODO: 临时勉强用状态，需结合数据库了~
    @Override
    public void getConfigAction() throws IOException {
        response.getWriter().print(FileUtil.ReadFile(this.getClass().getResource("/config/config.json").getFile()));
    }

    /**
     * 获取用户信息
     * openid | 是否管理员[暂废]
     * @throws IOException
     */
    @Override
    public void getUserInfoV2Action() throws IOException, ParseException {
        String code = request.getParameter("code");

        Map<String, Object> session = TencentUtil.code2session(code, getClientId());

        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("errorCode", 2000);
            put("info", new HashMap<String, Object>(){{
                put("isAdmin", false);
                put("openid", session.get("openid"));
            }});
        }}));
    }
}
