考虑一个问题，外部的应用如何来访问内部各种各样的微服务呢？在微服务架构中，后端服务往往不直接开放给调用端，而是通过一个API网关根据请求的url，路由到相应的服务。当添加API网关后，在第三方调用端和服务提供方之间就创建了一面墙，这面墙直接与调用方通信进行权限控制，后将请求均衡分发给后台服务端。
## Spring Cloud Zuul

Spring Cloud Zuul路由是微服务架构的不可或缺的一部分，提供动态路由，监控，弹性，安全等的边缘服务。Zuul是Netflix出品的一个基于JVM路由和服务端的负载均衡器。

下面我们通过代码来了解Zuul是如何工作的

### 简单使用

1、添加依赖

```
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zuul</artifactId>
</dependency>

```

引入`spring-cloud-starter-zuul`包

2、配置文件

```
spring.application.name=gateway-service-zuul
server.port=8888

#这里的配置表示，访问/it/** 直接重定向到http://127.0.0.1:8080/**
zuul.routes.baidu.path=/it/**
zuul.routes.baidu.url=http://127.0.0.1:8080/

```

3、启动类

```
@EnableZuulProxy
@SpringCloudApplication
public class GatewayServiceZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceZuulApplication.class, args);
	}
}

```

启动类添加`@EnableZuulProxy`，支持网关路由。

史上最简单的zuul案例就配置完了

4、测试

启动`vicente-cloud-zuul`项目，在浏览器中访问：`http://localhost:8764/it/test`，看到页面返回了：`http://127.0.0.1:8080/test` 页面的信息，如下：

