package bd.facade.interceptors;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import bd.facade.api.resp.RespBean;
import bd.facade.api.resp.RespCodeConstant;
/**
 * 异常输出日志
 * @author yihua
 *
 */
@Aspect
@Component
@Order(value = 2)
public class ExceptionLogInterceptor {

	private Logger LOG = Logger.getLogger(ExceptionLogInterceptor.class);

	/**
	 * 定义拦截规则：拦截bd.facade.report.engine.api包下面的所有类中，有@RequestMapping注解的方法。
	 */
	@Pointcut("execution(* bd.facade.api.*.*(..)) and @annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public void logPointCut() {
	}

	@Around("logPointCut()")
	public Object doAround(ProceedingJoinPoint pjp) {
		// // 接收到请求，记录请求内容
		// ServletRequestAttributes attributes = (ServletRequestAttributes)
		// RequestContextHolder.getRequestAttributes();
		// HttpServletRequest request = attributes.getRequest();
		//
		// // 记录下请求内容
		// LOG.info("请求地址 : " + request.getRequestURL().toString());
		// LOG.info("HTTP METHOD : " + request.getMethod());
		// LOG.info("IP : " + request.getRemoteAddr());

		long startTime = System.currentTimeMillis();

		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method method = signature.getMethod();// 获取被拦截的方法
		String methodPath = signature.getDeclaringTypeName();
		String methodName = methodPath+"."+method.getName();
		LOG.info(methodName+ ">>" + "请求开始，参数 : " + Arrays.toString(pjp.getArgs()));

		RespBean result = null;
		try {
			// 一切正常的情况下，继续执行被拦截的方法
			result = (RespBean) pjp.proceed();
		} catch (Throwable e) {
			result = new RespBean();
			result.setRespCode(RespCodeConstant.ERROR);
			result.setRespData(null);
			result.setRespDesc("异常信息：" + e);
			long costMs = System.currentTimeMillis() - startTime;
			LOG.error(methodName+ ">>" + "请求异常，"+ "异常信息：" + result.getRespDesc());
		}
		long costMs = System.currentTimeMillis() - startTime;
		LOG.info(methodName+ ">>" + "请求结束，" + "备注/异常信息：" + result.getRespDesc());
		return result;
	}

}
