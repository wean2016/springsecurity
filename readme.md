spring security 学习总结
=======


## 关于

`spring security 学习总结` 暑假的时候在学习了 Spring Security 并成功运用到了项目中。 在实践中摸索出了一套结合 json + jwt(json web token) + Spring Boot + Spring Security 技术的权限方案趁着国庆假期记录一下。


## 内容

1. 生成 jwt
2. 解析 jwt
3. 根据 1，2 实现对访问路径的权限拦截

## 使用

`spring security 学习总结`是逐步进行构建的，里程碑版本我都使用了git tag来管理。例如，最开始的tag是`step1`，那么可以使用

	git checkout step1
	
来获得这一版本。版本历史见[`changelog.md`](https://github.com/wean2016/springsecurity/blob/master/changelog.md)。


