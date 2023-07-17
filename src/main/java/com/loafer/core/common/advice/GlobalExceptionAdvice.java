package com.loafer.core.common.advice;

import com.aliyuncs.exceptions.ClientException;
import com.loafer.core.common.ErrorResponse;
import com.loafer.core.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.validation.ValidationException;

import java.util.stream.Collectors;

import static com.loafer.core.common.ErrorCode.*;

/**
 * @author
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse<?>> businessExceptionHandler(BusinessException e) {
        return ResponseEntity.status(e.getCode()).body(new ErrorResponse<>(e.getCode(), e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<?>> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        String message = bindingResult.getAllErrors().stream().map(this::wrapError).collect(Collectors.joining(" | "));
        return ResponseEntity.status(PARAMS_ERROR.getCode()).body(new ErrorResponse<>(PARAMS_ERROR.getCode(), message, null));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse<?>> validationExceptionHandler(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ErrorResponse<?>> clientExceptionHandler(ClientException ex) {
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(new ErrorResponse<>(HttpStatus.REQUEST_TIMEOUT.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse<?>> runtimeExceptionHandler(RuntimeException e) {
        e.printStackTrace();
        return ResponseEntity.status(SYSTEM_ERROR.getCode()).body(new ErrorResponse<>(SYSTEM_ERROR.getCode(), e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<?>> exceptionHandler(Exception e) {
        return ResponseEntity.status(OPERATION_ERROR.getCode()).body(new ErrorResponse<>(OPERATION_ERROR.getCode(), e.getMessage(), null));
    }

    @ModelAttribute
    public void globalAttributes(Model model) {
        model.addAttribute("appName", "loafer-core");
    }

    private String wrapError(ObjectError error) {
        String result = "";
        if (error instanceof FieldError) {
            FieldError fieldError = (FieldError) error;
            result += fieldError.getField() + ":";
        }
        result += (error.getDefaultMessage() == null ? "" : error.getDefaultMessage());
        return result;
    }

}