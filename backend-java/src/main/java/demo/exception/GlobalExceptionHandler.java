package demo.exception;

import demo.pojo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class) // Catch all exceptions
    public Result ex(Exception ex) {
        ex.printStackTrace();
        return Result.error("Sorry, operation failed. Please contact the administrator");
    }

}
