package io.optimogroup.xracoonuser.xracoonuser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private UserDetailsDTO details;

    private AccountDetail accountDetail;

}
