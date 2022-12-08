package io.optimogroup.xracoonuser.xracoonuser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;

@AllArgsConstructor
@Builder
@Data
public class UserAvatarDTO {

    private String path;
    private String code;
}
