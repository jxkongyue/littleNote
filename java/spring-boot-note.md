## spring-boot 笔记
[TOC]
### 静态资源
```
@Controller
public class HelloController {

    @RequestMapping("/hello/{name}")
    public String hello(@PathVariable("name") String name, Model model) {
        model.addAttribute("name", name);
        return "hello"
    }
}
```
在上述例子中，返回值`hello`并非直接将字符串返回给浏览器，而是寻找名字为`hello`的模板进行渲染，需要在文件夹`src/main/resources/templates/`目录下添加一个模板文件`hello.html`

#### java模板引擎Thymeleaf
https://www.tianmaying.com/tutorial/using-thymeleaf
不破坏html原有结构，支持动态生效  


### Rest接口
`@RestController`
```
@RequestMapping("/posts/{id}")
public String post(@PathVariable("id") int id) {
    return String.format("post %d", id);
}
```

### 配置项
global.properties
```
email=test@mkyong.com
thread-pool=12
```
使用@ConfigurationProperties：
```
import org.springframework.boot.context.properties.ConfigurationProperties;

@Component
@PropertySource("classpath:global.properties")
@ConfigurationProperties
public class GlobalProperties {

    private int threadPool;
    private String email;

    //getters and setters

}
```
比`@Value`注解简单，同时支持.properties和.yaml文件；
同时，@ConfigurationProperties支持JSR-303 bean校验
```java
@Component
@ConfigurationProperties
public class GlobalProperties {

    @Max(5)
    @Min(0)
    private int threadPool;

    @NotEmpty
    private String email;

    //getters and setters
}
```
### spring-boot starter
可以认为starter是一种服务——使得使用某个功能的开发者不需要关注各种依赖库的处理，不需要具体的配置信息，由Spring Boot自动通过classpath路径下的类发现需要的Bean，并织入bean。举个例子，spring-boot-starter-jdbc这个starter的存在，使得我们只需要在`BookPubApplication`下用`@Autowired`引入`DataSource`的bean就可以，Spring Boot会自动创建`DataSource`的实例.

### spring-boot Actuator端点
http://blog.didispace.com/spring-boot-actuator-1/
原生端点：
应用配置类：获取应用程序中加载的应用配置、环境变量、自动化配置报告等与Spring Boot应用密切相关的配置类信息。
度量指标类：获取应用程序运行过程中用于监控的度量指标，比如：内存信息、线程池信息、HTTP请求统计等。
操作控制类：提供了对应用的关闭等操作类功能。

### spring-boot cli
Spring Boot是一个命令行工具，用于使用Spring进行快速原型搭建。它允许你运行Groovy脚本。
GVM（Groovy环境管理器）可以用来管理多种不同版本的Groovy和Java二进制包。
http://www.cnblogs.com/smile361/p/4710595.html
Grape依赖管理器:内嵌在Groovy里的Jar包依赖管理器。Grape让你可以快速添加maven仓库依赖到你的classpath里，使脚本运行更加简单。
`/health` 监控检查
`/beans` 该端点用来获取应用上下文中创建的所有Bean
`logger`调整日志级别
### 其他
@Autowired和@Inject基本是一样的，因为两者都是使用AutowiredAnnotationBeanPostProcessor来处理依赖注入。但是@Resource是个例外，它使用的是CommonAnnotationBeanPostProcessor来处理依赖注入。当然，两者都是BeanPostProcessor。

@Service用于标注业务层组件
@Controller用于标注控制层组件（如struts中的action）
@Repository用于标注数据访问组件，即DAO组件
@Component泛指组件，当组件不好归类的时候，我们可以使用这个注解进行标注。
component-scan标签默认情况下自动扫描指定路径下的包（含所有子包），将带有@Component、@Repository、@Service、@Controller标签的类自动注册到spring容器.
> Written with [StackEdit](https://stackedit.io/).
