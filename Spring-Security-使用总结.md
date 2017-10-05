---
title: Spring Security 使用总结
date: 2017-10-02 16:15:09
tags:
  - Spring Security
  - json web token
---

暑假的时候在学习了 Spring Security 并成功运用到了项目中。 在实践中摸索出了一套结合 json + jwt(json web token) + Spring Boot + Spring Security 技术的权限方案趁着国庆假期记录一下。

以下所有步骤的源码可以从我的 [github](https://github.com/wean2016/springsecurity) 上取得。如果要了解，请阅读 readme.md。

## 各个技术的简要介绍

### json : 与前端交互的数据交换格式  

个人理解上，它的特点是可以促进 web 前后端解耦，提升团队的工作效率。 同时也是跟安卓端和 iOS 端交互的工具，目前是没想出除了 json 和 XML 之外的交流形式诶（或许等以后有空闲时间会看看）。  

它的另一个特点是轻量级，简洁和清晰的层次可以方便我们阅读和编写，并且减少服务器带宽占用。

### jwt (json web token)

用人话讲就是将用户的身份信息（账号名字）、其他信息（不固定，根据需要增加）在用户登陆时提取出来，并且通过加密手段加工成一串密文，在用户登陆成功时带在返回结果发送给用户。以后用户每次请求时均带上这串密文，服务器根据解析这段密文判断用户是否有权限访问相关资源，并返回相应结果。  

从网上摘录了一些优点，关于 jwt 的更多资料感兴趣的读者可以自行谷歌：

1. 相比于session，它无需保存在服务器，不占用服务器内存开销。
2. 无状态、可拓展性强：比如有3台机器（A、B、C）组成服务器集群，若session存在机器A上，session只能保存在其中一台服务器，此时你便不能访问机器B、C，因为B、C上没有存放该Session，而使用token就能够验证用户请求合法性，并且我再加几台机器也没事，所以可拓展性好就是这个意思。
3. 由 2 知，这样做可就支持了跨域访问。

### Spring Boot

Spring Boot 是一个用来简化 Spring 应用的搭建以及开发过程的框架。用完后会让你大呼 : "wocao! 怎么有这么方便的东西! mama 再也不用担心我不会配置 xml 配置文件了!"。

### Spring Security

这是 Spring Security 提供的一个安全权限控制框架，可以根据使用者的需要定制相关的角色身份和身份所具有的权限，完成黑名单操作、拦截无权限的操作。配合 Spring Boot 可以快速开发出一套完善的权限系统。  

## 本次技术方案中 Spring Security 执行流程

![本次技术方案中 Spring Security 执行流程](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E6%9C%AC%E6%AC%A1%E6%8A%80%E6%9C%AF%E6%96%B9%E6%A1%88%E4%B8%AD%20Spring%20Security%20%E6%89%A7%E8%A1%8C%E6%B5%81%E7%A8%8B.png)

从图中可以看出本次执行流程围绕着的就是 **token**。  

用户通过登陆操作获得我们返回的 token 并保存在本地。在以后每次请求都在请求头中带上 token ，服务器在收到客户端传来的请求时会判断是否有 token ，若有，解析 token 并写入权限到本次会话，若无直接跳过解析 token 的步骤，然后判断本次访问的接口是否需要认证，是否需要相应的权限，并根据本次会话中的认证情况做出反应。

## 动手实现这个安全框架

### 步骤一 ： 建立项目，配置好数据源

1. 使用 Itellij Idea 建立一个 Spring Boot 项目

![需要的组件](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E9%9C%80%E8%A6%81%E7%9A%84%E7%BB%84%E4%BB%B6.png)

选择 Web 、Security 、 Mybatis 和 JDBC 四个组件。

2. 在数据库中建立所需的数据库 spring_security  

![建立数据库](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E5%BB%BA%E7%AB%8B%E6%95%B0%E6%8D%AE%E5%BA%93.png)

3. 在 spring boot 配置文件 application.properties 中配置好数据源

![配置数据源](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E9%85%8D%E7%BD%AE%E6%95%B0%E6%8D%AE%E6%BA%90.png)

4. 启动项目查看 Spring Boot 是否替我们配置好 Spring Security 了。

![启动项目](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E5%90%AF%E5%8A%A8%E9%A1%B9%E7%9B%AE1.png)

若是正确启动了，可以看到 Spring Security 生成了一段默认密码。

我们访问 ```localhost:8080``` 会弹出一个 basic 认证框  

输入 用户名 `user` 密码 `前面自动生成的密码` 便可得到通过的返回消息（返回 404，因为我们还未建立任何页面）  
输入 错误的用户名或者密码会返回 401 ，提示未认证

如果你走到了这一步，意味着你已经配置好了所需要的环境，接下来就跟着进入下一步吧！

### 步骤二 ： 生成我们的 jwt

在这一步我们将学习如何根据我们的需要生成我们定制的 token ！

1. 关闭 Spring Boot 替我们配置好的 Spring Security。（因为默认配置好的 Spring Security 会拦截掉我们定制的登陆接口）

创建 Spring Security 配置类 ```WebSecurityConfig.java```
```
@Configuration      // 声明为配置类
@EnableWebSecurity      // 启用 Spring Security web 安全的功能
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().permitAll()       // 允许所有请求通过
                .and()
                .csrf()
                .disable()                      // 禁用 Spring Security 自带的跨域处理
                .sessionManagement()                        // 定制我们自己的 session 策略
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 调整为让 Spring Security 不创建和使用 session
    }
}
```

2. 在数据库中建立相应的用户和角色。

创建用户表 ```user```  
![user 表](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/user%20%E8%A1%A8.png)

其中各个属性和作用如下:

- username : 用户名
- password : 密码
- role_id : 用户所属角色编号
- last_password_change : 最后一次密码修改时间
- enable : 是否启用该账号，可以用来做黑名单

创建角色表 ```role```  
![role 表](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E8%A7%92%E8%89%B2%E6%9D%83%E9%99%90%E8%A1%A8.png)

其中各个属性作用如下:

- role_id : 角色相应 id
- role_name : 角色的名称
- auth : 角色所拥有的权限

3. 编写相应的登陆密码判断逻辑

因为登陆功能很容易实现，这里就不写出来占地方了哎。

4. 编写 token 操作类（生成 token 部分）

因为网上有造好的轮子，我们可以直接拿来做些修改就可以使用了。

使用 maven 导入网上造好的 jwt 轮子  
```
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.4</version>
</dependency>
```
建立我们自己的 token 操作类 ```TokenUtils.java```
```
public class TokenUtils {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Value("${token.secret}")
    private String secret;

    @Value("${token.expiration}")
    private Long expiration;

    /**
     * 根据 TokenDetail 生成 Token
     *
     * @param tokenDetail
     * @return
     */
    public String generateToken(TokenDetail tokenDetail) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("sub", tokenDetail.getUsername());
        claims.put("created", this.generateCurrentDate());
        return this.generateToken(claims);
    }

    /**
     * 根据 claims 生成 Token
     *
     * @param claims
     * @return
     */
    private String generateToken(Map<String, Object> claims) {
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(this.generateExpirationDate())
                    .signWith(SignatureAlgorithm.HS512, this.secret.getBytes("UTF-8"))
                    .compact();
        } catch (UnsupportedEncodingException ex) {
            //didn't want to have this method throw the exception, would rather log it and sign the token like it was before
            logger.warn(ex.getMessage());
            return Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(this.generateExpirationDate())
                    .signWith(SignatureAlgorithm.HS512, this.secret)
                    .compact();
        }
    }

    /**
     * token 过期时间
     *
     * @return
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + this.expiration * 1000);
    }

    /**
     * 获得当前时间
     *
     * @return
     */
    private Date generateCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

}
```

这个工具类的目前做的事情是 :

- 把用户名封装进下载的轮子的 token 的主体 **claims** 中，并在里面封装了当前时间（方便后面判断 token 是否在修改密码之前生成的）
- 再计算 token 过期的时间写入到 轮子的 token 中
- 对 轮子的 token 进行撒盐加密，生成一串字符串，即我们定制的 token

生成定制 token 的方法的入参 ```TokenDetail``` 的定义如下  
```
public interface TokenDetail {

    //TODO: 这里封装了一层，不直接使用 username 做参数的原因是可以方便未来增加其他要封装到 token 中的信息

    String getUsername();
}

public class TokenDetailImpl implements TokenDetail {

    private final String username;

    public TokenDetailImpl(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}
```

同时这个工具类把加密 token 撒盐的字符串和 token 的过期时间提取到了 application.properties 中  
```
# token 加密密钥
token.secret=secret
# token 过期时间，以秒为单位，604800 是 一星期
token.expiration=604800
```

5. 至此，我们生成 token 的教程已经完成，至于登陆接口，判断账号密码是否正确的操作就留给读者去实现，读者只需在登陆成功时在结果中返回生成好的 token 给用户即可。

### 步骤三 : 实现验证 token 是否有效，并根据 token 获得账号详细信息（权限，是否处于封号状态）的功能

1. 分析实现的过程

在步骤二中，我们把用户的的 username 、 token 创建的时间 、 token 过期的时间封装到了加密过后的 token 字符串中，就是为了服务此时我们验证用户权限的目的。

假设我们此时拿到了用户传递过来的一串 token，并且要根据这串 token 获得用户的详情可以这样做：

A. 尝试解析这串 token ，若成功解析出来，进入下一步，否则终止解析过程
B. 根据解析出来的 username 从数据库中查找用户的账号，最后一次密码修改的时间，权限，是否封号等用户详情信息，把这些信息封装到一个实体类中（userDetail类)。若查找不到该用户，终止解析进程
C. 检查 userDetail 中记录的封号状态，若是账号已被封号，返回封号结果，终止请求
D. 根据 userDtail 比较 token 是否处于有效期内，若不处于有效期内，终止解析过程，否则继续
E. 将 userDetail 中记录的用户权限写入本次请求会话中，解析完成。

