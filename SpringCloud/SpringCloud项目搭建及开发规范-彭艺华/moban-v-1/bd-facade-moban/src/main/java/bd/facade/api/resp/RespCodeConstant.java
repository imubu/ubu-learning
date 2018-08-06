package bd.facade.api.resp;

/**
 * 服务端返回的应答编码列表
 * 
 * @author xhw
 *
 */
public class RespCodeConstant {

	// 成功
	public static final String SUCCESS = "100";
	public static final String SUCCESS_DESC = "处理成功";

	// 数据校验失败
	public static final String VALIDATE_ERROR = "101";
	public static final String VALIDATE_ERROR_DESC = "数据处理校验失败";

	// 系统异常
	public static final String SYSTEM_EXCEPTION = "102";
	public static final String SYSTEM_EXCEPTION_DESC = "系统异常";

	// 数据库异常
	public static final String DB_EXCEPTION = "103";
	public static final String DB_EXCEPTION_DESC = "数据库异常";

	// 没查询到数据
	public static final String DATA_NOT_FIND = "104";
	public static final String DATA_NOT_FIND_DESC = "没查询到数据";

	// 客户端传入值数据为空
	public static final String DATA_NOT_FIND_NULL = "105";
	public static final String DATA_NOT_FIND_NULL_DESC = "客户端传入值数据为空";

	// 客户端传入值数据为空
	public static final String PARAM_NOT_MATCH = "106";
	public static final String PARAM_NOT_MATCH_DESC = "客户端传入值参数不匹配";

	// 失败
	public static final String ERROR = "107";
	public static final String ERROR_DESC = "处理失败";

	public static final String CONN_ERROR = "108";
	public static final String CONN_ERROR_DESC = "OPM系统连接异常";

	// 未知的错误
	public static final String UNKNOWED_ERROR = "-1";
	public static final String UNKNOWED_ERROR_DESC = "未知的错误";

	// 109 token找不到 110 token验证不通过
	public static final String TOKEN_NOT_FIND = "109";
	public static final String TOKEN_VALIDATE_ERROR = "110";

	// 服务不通的错误
	public static final String SERVICE_VALIDATE_ERROR = "111";
	// 服务不通的错误
	public static final String SERVICE_VALIDATE_ERROR_DESC="service服务提供中断";
}