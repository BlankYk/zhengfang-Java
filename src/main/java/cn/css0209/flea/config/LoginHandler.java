package cn.css0209.flea.config;

import cn.hutool.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

public class LoginHandler implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setContentType("application/json; charset=utf-8");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = null;
        HttpSession session = request.getSession();
        //检查用户是否登录
        if(session.getAttribute("name") != null){
            //已登录
            return true;
        }else{
            //未登录
            //设置状态码
            response.setStatus(200);
            //获取ip
            Logger log = LoggerFactory.getLogger(this.getClass());
            log.info("访问被拦截");
            //装Json格式输出
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result","fail");
            jsonObject.put("msg","未登录");
            jsonObject.put("msg2", "访问被拦截");
            out = response.getWriter();
            out.append(jsonObject.toString());
        }
        return false;
    }
}
