package cn.ssd.wean2016.springsecurity.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @version
 * @Description
 * @Author liuyuequn weanyq@gmail.com
 * @Date 2017年8月11日10:59:57
 */
@Component
public class EntryPointUnauthorizedHandler implements AuthenticationEntryPoint {

  /**
   * 未登录或无权限时触发的操作
   * 返回  {"code":401,"message":"小弟弟，你没有携带 token 或者 token 无效！","data":""}
   * @param httpServletRequest
   * @param httpServletResponse
   * @param e
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
      //返回json形式的错误信息
      httpServletResponse.setCharacterEncoding("UTF-8");
      httpServletResponse.setContentType("application/json");

      httpServletResponse.getWriter().println("{\"code\":401,\"message\":\"小弟弟，你没有携带 token 或者 token 无效！\",\"data\":\"\"}");
      httpServletResponse.getWriter().flush();
  }

}
