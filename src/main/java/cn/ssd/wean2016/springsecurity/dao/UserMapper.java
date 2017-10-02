package cn.ssd.wean2016.springsecurity.dao;

import cn.ssd.wean2016.springsecurity.model.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * @version V1.0.0
 * @Description
 * @Author liuyuequn weanyq@gmail.com
 * @Date 2017/10/3 1:50
 */
@Mapper
@Component
public interface UserMapper {


    User getUserFromDatabase(@Param("username") String username);

}
