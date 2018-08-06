package bd.facade.hystrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bd.facade.api.resp.RespBean;
import bd.facade.interfaces.LoginServiceInterface;
/**
 * controller的熔断层，调用服务无响应的时候，在这里处理
 * @author yihua
 *
 */
@Component
public class LoginServiceHystric implements LoginServiceInterface{
	
	@Autowired
	private RespBean respBean;

	@Override
	public int isLoginUser(String token) {
		// TODO Auto-generated method stub
		return -1;
	}


}
