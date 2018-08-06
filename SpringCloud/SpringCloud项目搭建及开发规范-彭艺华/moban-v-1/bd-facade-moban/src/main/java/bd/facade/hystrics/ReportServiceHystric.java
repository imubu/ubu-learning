package bd.facade.hystrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bd.facade.api.resp.RespBean;
import bd.facade.interfaces.ReportServiceInterface;
/**
 * controller的熔断层，调用服务无响应的时候，在这里处理
 * @author yihua
 *
 */
@Component
public class ReportServiceHystric implements ReportServiceInterface{
	
	@Autowired
	private RespBean respBean;

	@Override
	public RespBean insertSingle(String jsonParam) {
		return respBean.GetServiceError();
	}

	@Override
	public RespBean updateSingle(String jsonParam) {
		return respBean.GetServiceError();
	}

}
