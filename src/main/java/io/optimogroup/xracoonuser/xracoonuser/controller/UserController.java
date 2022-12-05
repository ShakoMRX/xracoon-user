package io.optimogroup.xracoonuser.xracoonuser.controller;

import io.optimogroup.xracoon.shared.models.BaseException;
import io.optimogroup.xracoonuser.xracoonuser.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("user")
public class UserController {

    private final UserService userService;


    @GetMapping("detail-info")
    public ResponseEntity<?> getUserDetailInfo() throws BaseException, IOException {
        return new ResponseEntity<>(userService.getUserDetails(), HttpStatus.OK);
    }

}
