package io.optimogroup.xracoonuser.xracoonuser.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
/**
 * @author Paata Lominadze
 * @version 1.0.0.1
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseException extends RuntimeException {
    private HttpStatus httpStatus;
    private String message;
}