可参考下图理解：

![分析解析 token ，检查权限的过程](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E5%88%86%E6%9E%90%E8%A7%A3%E6%9E%90%20token%20%EF%BC%8C%E6%A3%80%E6%9F%A5%E6%9D%83%E9%99%90%E7%9A%84%E8%BF%87%E7%A8%8B.png)

下面开始动手实现

2. 尝试解析 token 获得 username

```
/**
     * 从 token 中拿到 username
     *
     * @param token
     * @return
     */
    public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 解析 token 的主体 Claims
     *
     * @param token
     * @return
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(this.secret.getBytes("UTF-8"))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }
```

在这段代码中，我们先对 token 进行解密，获得 token 中封装好的主体部分 claims (前面第二部引入的 别人造好的轮子)，然后尝试获得里面封装的 username 字符串。

3. 从数据库中获得用户详情 userDetail

这里我们将实现 Spring Security 的一个 UserDetailService 接口，这个接口只有一个方法, loadUserByUsername。流程图如下

![获得 UserDetail 的流程图](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E8%8E%B7%E5%BE%97%20UserDetail%20%E7%9A%84%E6%B5%81%E7%A8%8B%E5%9B%BE.png)

代码如下:

```
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取 userDetail
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = this.userMapper.getUserFromDatabase(username);

        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return SecurityModelFactory.create(user);
        }
    }
}

public class User implements LoginDetail, TokenDetail {

    private String username;
    private String password;
    private String authorities;
    private Long lastPasswordChange;
    private char enable;

    // 省略构造器和 getter setter 方法
}

public class SecurityModelFactory {

    public static UserDetailImpl create(User user) {
        Collection<? extends GrantedAuthority> authorities;
        try {
            authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getAuthorities());
        } catch (Exception e) {
            authorities = null;
        }

        Date lastPasswordReset = new Date();
        lastPasswordReset.setTime(user.getLastPasswordChange());
        return new UserDetailImpl(
                user.getUsername(),
                user.getUsername(),
                user.getPassword(),
                lastPasswordReset,
                authorities,
                user.enable()
        );
    }

}
```

