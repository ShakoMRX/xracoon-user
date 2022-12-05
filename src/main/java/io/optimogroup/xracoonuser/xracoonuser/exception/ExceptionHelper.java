package io.optimogroup.xracoonuser.xracoonuser.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

/**
 * @author Paata Lominadze
 * @version 1.0.0.1
 */

@RestControllerAdvice
@Slf4j
public class ExceptionHelper {

    @ExceptionHandler(value = {BadRequestException.class})
    public ResponseEntity<Object> handleInvalidInputException(BadRequestException ex) {
        log.error("Bad Request Exception: ", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {InvalidInputException.class})
    public ResponseEntity<Object> handleInvalidInputException(InvalidInputException ex) {
        log.error("Invalid Input Exception: ", ex);
        return new ResponseEntity<>(ApiExceptionResponse.builder().message(ex.getMessage()).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {HttpClientErrorException.Unauthorized.class})
    public ResponseEntity<Object> handleUnauthorizedException(HttpClientErrorException.Unauthorized ex) {
        log.error("Unauthorized Exception: ", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {BusinessException.class})
    public ResponseEntity<ApiExceptionResponse> handleBusinessException(BusinessException ex) {
        log.error("Business Exception: ", ex);
        return new ResponseEntity<>(ApiExceptionResponse.builder().message(ex.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleException(Exception ex) {
        log.error("Some Generic Exception: ", ex);
        return new ResponseEntity<Object>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
