package cn.ssd.wean2016.springsecurity.model.vo;

import javax.validation.constraints.NotNull;

/**
 * @version V1.0.0
 * @Description 用户登陆接口参数的实体类
 * @Author liuyuequn weanyq@gmail.com
 * @Date 2017/10/3 1:29
 */
public class RequestLoginUser {

    @NotNull
    private String username;

    @NotNull
    private String password;

    public RequestLoginUser() {
    }

    public String getUsername() {
        return username;
    }

    public RequestLoginUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RequestLoginUser setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString() {
        return "RequestLoginUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
