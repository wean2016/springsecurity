package cn.ssd.wean2016.springsecurity;

import cn.ssd.wean2016.springsecurity.dao.UserMapper;
import cn.ssd.wean2016.springsecurity.model.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringsecurityApplicationTests {

	@Autowired
    private UserMapper userMapper;

	@Test
    public void testGetUser(){
        User user = userMapper.getUserFromDatabase("1");
        System.out.println(user);
    }

}
