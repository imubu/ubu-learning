package bd.facade.interfaces;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import bd.facade.hystrics.LoginServiceHystric;
/**
 * 业务调用接口层，可实现负载均衡
 * @author yihua
 *
 */
@FeignClient(name = "bd-service-user", fallback = LoginServiceHystric.class)
public interface LoginServiceInterface {

	
	@RequestMapping(value = "/user/isLoginUser", method = RequestMethod.GET)
	public int isLoginUser(@RequestParam(value = "token",required=false) String token);


}
