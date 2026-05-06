package cn.edu.tjrac.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        log.error("全局异常: {} {}", request.getMethod(), request.getRequestURI(), e);
        return Result.fail("系统异常");
    }

    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
}
