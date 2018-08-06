package bd.facade.api.resp;

import java.io.Serializable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 服务端返回的数据对象
 * 
 * @author yihua
 *
 */
@SuppressWarnings("serial")
@Component
@Scope("prototype")
public class RespBean implements Serializable {

	// 返回的状态码
	private String respCode = RespCodeConstant.SUCCESS;
	// 返回的备注信息
	private String respDesc = RespCodeConstant.SUCCESS_DESC;
	// 返回的内容数据
	private Object respData;

	public String getRespCode() {
		return respCode;
	}

	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}

	public String getRespDesc() {
		return respDesc;
	}

	public void setRespDesc(String respDesc) {
		this.respDesc = respDesc;
	}

	public Object getRespData() {
		return respData;
	}

	public void setRespData(Object respData) {
		this.respData = respData;
	}

	public RespBean GetServiceError() {
		this.setRespData(null);
		this.setRespCode(RespCodeConstant.SERVICE_VALIDATE_ERROR);
		this.setRespDesc(RespCodeConstant.SERVICE_VALIDATE_ERROR_DESC);
		return this;
	}

}