其中获得未处理过的用户详细信息 User 类的 mapper 类定义如下:
```
public interface UserMapper {


    User getUserFromDatabase(@Param("username") String username);

}
```

相应的 xml 文件为 :

```
<select id="getUserFromDatabase"  resultMap="getUserFromDatabaseMap">
        SELECT
        `user`.username,
        `user`.`password`,
        `user`.role_id,
        `user`.enable,
        `user`.last_password_change,
        `user`.enable,
        role.auth
        FROM
        `user` ,
        role
        WHERE
        `user`.role_id = role.role_id AND
        `user`.username = #{username}
    </select>

    <resultMap id="getUserFromDatabaseMap" type="cn.ssd.wean2016.springsecurity.model.domain.User">
        <id column="username" property="username"/>
        <result column="password" property="password"/>
        <result column="last_password_change" property="lastPasswordChange"/>
        <result column="auth" property="authorities"/>
        <result column="enable" property="enable"/>
    </resultMap>
```

至此，我们已经完成获取用户详细信息的的功能了。接下来只要限制接口的访问权限，并要求用户访问接口时带上 token 即可实现对权限的控制。

### 步骤四 : 定义解析 token 的拦截器

老规矩，上流程图:

![解析 token 的拦截器的执行流程](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E8%A7%A3%E6%9E%90%20token%20%E7%9A%84%E6%8B%A6%E6%88%AA%E5%99%A8%E7%9A%84%E6%89%A7%E8%A1%8C%E6%B5%81%E7%A8%8B.png)

