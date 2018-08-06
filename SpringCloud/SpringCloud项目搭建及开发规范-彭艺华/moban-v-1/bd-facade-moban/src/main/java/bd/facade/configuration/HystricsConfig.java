package bd.facade.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Retryer;
/**
 * 熔断设置
 * @author yihua
 *
 */
@Configuration
public class HystricsConfig {
	 @Bean
	    Retryer feignRetryer() {
	        return Retryer.NEVER_RETRY;
	    }
}
