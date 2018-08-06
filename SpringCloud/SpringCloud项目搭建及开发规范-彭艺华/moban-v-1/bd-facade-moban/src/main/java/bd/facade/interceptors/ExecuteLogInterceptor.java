package bd.facade.interceptors;

import java.lang.reflect.Method;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import bd.facade.api.resp.RespBean;
import bd.facade.interfaces.LoginServiceInterface;
/**
 * 执行日志记录
 * @author yihua
 *
 */
@Aspect
@Component
@Order(value = 1)
public class ExecuteLogInterceptor {

	private Logger LOG = Logger.getLogger(ExecuteLogInterceptor.class);
	 @Autowired
	 LoginServiceInterface loginServiceInterface;
	/**
	 * 定义拦截规则：拦截bd.facade.report.engine.api包下面的所有类中，有@RequestMapping注解的方法。
	 */
	@Pointcut("execution(* bd.facade.api.*.*(..)) and @annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public void logPointCut() {
	}

	@Around("logPointCut()")
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		// // 接收到请求，记录请求内容
		 ServletRequestAttributes attributes = (ServletRequestAttributes)
		 RequestContextHolder.getRequestAttributes();
		 HttpServletRequest request = attributes.getRequest();
		//
		// // 记录下请求内容
		// LOG.info("请求地址 : " + request.getRequestURL().toString());
		// LOG.info("HTTP METHOD : " + request.getMethod());
		// LOG.info("IP : " + request.getRemoteAddr());
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method method = signature.getMethod();// 获取被拦截的方法
		String methodPath = signature.getDeclaringTypeName();
		String methodName = methodPath+"."+method.getName();
		
		long startTime = System.currentTimeMillis();
		
		LOG.info(methodName+ ">>" + "请求开始，开始时间 : " + new Date(startTime));
//String token=request.getHeader("token");
//	int userid=	loginServiceInterface.isLoginUser(request.getHeader("token"));
//		System.out.println(userid);
		RespBean result = null;	
	    result = (RespBean) pjp.proceed();
	    
	    long endTime = System.currentTimeMillis();
		long costMs =endTime - startTime;
		LOG.info(methodName+ ">>" + "请求结束，结束时间："+new Date(endTime)+"，耗时：" + costMs + "ms" );
		return result;
	}

}
