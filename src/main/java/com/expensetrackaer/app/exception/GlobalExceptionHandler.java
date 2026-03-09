package com.expensetrackaer.app.exception;


import com.expensetrackaer.app.entity.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> resourceNotFoundException(ResourceNotFoundException ex){
         ApiResponse response=new ApiResponse(false, ex.getMessage());

         return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BudgetExceededException.class)
    public ResponseEntity<ApiResponse> budgetExceededException(BudgetExceededException ex){
        ApiResponse response=new ApiResponse(false,ex.getMessage());

        return new ResponseEntity<>(response,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> methodArgumentException(MethodArgumentNotValidException ex){
        Map<String,String> errors=new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error)->{
            String fieldName=((FieldError)error).getField();
            String message= error.getDefaultMessage();
            errors.put(fieldName,message);
        });

        ApiResponse response=new ApiResponse(false,"validation errors",errors);

        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }


       @ExceptionHandler(BusinessValidationException.class)
        public ResponseEntity<ApiResponse> handleBusinessException(BusinessValidationException ex){
            Map<String,String> mp=new HashMap<>();
            mp.put("error",ex.getMessage());
            ApiResponse response=new ApiResponse(false,"Not allowed",mp);
            return  new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
        ApiResponse response = new ApiResponse(false, "An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
