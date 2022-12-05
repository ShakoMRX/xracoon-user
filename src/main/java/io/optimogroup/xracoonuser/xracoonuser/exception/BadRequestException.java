package io.optimogroup.xracoonuser.xracoonuser.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author Paata Lominadze
 * @version 1.0.0.1
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ControllerAdvice()
public class BadRequestException extends BaseException {
    private String message;
}
