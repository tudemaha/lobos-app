package id.my.tudemaha.lobos.controller;

import id.my.tudemaha.lobos.dto.HttpResponse;
import id.my.tudemaha.lobos.dto.UserRequest;
import id.my.tudemaha.lobos.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<HttpResponse<Void>> registerUser(@RequestBody UserRequest userRequest) {
        userService.register(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(HttpResponse.success("user registered successfully", null));
    }

}