![image.png](https://upload-images.jianshu.io/upload_images/6738270-37f82038aee2bd17.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![image.png](https://upload-images.jianshu.io/upload_images/6738270-effb43286c68073d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

说明访问`vicente-cloud-zuul`的请求自动转发到了`vicente-boot`，并且将结果返回。

********************

#### 服务化
通过url映射的方式来实现zull的转发有局限性，比如每增加一个服务就需要配置一条内容，另外后端的服务如果是动态来提供，就不能采用这种方案来配置了。实际上在实现微服务架构时，服务名与服务实例地址的关系在eureka server中已经存在了，所以只需要将Zuul注册到eureka server上去发现其他服务，就可以实现对serviceId的映射。

我们结合示例来说明，在上面示例项目`vicente-cloud-zuul`的基础上来改造。

1、添加依赖
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```
增加spring-cloud-starter-eureka包，添加对eureka的支持。
2、配置文件

配置修改为：
```
eureka:
  client:
    serviceUrl:
      defaultZone: http://192.168.243.21:9001/eureka/
server:
  port: 8764
spring:
  application:
    name: vice-zuul-local
zuul:
  routes:
    client-hi:
      path: /hhhi/**
      serviceId: service-hi
    consumer:
      path: /bd-its/**
      serviceId: bd-its-deg-service
    baidu:
      path: /it/**
      url: http://192.168.243.21:9090/
```
启动完成后，zuul会自动注册发现的服务
![image.png](https://upload-images.jianshu.io/upload_images/6738270-d4a3d0d763432a64.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

3、测试

访问：`http://127.0.0.1:8764/it/itsdeg/select/style`,`http://127.0.0.1:8764/bd-its/itsdeg/select/style`,`http://127.0.0.1:8764/bd-its-deg-service/itsdeg/select/style`，
实际均是访问的是服务名为：`bd-its-deg-service`的项目，
url地址：`http://192.168.243.21:9090/itsdeg/select/style`
返回：
![image.png](https://upload-images.jianshu.io/upload_images/6738270-da49ebb2637deabe.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

说明访问/bd-its-deg-service的请求自动转发到了bd-its-deg-service项目，并且将结果返回。

为了更好的模拟服务集群，我们复制BD-ITS-DEG-SERVICE项目，启动2个同名的项目，修改方法，使两个同名项目返回的结果不一样
![image.png](https://upload-images.jianshu.io/upload_images/6738270-5a26525709642f1f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

修改完成后启动项目。测试多次访问`http://127.0.0.1:8764/bd-its-deg-service/itsdeg/select/style`，依次返回：
![image.png](https://upload-images.jianshu.io/upload_images/6738270-8c4fcb37fce1cd0b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![image.png](https://upload-images.jianshu.io/upload_images/6738270-6218fc9dea12d6a7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

说明通过zuul成功调用了BD-ITS-DEG-SERVICE服务并且做了均衡负载。

#####网关的默认路由规则

但是如果后端服务多达十几个的时候，每一个都这样配置也挺麻烦的，spring cloud zuul已经帮我们做了默认配置。默认情况下，Zuul会代理所有注册到Eureka Server的微服务，并且Zuul的路由规则如下：http://ZUUL_HOST:ZUUL_PORT/微服务在Eureka上的serviceId/**会被转发到serviceId对应的微服务。

我们注销掉gateway-service-zuul-eureka项目中关于路由的配置：
`zuul.routes.api-a.path=/producer/**`
`zuul.routes.api-a.serviceId=spring-cloud-producer`
重新启动后，访问http://localhost:8888/spring-cloud-producer/hello?name=%E5%B0%8F%E6%98%8E，测试返回结果和上述示例相同，说明Spring cloud zuul默认已经提供了转发功能。

到此zuul的基本使用我们就介绍完了。

上篇文章主要介绍了Zuul网关使用模式，以及自动转发机制，但其实Zuul还有更多的应用场景，比如：鉴权、流量转发、请求统计等等，这些功能都可以使用Zuul来实现。

## Zuul的核心

Filter是Zuul的核心，用来实现对外服务的控制。Filter的生命周期有4个，分别是“PRE”、“ROUTING”、“POST”、“ERROR”，整个生命周期可以用下图来表示。

![image](http://upload-images.jianshu.io/upload_images/6738270-338050393098eb56.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Zuul大部分功能都是通过过滤器来实现的，这些过滤器类型对应于请求的典型生命周期。

*   **PRE：** 这种过滤器在请求被路由之前调用。我们可利用这种过滤器实现身份验证、在集群中选择请求的微服务、记录调试信息等。
*   **ROUTING：**这种过滤器将请求路由到微服务。这种过滤器用于构建发送给微服务的请求，并使用Apache HttpClient或Netfilx Ribbon请求微服务。
*   **POST：**这种过滤器在路由到微服务以后执行。这种过滤器可用来为响应添加标准的HTTP Header、收集统计信息和指标、将响应从微服务发送给客户端等。
*   **ERROR：**在其他阶段发生错误时执行该过滤器。 除了默认的过滤器类型，Zuul还允许我们创建自定义的过滤器类型。例如，我们可以定制一种STATIC类型的过滤器，直接在Zuul中生成响应，而不将请求转发到后端的微服务。

### Zuul中默认实现的Filter

| 类型 | 顺序 | 过滤器 | 功能 |
| --- | --- | --- | --- |
| pre | -3 | ServletDetectionFilter | 标记处理Servlet的类型 |
| pre | -2 | Servlet30WrapperFilter | 包装HttpServletRequest请求 |
| pre | -1 | FormBodyWrapperFilter | 包装请求体 |
| route | 1 | DebugFilter | 标记调试标志 |
| route | 5 | PreDecorationFilter | 处理请求上下文供后续使用 |
| route | 10 | RibbonRoutingFilter | serviceId请求转发 |
| route | 100 | SimpleHostRoutingFilter | url请求转发 |
| route | 500 | SendForwardFilter | forward请求转发 |
| post | 0 | SendErrorFilter | 处理有错误的请求响应 |
| post | 1000 | SendResponseFilter | 处理正常的请求响应 |

**禁用指定的Filter**

可以在application.yml中配置需要禁用的filter，格式：

```
zuul:
	FormBodyWrapperFilter:
		pre:
			disable: true

```

## 自定义Filter

实现自定义Filter，需要继承ZuulFilter的类，并覆盖其中的4个方法。

```
public class MyFilter extends ZuulFilter {
    @Override
    String filterType() {
        return "pre"; //定义filter的类型，有pre、route、post、error四种
    }

    @Override
    int filterOrder() {
        return 10; //定义filter的顺序，数字越小表示顺序越高，越先执行
    }

    @Override
    boolean shouldFilter() {
        return true; //表示是否需要执行该filter，true表示执行，false表示不执行
    }

    @Override
    Object run() {
        return null; //filter需要执行的具体操作
    }
}

```

## 自定义Filter示例

我们假设有这样一个场景，因为服务网关应对的是外部的所有请求，为了避免产生安全隐患，我们需要对请求做一定的限制，比如请求中含有Token便让请求继续往下走，如果请求不带Token就直接返回并给出提示。

首先自定义一个Filter，在run()方法中验证参数是否含有Token。

```
public class TokenFilter extends ZuulFilter {

    private final Logger logger = LoggerFactory.getLogger(TokenFilter.class);

    @Override
    public String filterType() {
        return "pre"; // 可以在请求被路由之前调用
    }

    @Override
    public int filterOrder() {
        return 0; // filter执行顺序，通过数字指定 ,优先级为0，数字越大，优先级越低
    }

    @Override
    public boolean shouldFilter() {
        return true;// 是否执行该过滤器，此处为true，说明需要过滤
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        logger.info("--->>> TokenFilter {},{}", request.getMethod(), request.getRequestURL().toString());

        String token = request.getParameter("token");// 获取请求的参数

        if (StringUtils.isNotBlank(token)) {
            ctx.setSendZuulResponse(true); //对请求进行路由
            ctx.setResponseStatusCode(200);
            ctx.set("isSuccess", true);
            return null;
        } else {
            ctx.setSendZuulResponse(false); //不对其进行路由
            ctx.setResponseStatusCode(400);
            ctx.setResponseBody("token is empty");
            ctx.set("isSuccess", false);
            return null;
        }
    }

}

```

将TokenFilter加入到请求拦截队列，在启动类中添加以下代码：

```
@Bean
public TokenFilter tokenFilter() {
	return new TokenFilter();
}

```

这样就将我们自定义好的Filter加入到了请求拦截中。

**测试**

我们依次启动示例项目：`spring-cloud-eureka`、`spring-cloud-producer`、`spring-cloud-zuul`，这个三个项目均为上一篇示例项目，`spring-cloud-zuul`稍微进行改造。

访问地址：`http://localhost:8888/spring-cloud-producer/hello?name=neo`，返回：token is empty ，请求被拦截返回。
访问地址：`http://localhost:8888/spring-cloud-producer/hello?name=neo&token=xx`，返回：hello neo，this is first messge，说明请求正常响应。

通过上面这例子我们可以看出，我们可以使用“PRE”类型的Filter做很多的验证工作，在实际使用中我们可以结合shiro、oauth2.0等技术去做鉴权、验证。

## 路由熔断

当我们的后端服务出现异常的时候，我们不希望将异常抛出给最外层，期望服务可以自动进行一降级。Zuul给我们提供了这样的支持。当某个服务出现异常时，直接返回我们预设的信息。

我们通过自定义的fallback方法，并且将其指定给某个route来实现该route访问出问题的熔断处理。主要继承ZuulFallbackProvider接口来实现，ZuulFallbackProvider默认有两个方法，一个用来指明熔断拦截哪个服务，一个定制返回内容。

```
public interface ZuulFallbackProvider {
   /**
	 * The route this fallback will be used for.
	 * @return The route the fallback will be used for.
	 */
	public String getRoute();

	/**
	 * Provides a fallback response.
	 * @return The fallback response.
	 */
	public ClientHttpResponse fallbackResponse();
}

