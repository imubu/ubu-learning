package bd.service.api;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import bd.service.api.resp.RespBean;
import bd.service.domain.ReportVo;
import bd.service.service.ReportService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@RestController
@RequestMapping("/report")
public class ReportController {

	@SuppressWarnings("unused")
	private Logger LOG = Logger.getLogger(ReportController.class);

	@Autowired
	private RespBean respBean;

	@Resource
	private ReportService reportService;

	

	/**
	 * 插入数据
	 * 
	 * @param jsonParam
	 * @return
	 */
	@ApiOperation("插入单条报表")
	@ApiResponses({ @ApiResponse(code = 100, message = "返回正常"), @ApiResponse(code = 107, message = "接口内部异常出错") })
	@RequestMapping(value = "/insertSingle", method = RequestMethod.POST)
	public RespBean insertSingle(@RequestBody String jsonParam) {
		ReportVo reportVo = JSON.parseObject(jsonParam, ReportVo.class);
		int i = reportService.insertSingle(reportVo);
		if (i > 0) {
			respBean.setRespData(1);
		} else {
			respBean.setRespData(0);
			respBean.setRespDesc("数据插入失败");
		}
		return respBean;
	}


	/**
	 * 更新数据
	 * 
	 * @param jsonParam
	 * @return
	 */
	@ApiOperation("更新单条报表")
	@ApiResponses({ @ApiResponse(code = 100, message = "返回正常"), @ApiResponse(code = 107, message = "接口内部异常出错") })
	@RequestMapping(value = "/updateSingle", method = RequestMethod.POST)
	public RespBean updateSingle(@RequestBody @ApiParam(value = "{\"report_id\":1,\"report_content\":\"sdsadasdasss\"}", required = true) String jsonParam) {
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ReportVo reportVo = JSON.parseObject(jsonParam, ReportVo.class);
		int i = reportService.updateSingle(reportVo);
		if (i > 0) {
			respBean.setRespData(1);
		} else {
			respBean.setRespData(0);
			respBean.setRespDesc("数据更新失败");
		}
		return respBean;
	}

	

	
	
	

}
