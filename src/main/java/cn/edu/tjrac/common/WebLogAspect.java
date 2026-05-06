package cn.edu.tjrac.common;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Aspect
@Component
@Slf4j
public class WebLogAspect {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // 切点：Controller + Service + Dao
    @Pointcut("execution(public * cn.edu.tjrac.controller..*(..)) || " +
            "execution(public * cn.edu.tjrac..service..*(..)) || " +
            "execution(public * cn.edu.tjrac..dao..*(..))")
    public void allLog() {}

    @Around("allLog()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 基础信息
        String className = point.getTarget().getClass().toString();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String params = Arrays.toString(point.getArgs());

        boolean isController = className.contains("controller");

        // ====================== Controller 打印请求信息 ======================
        String requestUrl = "";
        String ip = "";
        String startTime = "";
        if (isController) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            requestUrl = request.getRequestURL().toString();
            ip = getIpAddr(request);
            startTime = sdf.format(new Date());
            log.info("====================== 请求开始 ======================");
            log.info("请求地址：{}", requestUrl);
            log.info("客户端IP：{}", ip);
            log.info("开始时间：{}", startTime);
        }

        // ====================== 统一打印：进入方法 ======================
        log.info("进入 → {}.{}，参数：{}", className, methodName, params);

        long start = System.currentTimeMillis();
        Object result = null;

        try {
            // 执行方法
            result = point.proceed();

            // ====================== 统一打印：退出方法 ======================
            log.info("退出 → {}.{}，返回值：{}", className, methodName, JSON.toJSONString(result));

        } catch (Exception e) {
            log.error("异常 → {}.{}，异常信息：", className, methodName, e);
            throw e;
        }

        // ====================== Controller 打印结束信息 ======================
        if (isController) {
            String endTime = sdf.format(new Date());
            long costTime = System.currentTimeMillis() - start;
            log.info("结束时间：{}", endTime);
            log.info("总耗时：{} ms", costTime);
            log.info("====================== 请求结束 ======================\n");
        }

        return result;
    }

    // 获取真实IP
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}