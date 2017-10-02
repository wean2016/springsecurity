package cn.ssd.wean2016.springsecurity.service;

import cn.ssd.wean2016.springsecurity.model.LoginDetail;
import cn.ssd.wean2016.springsecurity.model.TokenDetail;

/**
 * @version V1.0.0
 * @Description
 * @Author liuyuequn weanyq@gmail.com
 * @Date 2017/10/3 2:11
 */
public interface LoginService {

    LoginDetail getLoginDetail(String username);

    String generateToken(TokenDetail tokenDetail);

}
