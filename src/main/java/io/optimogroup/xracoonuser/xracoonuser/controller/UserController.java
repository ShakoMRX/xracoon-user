package io.optimogroup.xracoonuser.xracoonuser.controller;

import io.optimogroup.xracoonuser.xracoonuser.dto.RatingDto;
import io.optimogroup.xracoonuser.xracoonuser.dto.UserDetailsDTO;
import io.optimogroup.xracoonuser.xracoonuser.exception.BadRequestException;
import io.optimogroup.xracoonuser.xracoonuser.repository.RatingRepository;
import io.optimogroup.xracoonuser.xracoonuser.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("user")
public class UserController {

    private final UserService userService;
    private final RatingRepository repository;


    @GetMapping("detail-info")
    public ResponseEntity<?> getUserDetailInfo() throws IOException {
        return new ResponseEntity<>(userService.getUserDetails(), HttpStatus.OK);
    }

    @GetMapping("point-transactions/{partyId}")
    public ResponseEntity<?> getUserPointTransactions(@PathVariable Long partyId) {
        return new ResponseEntity<>(userService.getUserPointTransactions(partyId), HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody UserDetailsDTO userDetail) {
        if (id == null) throw new BadRequestException("Invalid user id provided!");
        userService.updateUser(id, userDetail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("upload-avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam Long userId,
            @RequestParam String path, @RequestParam String colorCode) {
        return new ResponseEntity<>(userService.uploadAvatar(userId, path, colorCode), HttpStatus.OK);
    }

    @GetMapping("get-user-avatar/{partyId}")
    public ResponseEntity<?> getUserAvatar(@PathVariable Long partyId) {
        return new ResponseEntity<>(userService.getUserAvatar(partyId), HttpStatus.OK);
    }

    @PostMapping
    public void saveRating(@RequestBody RatingDto ratingDto) {
        repository.save(ratingDto);
    }

    @GetMapping
    private List<RatingDto> get() {
        return repository.findAll();
    }

}
