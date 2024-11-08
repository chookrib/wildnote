package cool.done.wildnotesvc.adapter.driving;

import cool.done.wildnotesvc.domain.NotLoginException;
import cool.done.wildnotesvc.domain.ValidationException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 全局异常处理器
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 未登录异常处理器
     */
    @ExceptionHandler(value = NotLoginException.class)
    @ResponseBody
    public Result notLoginExceptionHandler(HttpServletResponse response, NotLoginException e) {
        return Result.error(ResultCodes.ERROR_NOT_LOGIN, e.getMessage());
    }

    /**
     * 验证失败异常处理器
     */
    @ExceptionHandler(value = ValidationException.class)
    @ResponseBody
    public Result validationExceptionHandler(HttpServletResponse response, ValidationException e) {
        return Result.error(ResultCodes.ERROR_VALIDATION, e.getMessage());
    }

    /**
     * 默认异常处理器
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result defaultExceptionHandler(HttpServletResponse response, Exception e) {
        logger.error("捕捉到未处理的异常", e);
        //response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        //String message = e.getMessage();
        String message = e.toString();
        if(message == null) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            message = errors.toString();
        }
        return Result.error(ResultCodes.ERROR_DEFAULT, message);
    }
}
