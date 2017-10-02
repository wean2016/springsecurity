package cn.ssd.wean2016.springsecurity.model;

/**
 * @version V1.0.0
 * @Description
 * @Author liuyuequn weanyq@gmail.com
 * @Date 2017/10/3 1:44
 */
public interface LoginDetail {

    String getUsername();
    String getPassword();
    boolean enable();
}
