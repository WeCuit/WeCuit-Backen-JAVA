package cn.wecuit.backen.service.api.impl;

import cn.wecuit.backen.dao.SubMapper;
import cn.wecuit.backen.dao.UserMapper;
import cn.wecuit.backen.utils.RSAUtils;
import cn.wecuit.mybatis.entity.MyBatis;
import cn.wecuit.backen.exception.BaseException;
import cn.wecuit.backen.service.api.ISubService;
import cn.wecuit.backen.service.impl.BaseServiceImpl;
import cn.wecuit.backen.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订阅处理器
 *
 * @Author jiyec
 * @Date 2021/5/10 23:17
 * @Version 1.0
 **/
@Slf4j
public class SubServiceImpl extends BaseServiceImpl implements ISubService {

    @Override
    public void getTemplateIdListAction() throws IOException {
        int clientId = getClientId();
        if(-1 == clientId) throw new BaseException(403, "不支持的客户端");
        try (SqlSession sqlSession = MyBatis.getSqlSessionFactory().openSession()) {
            List<HashMap<String, Object>> list = sqlSession.selectList("cn.wecuit.backen.sub.selectTplList", client[clientId]);

            response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>() {{
                put("status", 2000);
                put("errorCode", 2000);
                put("data", list);
            }}));
        }

    }

    @Override
    public void getStatusV2Action() throws NoSuchAlgorithmException, IOException {

        String openid = request.getParameter("openid");
        String sign = request.getParameter("sign");

        String path = (request.getServletPath() + ((null == request.getPathInfo())?"":request.getPathInfo()) + "/").substring(4);
        String s = genQuerySign(path, openid);

        if(sign == null || !sign.equals(s))throw new BaseException(403, "非法请求");

        String client = getClient();
        try (SqlSession sqlSession = MyBatis.getSqlSessionFactory().openSession()) {

            List<HashMap<String, Object>> list = sqlSession.selectList("cn.wecuit.backen.sub.subStatus",
                    new HashMap<String, String>(){{
                        put("client", client);
                        put("openid", openid);
                    }}
            );
            response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>() {{
                put("status", 2000);
                put("errorCode", 2000);
                put("sub", list);
            }}));
        }

    }

    @Override
    public void changeStatusV2Action() throws Exception {
        String openid = request.getParameter("openid");
        String status = request.getParameter("status");
        String tplId = request.getParameter("tplId");
        String sign = request.getParameter("sign");
        String userId = request.getParameter("userId");
        String userPass = request.getParameter("userPass");

        String client = getClient();

        String path = (request.getServletPath() + ((null == request.getPathInfo())?"":request.getPathInfo()) + "/").substring(4);
        String s = genQuerySign(path, openid, tplId);

        if(sign == null || !sign.equals(s))throw new BaseException(403, "非法请求");
        
        String pass = RSAUtils.decryptRSAByPriKey(userPass);

        if(userId.isEmpty() || pass.isEmpty())throw new BaseException(10401, "账号密码缺失");

        boolean sub_enable = false;
        if("true".equals(status))sub_enable=true;

        try(SqlSession sqlSession = MyBatis.getSqlSessionFactory().openSession()){
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            SubMapper subMapper = sqlSession.getMapper(SubMapper.class);

            BigInteger uid = userMapper.queryUIdBySId(userId);
            // 用户账号数据处理
            if(uid != null){
                // 旧用户
                int i = userMapper.updateStuInfoByUId(uid, userId, userPass);
                if(i != 1)throw new BaseException(10500, "更新用户失败");
            }else{
                // 新用户
                Map<String, BigInteger> uid_t = new HashMap<>();
                int i = userMapper.addUser(uid_t, getClient(), openid, userId, userPass);
                uid = uid_t.get("uid");
                if(i!=1 || uid == null)throw new BaseException(10500, "新增用户失败");

            }

            // 订阅信息处理
            int i = subMapper.setEnable(sub_enable, uid, tplId);
            if(i == 0){
                // 影响0行，可能不存在
                int i1 = subMapper.insertSub(uid, tplId);

                if(i1 != 1)throw new BaseException(10500, "订阅信息更新失败");
            }

            sqlSession.commit();

        }

        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("status", 2000);
            put("errorCode", 2000);
            put("errMsg", "已更新");
        }}));

    }

    // TODO: QQ WX分开删
    @Override
    public void deleteV2Action() throws NoSuchAlgorithmException, IOException {
        String openid = request.getParameter("openid");
        String sign = request.getParameter("sign");

        String path = (request.getServletPath() + ((null == request.getPathInfo())?"":request.getPathInfo()) + "/").substring(4);
        String s = genQuerySign(path, openid);

        if(sign == null || !sign.equals(s))throw new BaseException(403, "非法请求");

        try(SqlSession sqlSession = MyBatis.getSqlSessionFactory().openSession()){
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            SubMapper subMapper = sqlSession.getMapper(SubMapper.class);

            BigInteger uid = userMapper.queryUIdByOpenId(getClient(), openid);
            int delete = subMapper.deleteSub(uid);

            if(delete==0)throw new BaseException(10500, "删除失败");

            sqlSession.commit();
        }

        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("status", 2000);
            put("errorCode", 2000);
            put("errMsg", "已删除");
        }}));
    }

    @Override
    public void addCntV2Action() throws NoSuchAlgorithmException, IOException {
        String openid = request.getParameter("openid");
        String tplId = request.getParameter("tplId");
        String sign = request.getParameter("sign");

        String path = (request.getServletPath() + ((null == request.getPathInfo())?"":request.getPathInfo()) + "/").substring(4);
        String s = genQuerySign(path, openid, tplId);
        if(sign == null || !sign.equals(s))throw new BaseException(403, "非法请求");

        try(SqlSession sqlSession = MyBatis.getSqlSessionFactory().openSession()){
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            SubMapper subMapper = sqlSession.getMapper(SubMapper.class);

            BigInteger uid = userMapper.queryUIdByOpenId(getClient(), openid);
            int i = subMapper.incrCnt(uid, tplId);
            if(i != 1)throw new BaseException(10500, "操作失败");

            sqlSession.commit();
        }

        response.getWriter().print(JsonUtil.obj2String(new HashMap<String, Object>(){{
            put("status", 2000);
            put("errorCode", 2000);
            put("errMsg", "+1");
        }}));
    }

}