```

实现类通过实现getRoute方法，告诉Zuul它是负责哪个route定义的熔断。而fallbackResponse方法则是告诉 Zuul 断路出现时，它会提供一个什么返回值来处理请求。

后来Spring又扩展了此类，丰富了返回方式，在返回的内容中添加了异常信息，因此最新版本建议直接继承类`FallbackProvider` 。

我们以上面的spring-cloud-producer服务为例，定制它的熔断返回内容。

```
@Component
public class ProducerFallback implements FallbackProvider {
    private final Logger logger = LoggerFactory.getLogger(FallbackProvider.class);

    //指定要处理的 service。
    @Override
    public String getRoute() {
        return "spring-cloud-producer";
    }

    public ClientHttpResponse fallbackResponse() {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.OK;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return 200;
            }

            @Override
            public String getStatusText() throws IOException {
                return "OK";
            }

            @Override
            public void close() {

            }

            @Override
            public InputStream getBody() throws IOException {
                return new ByteArrayInputStream("The service is unavailable.".getBytes());
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }

    @Override
    public ClientHttpResponse fallbackResponse(Throwable cause) {
        if (cause != null && cause.getCause() != null) {
            String reason = cause.getCause().getMessage();
            logger.info("Excption {}",reason);
        }
        return fallbackResponse();
    }
}

```

当服务出现异常时，打印相关异常信息，并返回”The service is unavailable.”。

启动项目spring-cloud-producer-2，这时候服务中心会有两个spring-cloud-producer项目，我们重启Zuul项目。再手动关闭spring-cloud-producer-2项目，多次访问地址：`http://localhost:8888/spring-cloud-producer/hello?name=neo&token=xx`，会交替返回：

