package bd.facade.api;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import bd.facade.api.resp.RespBean;
import bd.facade.interfaces.LoginServiceInterface;
import bd.facade.interfaces.ReportServiceInterface;
/**
 * controller层，默认json格式应答
 * @author yihua
 *
 */
@RestController
@RequestMapping("/report")
public class ReportController {

	@SuppressWarnings("unused")
	private Logger LOG = Logger.getLogger(ReportController.class);

	 @Autowired
	 ReportServiceInterface reportServiceInterface;
	 @Autowired
	LoginServiceInterface loginServiceInterface;


	@RequestMapping(value = "/insertSingle", method = RequestMethod.POST)
	public RespBean insertSingle(@RequestBody String jsonParam) {
		
		int userid=	loginServiceInterface.isLoginUser("664048fa236c9eb81560902071d4b0e1");
			System.out.println(userid);
		return reportServiceInterface.insertSingle(jsonParam);
	}

	@RequestMapping(value = "/updateSingle", method = RequestMethod.POST)
	public RespBean updateSingle(@RequestBody String jsonParam) {
		int userid=	loginServiceInterface.isLoginUser("664048fa236c9eb81560902071d4b0e1");
		System.out.println(userid);
		return reportServiceInterface.updateSingle(jsonParam);
	}

}
