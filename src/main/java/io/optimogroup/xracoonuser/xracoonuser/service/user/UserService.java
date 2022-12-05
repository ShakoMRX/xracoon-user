package io.optimogroup.xracoonuser.xracoonuser.service.user;


import io.optimogroup.xracoon.shared.models.BaseException;
import io.optimogroup.xracoonuser.xracoonuser.dto.UserDTO;
import io.optimogroup.xracoonuser.xracoonuser.dto.UserDetailsDTO;

import java.io.IOException;

public interface UserService {


    String getCurrentUserId();

    UserDTO getUserDetails() throws BaseException, IOException;
}