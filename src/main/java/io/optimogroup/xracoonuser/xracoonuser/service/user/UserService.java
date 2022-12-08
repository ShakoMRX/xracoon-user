package io.optimogroup.xracoonuser.xracoonuser.service.user;


import io.optimogroup.xracoon.shared.models.BaseException;
import io.optimogroup.xracoonuser.xracoonuser.dto.PointsTransactionDTO;
import io.optimogroup.xracoonuser.xracoonuser.dto.UserAvatarDTO;
import io.optimogroup.xracoonuser.xracoonuser.dto.UserDTO;
import io.optimogroup.xracoonuser.xracoonuser.dto.UserDetailsDTO;
import io.optimogroup.xracoonuser.xracoonuser.model.User;

import java.io.IOException;
import java.util.List;

public interface UserService {


    User get(Long userId);
    String getCurrentUserId();

    UserDTO getUserDetails() throws BaseException, IOException;

    List<PointsTransactionDTO> getUserPointTransactions(Long partyId);

    UserAvatarDTO uploadAvatar(Long userId, String path, String code);

    void updateUser(Long id,UserDetailsDTO detailsDTO);
}