下面定义这个拦截器

```
public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * json web token 在请求头的名字
     */
    @Value("${token.header}")
    private String tokenHeader;

    /**
     * 辅助操作 token 的工具类
     */
    @Autowired
    private TokenUtils tokenUtils;

    /**
     * Spring Security 的核心操作服务类
     * 在当前类中将使用 UserDetailsService 来获取 userDetails 对象
     */
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 将 ServletRequest 转换为 HttpServletRequest 才能拿到请求头中的 token
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // 尝试获取请求头的 token
        String authToken = httpRequest.getHeader(this.tokenHeader);
        // 尝试拿 token 中的 username
        // 若是没有 token 或者拿 username 时出现异常，那么 username 为 null
        String username = this.tokenUtils.getUsernameFromToken(authToken);

        // 如果上面解析 token 成功并且拿到了 username 并且本次会话的权限还未被写入
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 用 UserDetailsService 从数据库中拿到用户的 UserDetails 类
            // UserDetails 类是 Spring Security 用于保存用户权限的实体类
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            // 检查用户带来的 token 是否有效
            // 包括 token 和 userDetails 中用户名是否一样， token 是否过期， token 生成时间是否在最后一次密码修改时间之前
            // 若是检查通过
            if (this.tokenUtils.validateToken(authToken, userDetails)) {
                // 生成通过认证
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                // 将权限写入本次会话
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            if (!userDetails.isEnabled()){
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().print("{\"code\":\"452\",\"data\":\"\",\"message\":\"账号处于黑名单\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

}
```

其中检查 token 是否有效的 ```tokenUtils.validateToken(authToken, userDetails) ``` 方法定义如下:

```
/**
     * 检查 token 是否处于有效期内
     * @param token
     * @param userDetails
     * @return
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        UserDetailImpl user = (UserDetailImpl) userDetails;
        final String username = this.getUsernameFromToken(token);
        final Date created = this.getCreatedDateFromToken(token);
        return (username.equals(user.getUsername()) && !(this.isTokenExpired(token)) && !(this.isCreatedBeforeLastPasswordReset(created, user.getLastPasswordReset())));
    }

    /**
     * 获得我们封装在 token 中的 token 创建时间
     * @param token
     * @return
     */
    public Date getCreatedDateFromToken(String token) {
        Date created;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            created = new Date((Long) claims.get("created"));
        } catch (Exception e) {
            created = null;
        }
        return created;
    }

    /**
     * 获得我们封装在 token 中的 token 过期时间
     * @param token
     * @return
     */
    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }

    /**
     * 检查当前时间是否在封装在 token 中的过期时间之后，若是，则判定为 token 过期
     * @param token
     * @return
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = this.getExpirationDateFromToken(token);
        return expiration.before(this.generateCurrentDate());
    }

    /**
     * 检查 token 是否是在最后一次修改密码之前创建的（账号修改密码之后之前生成的 token 即使没过期也判断为无效）
     * @param created
     * @param lastPasswordReset
     * @return
     */
    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }
```

### 步骤五 : 注册步骤四的拦截器，使它在 Spring Security 读取本次会话权限前将用户所具有的权限写入本次会话中

在 SpringSecurity 的配置类 ```WebSecurityConfig.java``` 中添加如下配置

