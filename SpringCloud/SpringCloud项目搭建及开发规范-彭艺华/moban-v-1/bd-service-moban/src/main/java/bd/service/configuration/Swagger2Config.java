package bd.service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableScheduling
@EnableAsync
@EnableSwagger2
public class Swagger2Config {


	@Bean
	  public Docket createRestApi() {
	    return new Docket(DocumentationType.SWAGGER_2)
	        .apiInfo(apiInfo())
	        .select()
	        .apis(RequestHandlerSelectors.basePackage("bd.service.report.engine.api"))
	        .paths(PathSelectors.any())
	        .build();
	  }
	  @SuppressWarnings("deprecation")
	private ApiInfo apiInfo() {
	    return new ApiInfoBuilder()
	        .title("bd-service-report-engine API")
	        .description("VERSION 3.0.0")
//	        .termsOfServiceUrl("http://hwangfantasy.github.io/")
	        .contact("pengyihua")
	        .version("3.0.0")
	        .build();
	  }
	


}