```
hello neo，this is first messge
The service is unavailable.
...

```

根据返回结果可以看出：spring-cloud-producer-2项目已经启用了熔断，返回:`The service is unavailable.`

> Zuul 目前只支持服务级别的熔断，不支持具体到某个URL进行熔断。

## 路由重试

有时候因为网络或者其它原因，服务可能会暂时的不可用，这个时候我们希望可以再次对服务进行重试，Zuul也帮我们实现了此功能，需要结合Spring Retry 一起来实现。下面我们以上面的项目为例做演示。

**添加Spring Retry依赖**

首先在spring-cloud-zuul项目中添加Spring Retry依赖。

```
<dependency>
	<groupId>org.springframework.retry</groupId>
	<artifactId>spring-retry</artifactId>
</dependency>

```

**开启Zuul Retry**

再配置文件中配置启用Zuul Retry

```
#是否开启重试功能
zuul.retryable=true
#对当前服务的重试次数
ribbon.MaxAutoRetries=2
#切换相同Server的次数
ribbon.MaxAutoRetriesNextServer=0

```

这样我们就开启了Zuul的重试功能。

**测试**

我们对spring-cloud-producer-2进行改造，在hello方法中添加定时，并且在请求的一开始打印参数。

```
@RequestMapping("/hello")
public String index(@RequestParam String name) {
    logger.info("request two name is "+name);
    try{
        Thread.sleep(1000000);
    }catch ( Exception e){
        logger.error(" hello two error",e);
    }
    return "hello "+name+"，this is two messge";
}

```

重启 spring-cloud-producer-2和spring-cloud-zuul项目。

访问地址：`http://localhost:8888/spring-cloud-producer/hello?name=neo&token=xx`，当页面返回：`The service is unavailable.`时查看项目spring-cloud-producer-2后台日志如下：

```
2018-01-22 19:50:32.401  INFO 19488 --- [io-9001-exec-14] o.s.c.n.z.f.route.FallbackProvider       : request two name is neo
2018-01-22 19:50:33.402  INFO 19488 --- [io-9001-exec-15] o.s.c.n.z.f.route.FallbackProvider       : request two name is neo
2018-01-22 19:50:34.404  INFO 19488 --- [io-9001-exec-16] o.s.c.n.z.f.route.FallbackProvider       : request two name is neo

```

说明进行了三次的请求，也就是进行了两次的重试。这样也就验证了我们的配置信息，完成了Zuul的重试功能。

**注意**

开启重试在某些情况下是有问题的，比如当压力过大，一个实例停止响应时，路由将流量转到另一个实例，很有可能导致最终所有的实例全被压垮。说到底，断路器的其中一个作用就是防止故障或者压力扩散。用了retry，断路器就只有在该服务的所有实例都无法运作的情况下才能起作用。这种时候，断路器的形式更像是提供一种友好的错误信息，或者假装服务正常运行的假象给使用者。

不用retry，仅使用负载均衡和熔断，就必须考虑到是否能够接受单个服务实例关闭和eureka刷新服务列表之间带来的短时间的熔断。如果可以接受，就无需使用retry。

##跨域问题处理
在项目中添加上ZuulFilter的文件
```
public class PreFilter extends ZuulFilter{
	private Logger LOG =Logger.getLogger(PreFilter.class);
	@Override
	public Object run() {
		// TODO Auto-generated method stub
//		 return checkToken();
		RequestContext ctx = RequestContext.getCurrentContext();
         HttpServletResponse response = ctx.getResponse();
         response.setHeader("Access-Control-Allow-Origin", "*");  
         response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");  
         response.setHeader("Access-Control-Max-Age", "3600");  
         response.setHeader("Access-Control-Allow-Headers", "x-requested-with");  
		ctx.setSendZuulResponse(true);// 对该请求进行路由  
        ctx.setResponseStatusCode(200);  
        ctx.set("isSuccess", true);// 设值，让下一个Filter看到上一个Filter的状态
        LOG.info(" PreFilter1 ok");
        return null;       
	    }

	@Override
	public boolean shouldFilter() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int filterOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String filterType() {
		// TODO Auto-generated method stub
		 return "pre";
	}

}
```
这样前端通过zuul访问服务器接口就可以跨域访问。
**还有个问题**：就是设置跨域访问只能设置一处，如果zuul调用的cloud服务也设置了跨域访问，这样就会报错，这个要怎么解决呢?可以在application配置文件中使用ZUUL配置忽略头部信息
```
zuul:
#需要忽略的头部信息，不再传播到其他服务
  sensitive-headers: Access-Control-Allow-Origin
  ignored-headers: Access-Control-Allow-Origin,H-APP-Id,Token,APPToken
```
##404页面处理
springboot2中可以通过ErrorPageRegistrar接口来实现错误跳转不同页面
自定义404和500等页面：
```
@Configuration
public class ErrorPageConfig {
 
    @Bean
    public ErrorPageRegistrar errorPageRegistrar() {
        return new ErrorPageRegistrar() {
            @Override
            public void registerErrorPages(ErrorPageRegistry registry) {
//              registry.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error404"),
                registry.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error404"),
                        new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error500"));
            }
        };
    }
 
}
```
ErrorPage中第一个参数是错误的状态，第二个参数是发生这个错误要跳转的链接，比如NOT_FOUND是404的错误代码，发生404的时候就会跳转到error404的链接，我们再配置一个controller，用于控制跳转错误页面
```
@Controller
public class ErrorController {

    @RequestMapping("/error404")
    public String error() {
        return "redirect:/bd-portal-view/";
    }

}
```
**第二种方法**
springboot2我们不需要自定义error的filter，最后都会进入到/error映射的方法里面。
    然后我们看/error，对应的文件是BasicErrorController，里面有两个方法：
