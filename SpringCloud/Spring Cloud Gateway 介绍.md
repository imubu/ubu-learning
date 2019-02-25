Spring 官方最终还是按捺不住推出了自己的网关组件：Spring Cloud Gateway ，相比之前我们使用的 Zuul（1.x） 它有哪些优势呢？Zuul（1.x） 基于 Servlet，使用阻塞 API，它不支持任何长连接，如 WebSockets，Spring Cloud Gateway 使用非阻塞 API，支持 WebSockets，支持限流等新特性。

## Spring Cloud Gateway

Spring Cloud Gateway 是 Spring Cloud 的一个全新项目，该项目是基于 Spring 5.0，Spring Boot 2.0  等技术开发的网关，它旨在为微服务架构提供一种简单有效的统一的 API 路由管理方式。

Spring Cloud Gateway 作为 Spring Cloud 生态系统中的网关，目标是替代 Netflix Zuul，其不仅提供统一的路由方式，并且基于 Filter 链的方式提供了网关基本的功能，例如：安全，监控/指标，和限流。

**相关概念:**

- Route（路由）：这是网关的基本构建块。它由一个 ID，一个目标 URI，一组断言和一组过滤器定义。如果断言为真，则路由匹配。
- Predicate（断言）：这是一个 Java 8 的 Predicate。输入类型是一个 ServerWebExchange。我们可以使用它来匹配来自 HTTP 请求的任何内容，例如 headers 或参数。
- Filter（过滤器）：这是`org.springframework.cloud.gateway.filter.GatewayFilter`的实例，我们可以使用它修改请求和响应。

**工作流程：**