```
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 注册 token 转换拦截器为 bean
     * 如果客户端传来了 token ，那么通过拦截器解析 token 赋予用户权限
     *
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
        AuthenticationTokenFilter authenticationTokenFilter = new AuthenticationTokenFilter();
        authenticationTokenFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationTokenFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/auth").authenticated()       // 需携带有效 token
                .antMatchers("/admin").hasAuthority("admin")   // 需拥有 admin 这个权限
                .antMatchers("/ADMIN").hasRole("ADMIN")     // 需拥有 ADMIN 这个身份
                .anyRequest().permitAll()       // 允许所有请求通过
                .and()
                .csrf()
                .disable()                      // 禁用 Spring Security 自带的跨域处理
                .sessionManagement()                        // 定制我们自己的 session 策略
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 调整为让 Spring Security 不创建和使用 session


        /**
         * 本次 json web token 权限控制的核心配置部分
         * 在 Spring Security 开始判断本次会话是否有权限时的前一瞬间
         * 通过添加过滤器将 token 解析，将用户所有的权限写入本次 Spring Security 的会话
         */
        http
                .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
    }
}
```

其中我们将步骤四中定义的拦截器注册到 Spring 中成为一个 bean ，并登记在 Spring Security 开始判断本次会话是否有权限时的前一瞬间通过添加过滤器将 token 解析，将用户所有的权限写入本次会话。

其次，我们添加了三个 ant 风格的地址拦截规则 :

- /auth : 要求携带有效的 token
- /admin : 要求携带 token 所对应的账号具有 admin 这个权限
- /ADMIN : 要求携带 token 对应的张账号具有 ROLE_ADMIN 这个身份

启动程序到 8080 端口，通过 /login 接口登陆 guest 账号，对 ```/auth``` 接口尝试访问，结果如下 :

![对 auth 的访问结果](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E5%AF%B9%20auth%20%E7%9A%84%E8%AE%BF%E9%97%AE%E7%BB%93%E6%9E%9C.png)

显然，因为 token 有效，所以成功通过了拦截

接下来尝试访问 ```/admin``` 接口，结果如下 :

![对 admin 的访问结果](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E5%AF%B9%20admin%20%E7%9A%84%E8%AE%BF%E9%97%AE%E7%BB%93%E6%9E%9C.png)

显然，因为携带的 token 不具有 admin 这个权限，所以请求被拦截拦截

至此，我们已经完成了一套权限简单的权限规则系统，在下一步中，我们将对无权限访问的返回结果进行优化，并结束这次总结。

## 步骤六 : 完善 401 和 403 返回结果

定义 401 处理器，实现 ```AuthenticationEntryPoint``` 接口  
```
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
```

定义 403 处理器，实现 ```AccessDeniedHandler``` 接口
```
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
        //返回json形式的错误信息
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json");

        httpServletResponse.getWriter().println("{\"code\":403,\"message\":\"小弟弟，你没有权限访问呀！\",\"data\":\"\"}");
        httpServletResponse.getWriter().flush();
    }
}
```

将这两个处理器配置到 SpringSecurity 的配置类中 :

```
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 注册 401 处理器
     */
    @Autowired
    private EntryPointUnauthorizedHandler unauthorizedHandler;

    /**
     * 注册 403 处理器
     */
    @Autowired
    private MyAccessDeniedHandler accessDeniedHandler;

    ...

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                ...

                // 配置被拦截时的处理
                .exceptionHandling()
                .authenticationEntryPoint(this.unauthorizedHandler)   // 添加 token 无效或者没有携带 token 时的处理
                .accessDeniedHandler(this.accessDeniedHandler)      //添加无权限时的处理

                ...
    }
}
```

尝试以 guest 的身份访问 /admin 接口，结果如下:

![以 guest 身份访问 admin 接口](http://wean-blog.oss-cn-shenzhen.aliyuncs.com/%E4%BB%A5%20guest%20%E8%BA%AB%E4%BB%BD%E8%AE%BF%E9%97%AE%20admin%20%E6%8E%A5%E5%8F%A3.png)

嘻嘻，显然任务完成啦！！！（这个接口也可以用 lamda 表达式配置，这个留给大家去探索啦~~~）

溜了溜了……