errorHtml和error，看方法名就知道一个是返回html一个是返回json、xml对象的。他们是根据客户端请求是的accept进行选择具体返回内容的。这里面返回的参数可以这样进行配置,当然如果你想自己重新/error这个映射也是可以的，如下配置：
```
@Controller
public class GlobalErrorController implements ErrorController {
 
    @Override
    public String getErrorPath() {
        return "/error";
    }
     
    @ResponseBody
    @RequestMapping("/error")
    public String error() {
        return "自定义错误信息";
    }
 
}
```

## Zuul高可用

![image](http://upload-images.jianshu.io/upload_images/6738270-ff576d9b78d8b000.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

我们实际使用Zuul的方式如上图，不同的客户端使用不同的负载将请求分发到后端的Zuul，Zuul在通过Eureka调用后端服务，最后对外输出。因此为了保证Zuul的高可用性，前端可以同时启动多个Zuul实例进行负载，在Zuul的前端使用Nginx或者F5进行负载转发以达到高可用性。

# [Zuul的高可用](http://www.itmuch.com/spring-cloud/zuul/zuul-ha/)

Zuul的高可用非常关键，因为外部请求到后端微服务的流量都会经过Zuul。故而在生产环境中，我们一般都需要部署高可用的Zuul以避免单点故障。

笔者分两种场景讨论Zuul的高可用。

## Zuul客户端也注册到了Eureka Server上

这种情况下，Zuul的高可用非常简单，只需将多个Zuul节点注册到Eureka Server上，就可实现Zuul的高可用。此时，Zuul的高可用与其他微服务的高可用没什么区别。

![image](http://upload-images.jianshu.io/upload_images/6738270-68e7fd45cb4088e4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

                                                                                                    　　Zuul高可用架构图

当Zuul客户端也注册到Eureka Server上时，只需部署多个Zuul节点即可实现其高可用。Zuul客户端会自动从Eureka Server中查询Zuul Server的列表，并使用Ribbon负载均衡地请求Zuul集群。

## Zuul客户端未注册到Eureka Server上

现实中，这种场景往往更常见，例如，Zuul客户端是一个手机APP——我们不可能让所有的手机终端都注册到Eureka Server上。这种情况下，我们可借助一个额外的负载均衡器来实现Zuul的高可用，例如Nginx、HAProxy、F5等。

![image](http://upload-images.jianshu.io/upload_images/6738270-f5371416bc2363ab.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

　　　　　　　　　　　　　　　　　　　　　　　　　　　 Zuul高可用架构图

　　Zuul客户端将请求发送到负载均衡器，负载均衡器将请求转发到其代理的其中一个Zuul节点。这样，就可以实现Zuul的高可用。


**参考：**
[springcloud(十)：服务网关zuul初级篇](http://www.ityouknow.com/springcloud/2017/06/01/gateway-service-zuul.html) 
[springcloud(十一)：服务网关Zuul高级篇](http://www.ityouknow.com/springcloud/2018/01/20/spring-cloud-zuul.html)
[Zuul的高可用](http://www.itmuch.com/spring-cloud/zuul/zuul-ha/)
[zuul统一异常处理](https://www.acgist.com/article/489.html)
