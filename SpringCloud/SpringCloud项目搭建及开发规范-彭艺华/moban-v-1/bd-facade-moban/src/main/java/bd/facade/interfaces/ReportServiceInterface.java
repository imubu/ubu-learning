package bd.facade.interfaces;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import bd.facade.api.resp.RespBean;
import bd.facade.hystrics.ReportServiceHystric;
/**
 * 业务调用接口层，可实现负载均衡
 * @author yihua
 *
 */
@FeignClient(name = "bd-service-moban", fallback = ReportServiceHystric.class)
public interface ReportServiceInterface {

	
	@RequestMapping(value = "/report/insertSingle", method = RequestMethod.POST)
	public RespBean insertSingle(@RequestBody String jsonParam);

	@RequestMapping(value = "/report/updateSingle", method = RequestMethod.POST)
	public RespBean updateSingle(@RequestBody String jsonParam);

}