![img](https://github.com/imubu/ubu-learning/blob/master/SpringCloud/spring_cloud_gateway_diagram.png)

客户端向 Spring Cloud Gateway 发出请求。如果 Gateway Handler Mapping 中找到与请求相匹配的路由，将其发送到 Gateway Web Handler。Handler 再通过指定的过滤器链来将请求发送到我们实际的服务执行业务逻辑，然后返回。 过滤器之间用虚线分开是因为过滤器可能会在发送代理请求之前（“pre”）或之后（“post”）执行业务逻辑。

Spring Cloud Gateway 的特征：

- 基于 Spring Framework 5，Project Reactor 和 Spring Boot 2.0
- 动态路由
- Predicates 和 Filters 作用于特定路由
- 集成 Hystrix 断路器
- 集成 Spring Cloud DiscoveryClient
- 易于编写的 Predicates 和 Filters
- 限流
- 路径重写

## 快速上手

Spring Cloud Gateway 网关路由有两种配置方式：

- 在配置文件 yml 中配置
- 通过`@Bean`自定义 RouteLocator，在启动主类 Application 中配置

这两种方式是等价的，建议使用 yml 方式进配置。

使用 Spring Cloud Finchley 版本，Finchley 版本依赖于 Spring Boot 2.0.6.RELEASE。

```
<parent>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-parent</artifactId>
	<version>2.0.6.RELEASE</version>
	<relativePath/> <!-- lookup parent from repository -->
</parent>

<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-dependencies</artifactId>
			<version>Finchley.SR2</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>
```

添加项目需要使用的依赖包

```
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

Spring Cloud Gateway 是使用 netty+webflux 实现因此不需要再引入 web 模块。

我们先来测试一个最简单的请求转发。

```
server:
  port: 8080
spring:
  cloud:
    gateway:
      routes:
      - id: neo_route
        uri: http://192.168.243.21:9980
        predicates:
        - Path=/user/getUserList
```

各字段含义如下：

- id：我们自定义的路由 ID，保持唯一
- uri：目标服务地址
- predicates：路由条件，Predicate 接受一个输入参数，返回一个布尔值结果。该接口包含多种默认方法来将 Predicate 组合成其他复杂的逻辑（比如：与，或，非）。
- filters：过滤规则，本示例暂时没用。

上面这段配置的意思是，配置了一个 id 为 neo_route 的路由规则，当访问地址 `http://localhost:8080/user/getUserList`时会自动转发到地址：`http://192.168.243.21:9980/user/getUserList`。配置完成启动项目即可在浏览器访问进行测试，当我们访问地址`http://localhost:8080/user/getUserList` 时会看看展示页面如何：



方法二：

转发功能同样可以通过代码来实现，我们可以在启动类 GateWayApplication 中添加方法 `customRouteLocator()` 来定制转发规则。

```
@SpringBootApplication
public class GateWayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GateWayApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("path_route", r -> r.path("/demo/error")
						.uri("http://192.168.243.21:9980/demo/error"))
				.build();
	}

}
```

上面配置了一个 id 为 path_route 的路由，当访问地址`http://localhost:8080/demo/error`时会自动转发到地址：`http://192.168.243.21:9980/demo/error`和上面的转发效果一样。

以上便是 Spring Cloud Gateway 最简单的两个请求示例，Spring Cloud Gateway 还有更多实用的功能接下来我们一一介绍。

## 路由规则

Spring Cloud Gateway 的功能很强大，我们仅仅通过 Predicates 的设计就可以看出来，前面我们只是使用了 predicates 进行了简单的条件匹配，其实 Spring Cloud Gataway 帮我们内置了很多 Predicates 功能。

Spring Cloud Gateway 是通过 Spring WebFlux 的 `HandlerMapping` 做为底层支持来匹配到转发路由，Spring Cloud Gateway 内置了很多 Predicates 工厂，这些 Predicates 工厂通过不同的 HTTP 请求参数来匹配，多个 Predicates 工厂可以组合使用。

### Predicate 介绍

Predicate 来源于 Java 8，是 Java 8 中引入的一个函数，Predicate 接受一个输入参数，返回一个布尔值结果。该接口包含多种默认方法来将 Predicate 组合成其他复杂的逻辑（比如：与，或，非）。可以用于接口请求参数校验、判断新老数据是否有变化需要进行更新操作。

在 Spring Cloud Gateway 中 Spring 利用 Predicate 的特性实现了各种路由匹配规则，有通过 Header、请求参数等不同的条件来进行作为条件匹配到对应的路由。网上有一张图总结了 Spring Cloud 内置的几种 Predicate 的实现。

![spring_cloud_gateway_predicate](https://github.com/imubu/ubu-learning/blob/master/SpringCloud/spring_cloud_gateway_predicate.png)

说白了 Predicate 就是为了实现一组匹配规则，方便让请求过来找到对应的 Route 进行处理，接下来我们接下 Spring Cloud GateWay 内置几种 Predicate 的使用。

### 通过时间匹配

Predicate 支持设置一个时间，在请求进行转发的时候，可以通过判断在这个时间之前或者之后进行转发。比如我们现在设置只有在2019年1月1日才会转发到我的网站，在这之前不进行转发，我就可以这样配置：

```
spring:
  cloud:
    gateway:
      routes:
       - id: time_route
        uri: http://192.168.243.21:9980
        predicates:
         - After=2019-03-20T06:06:06+08:00[Asia/Shanghai]
```

Spring 是通过 ZonedDateTime 来对时间进行的对比，ZonedDateTime 是 Java 8 中日期时间功能里，用于表示带时区的日期与时间信息的类，ZonedDateTime 支持通过时区来设置时间，中国的时区是：`Asia/Shanghai`。

After Route Predicate 是指在这个时间之后的请求都转发到目标地址。上面的示例是指，请求时间在 2018年1月20日6点6分6秒之后的所有请求都转发到地址`http://192.168.243.21:9980`。`+08:00`是指时间和UTC时间相差八个小时，时间地区为`Asia/Shanghai`。

添加完路由规则之后，访问地址`http://localhost:8080`会自动转发到`http://192.168.243.21:9980`。

Before Route Predicate 刚好相反，在某个时间之前的请求的请求都进行转发。我们把上面路由规则中的 After 改为 Before，如下：

```
spring:
  cloud:
    gateway:
      routes:
       - id: after_route
        uri: http://192.168.243.21:9980
        predicates:
         - Before=2018-01-20T06:06:06+08:00[Asia/Shanghai]
```

就表示在这个时间之前可以进行路由，在这时间之后停止路由，修改完之后重启项目再次访问地址`http://localhost:8080`，页面会报 404 没有找到地址。

除过在时间之前或者之后外，Gateway 还支持限制路由请求在某一个时间段范围内，可以使用 Between Route Predicate 来实现。

```
spring:
  cloud:
    gateway:
      routes:
       - id: after_route
        uri: http://192.168.243.21:9980
        predicates:
         - Between=2018-01-20T06:06:06+08:00[Asia/Shanghai], 2019-01-20T06:06:06+08:00[Asia/Shanghai]
```

这样设置就意味着在这个时间段内可以匹配到此路由，超过这个时间段范围则不会进行匹配。通过时间匹配路由的功能很酷，可以用在限时抢购的一些场景中。

### 



### 通过请求方式匹配

可以通过是 POST、GET、PUT、DELETE 等不同的请求方式来进行路由。

```
spring:
  cloud:
    gateway:
      routes:
      - id: method_route
        uri: http://192.168.243.21:9980/
        predicates:
        - Method=GET
```

使用 curl 测试，命令行输入:

```
# curl 默认是以 GET 的方式去请求
curl http://localhost:8080
```

测试返回页面代码，证明匹配到路由，我们再以 POST 的方式请求测试。

```
# curl 默认是以 GET 的方式去请求
curl -X POST http://localhost:8080
```

返回 404 没有找到，证明没有匹配上路由



### 通过请求参数匹配

Query Route Predicate 支持传入两个参数，一个是属性名一个为属性值，属性值可以是正则表达式。

```
spring:
  cloud:
    gateway:
      routes:
      - id: query_route
        uri: http://192.168.243.21:9980
        predicates:
        - Query=smile
```

这样配置，只要请求中包含 smile 属性的参数即可匹配路由。

使用 curl 测试，命令行输入:

```
curl localhost:8080?smile=x&id=2
```

经过测试发现只要请求汇总带有 smile 参数即会匹配路由，不带 smile 参数则不会匹配。

还可以将 Query 的值以键值对的方式进行配置，这样在请求过来时会对属性值和正则进行匹配，匹配上才会走路由。

```
spring:
  cloud:
    gateway:
      routes:
      - id: query_route
        uri: http://192.168.243.21:9980
        predicates:
        - Query=keep, pu.
```

这样只要当请求中包含 keep 属性并且参数值是以 pu 开头的长度为三位的字符串才会进行匹配和路由。

使用 curl 测试，命令行输入:

```
curl localhost:8080?keep=pub
```

测试可以返回页面代码，将 keep 的属性值改为 pubx 再次访问就会报 404,证明路由需要匹配正则表达式才会进行路由。

### 通过请求 ip 地址进行匹配

Predicate 也支持通过设置某个 ip 区间号段的请求才会路由，RemoteAddr Route Predicate 接受 cidr 符号(IPv4 或 IPv6 )字符串的列表(最小大小为1)，例如 192.168.0.1/16 (其中 192.168.0.1 是 IP 地址，16 是子网掩码)。

```
spring:
  cloud:
    gateway:
      routes:
      - id: remoteaddr_route
        uri: http://192.168.243.21:9980
        predicates:
        - RemoteAddr=192.168.1.1/24
```

可以将此地址设置为本机的 ip 地址进行测试。

```
curl localhost:8080
```

果请求的远程地址是 192.168.1.10，则此路由将匹配。



还有通过 Cookie 匹配，通过 Header 属性匹配，通过 Host 匹配这里就不介绍了。

### 组合使用

上面为了演示各个 Predicate 的使用，我们是单个单个进行配置测试，其实可以将各种 Predicate 组合起来一起使用。

例如：

```
spring:
  cloud:
    gateway:
      routes:
       - id: host_foo_path_headers_to_httpbin
        uri: http://192.168.243.21:9980
        predicates:
        - Host=**.foo.org
        - Path=/headers
        - Method=GET
        - Header=X-Request-Id, \d+
        - Query=foo, ba.
        - Query=baz
        - Cookie=chocolate, ch.p
        - After=2018-01-20T06:06:06+08:00[Asia/Shanghai]
```

各种 Predicates 同时存在于同一个路由时，请求必须同时满足所有的条件才被这个路由匹配。

> 一个请求满足多个路由的谓词条件时，请求只会被首个成功匹配的路由转发

## 注册中心

在实际的工作中，服务的相互调用都是依赖于服务中心提供的入口来使用，服务中心往往注册了很多服务，如果每个服务都需要单独配置的话，这将是一份很枯燥的工作。Spring Cloud Gateway 提供了一种默认转发的能力，只要将 Spring Cloud Gateway 注册到服务中心，Spring Cloud Gateway 默认就会代理服务中心的所有服务，下面用代码演示。

### 准备服务和注册中心

需将项目版本升级到 `Finchley.SR2` 。

spring-cloud-eureka(Eureka Server) 的 pom 文件更改，其它依赖包不变。

升级前：

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka-server</artifactId>
</dependency>
```

升级后：

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

spring-cloud-producer(Eureka Client)的 pom 文件更改。因为配置中心需要作为服务注册到注册中心，所以需要升级 Eureka Client，其他依赖没有变动。

升级前：

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```

升级后：

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

两个项目升级完依赖包后依次重启，访问注册中心地址 `http://localhost:8000/` 即可看到名为 `SPRING-CLOUD-PRODUCER`的服务。

### 服务网关注册到注册中心

复制上一节的示例项目 [cloud-gateway](http://www.ityouknow.com/springcloud/2018/12/12/spring-cloud-gateway-start.html) 重新命名为 cloud-gateway-eureka，添加 eureka 的客户端依赖包。

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

修改 application.yml 配置文件内容如下

```
server:
  port: 8888
spring:
  application:
    name: cloud-gateway-eureka
  cloud:
    gateway:
     discovery:
        locator:
         enabled: true
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8000/eureka/
logging:
  level:
    org.springframework.cloud.gateway: debug
```

配置说明：

- `spring.cloud.gateway.discovery.locator.enabled`：是否与服务注册于发现组件进行结合，通过 serviceId 转发到具体的服务实例。默认为 false，设为 true 便开启通过服务中心的自动根据 serviceId 创建路由的功能。
- `eureka.client.service-url.defaultZone`指定注册中心的地址，以便使用服务发现功能
- `logging.level.org.springframework.cloud.gateway` 调整相 gateway 包的 log 级别，以便排查问题

修改完成后启动 cloud-gateway-eureka 项目，访问注册中心地址 `http://localhost:8000/` 即可看到名为 `CLOUD-GATEWAY-EUREKA`的服务。

### 测试

将 Spring Cloud Gateway 注册到服务中心之后，网关会自动代理所有的在注册中心的服务，访问这些服务的语法为：

```
http://网关地址：端口/服务中心注册 serviceId/具体的url
```

比如我们的 spring-cloud-producer 项目有一个 `/hello` 的服务，访问此服务的时候会返回：hello world。

比如访问地址：`http://localhost:9000/hello`，页面返回：hello world!

按照上面的语法我们通过网关来访问，浏览器输入：`http://localhost:8888/SPRING-CLOUD-PRODUCER/hello` 同样返回：hello world!证明服务网关转发成功。

我们将项目 spring-cloud-producer 复制一份为 spring-cloud-producer-1，将`/hello`服务的返回值修改为 hello world smile !，修改端口号为 9001 ，修完完成后重启，这时候访问注册中心后台会发现有两个名为 `SPRING-CLOUD-PRODUCER`的服务。

在浏览器多次访问地址：`http://localhost:8888/SPRING-CLOUD-PRODUCER/hello`，页面交替返回以下信息：

```
hello world!
hello world smile!
```

说明后端服务自动进行了均衡负载。

## 基于 Filter(过滤器) 实现的高级功能

Spring Cloud Gateway 的 Filter 的生命周期不像 Zuul 的那么丰富，它只有两个：“pre” 和 “post”。

- **PRE**： 这种过滤器在请求被路由之前调用。我们可利用这种过滤器实现身份验证、在集群中选择请求的微服务、记录调试信息等。
- **POST**：这种过滤器在路由到微服务以后执行。这种过滤器可用来为响应添加标准的 HTTP Header、收集统计信息和指标、将响应从微服务发送给客户端等。

Spring Cloud Gateway 的 Filter 分为两种：GatewayFilter 与 GlobalFilter。GlobalFilter 会应用到所有的路由上，而 GatewayFilter 将应用到单个路由或者一个分组的路由上。

Spring Cloud Gateway 内置了9种 GlobalFilter，比如 Netty Routing Filter、LoadBalancerClient Filter、Websocket Routing Filter 等，根据名字即可猜测出这些 Filter 的作用

利用 GatewayFilter 可以修改请求的 Http 的请求或者响应，或者根据请求或者响应做一些特殊的限制。 更多时候我们会利用 GatewayFilter 做一些具体的路由配置，下面我们做一些简单的介绍。

### 快速上手 Filter 使用

我们以 AddRequestParameter GatewayFilter 来演示一下，如何在项目中使用 GatewayFilter，AddRequestParameter GatewayFilter 可以在请求中添加指定参数。

**application.yml配置示例**

```
spring:
  cloud:
    gateway:
      routes:
      - id: add_request_parameter_route
        uri: http://example.org
        filters:
        - AddRequestParameter=foo, bar
```

这样就会给匹配的每个请求添加上`foo=bar`的参数和值。

我们将以上配置融入到 cloud-gateway-eureka 项目中，完整的 `application.yml` 文件配置信息如下：

```
server:
  port: 8888
spring:
  application:
    name: cloud-gateway-eureka
  cloud:
    gateway:
     discovery:
        locator:
         enabled: true
     routes:
     - id: add_request_parameter_route
       uri: http://localhost:9000
       filters:
       - AddRequestParameter=foo, bar
       predicates:
         - Method=GET
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8000/eureka/
logging:
  level:
    org.springframework.cloud.gateway: debug
```

这里的 routes 手动指定了服务的转发地址，设置所有的 GET 方法都会自动添加`foo=bar`，`http://localhost:9000` 是 spring-cloud-producer 项目，我们在此项目中添加一个 foo() 方法，用来接收转发中添加的参数 foo。

```
@RequestMapping("/foo")
public String foo(String foo) {
    return "hello "+foo+"!";
}
```

修改完成后重启 cloud-gateway-eureka、spring-cloud-producer 项目。访问地址`http://localhost:9000/foo`页面返回：hello null!，说明并没有接受到参数 foo；通过网关来调用此服务，浏览器访问地址`http://localhost:8888/foo`页面返回：hello bar!，说明成功接收到参数 foo 参数的值 bar ,证明网关在转发的过程中已经通过 filter 添加了设置的参数和值。

### 服务化路由转发

上面我们使用 uri 指定了一个服务转发地址，单个服务这样使用问题不大，但是我们在注册中心往往会使用多个服务来共同支撑整个服务的使用，这个时候我们就期望可以将 Filter 作用到每个应用的实例上，spring cloud gateway 工了这样的功能，只需要简单配置即可。

为了测试两个服务提供者是否都被调用，我们在 spring-cloud-producer-1 项目中也同样添加 foo() 方法。

```
@RequestMapping("/foo")
public String foo(String foo) {
    return "hello "+foo+"!!";
}
```

为了和 spring-cloud-producer 中 foo() 方法有所区别，这里使用了两个感叹号。同时将 cloud-gateway-eureka 项目配置文件中的 uri 内容修改如下：

```
#格式为：lb://应用注册服务名
uri: lb://spring-cloud-producer
```

修改完之后，重新启动项目 cloud-gateway-eureka、spring-cloud-producer-1，浏览器访问地址:`http://localhost:8888/foo`页面交替出现：

```
hello bar!
hello bar!!
```

证明请求依据均匀转发到后端服务，并且后端服务均接收到了 filter 增加的参数 foo 值。

这里其实默认使用了全局过滤器 LoadBalancerClient ，当路由配置中 uri 所用的协议为 lb 时（以uri: lb://spring-cloud-producer为例），gateway 将使用 LoadBalancerClient 把 spring-cloud-producer 通过 eureka 解析为实际的主机和端口，并进行负载均衡。

## 修改请求路径的过滤器

**StripPrefix Filter**

StripPrefix Filter 是一个请求路径截取的功能，我们可以利用这个功能来做特殊业务的转发。

application.yml 配置如下：

```
spring:
  cloud:
    gateway:
      routes:
      - id: nameRoot
        uri: http://nameservice
        predicates:
        - Path=/name/**
        filters:
        - StripPrefix=2
```

上面这个配置的例子表示，当请求路径匹配到`/name/**`会将包含name和后边的字符串接去掉转发， `StripPrefix=2`就代表截取路径的个数，这样配置后当请求`/name/bar/foo`后端匹配到的请求路径就会变成`http://nameservice/foo`。

我们还是在 cloud-gateway-eureka 项目中进行测试，修改 application.yml 如下：

```
spring:
  cloud:
     routes:
     - id: nameRoot
       uri: lb://spring-cloud-producer
       predicates:
       - Path=/name/**
       filters:
       - StripPrefix=2
```

配置完后重启 cloud-gateway-eureka 项目，访问地址：`http://localhost:8888/name/foo/hello`页面会交替显示：

```
hello world!
hello world smile!
```

和直接访问地址 `http://localhost:8888/hello`展示的效果一致，说明请求路径中的 `name/foo/` 已经被截取。

**PrefixPath Filter**

PrefixPath Filter 的作用和 StripPrefix 正相反，是在 URL 路径前面添加一部分的前缀

```
spring:
  cloud:
    gateway:
      routes:
      - id: prefixpath_route
        uri: http://example.org
        filters:
        - PrefixPath=/mypath
```

大家可以下来去测试，这里不在演示。



 filter中还有限速路由器 RequestRateLimiter，熔断路由器 Hystrix，重试路由器 RetryGatewayFilter 就不一一介绍了。

