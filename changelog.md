1. step1

输入 ```git checkout step1``` 即可前往

建立了 ```spring boot``` ```spring security``` 和 ```数据库```

2. step2

输入 ```git checkout step2``` 即可前往

完成了生成 token 的过程

顺便完成了登陆时生成 token 并返回 json 的接口

接口的地址在 LoginController 类，可以自行研究

3. step3

输入 ```git checkout step3``` 即可前往

完成了解析 token ，并从数据库中获取用户详细信息 userDetail 的功能

4. step4

输入 ```git checkout step4``` 即可前往

定义解析 token 的拦截器，该拦截器能够实现拦截用户请求并解析其中 token 所对应账户的权限并将权限写入本次会话中

5. step5

输入 ```git checkout step5``` 即可前往

注册步骤四的拦截器，使它在 Spring Security 读取本次会话权限前将用户所具有的权限写入本次会话中

6. step6

输入 ```git checkout step6``` 即可前往

完善 401 和 403 返回结果