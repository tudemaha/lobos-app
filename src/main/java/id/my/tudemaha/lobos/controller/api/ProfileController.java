package id.my.tudemaha.lobos.controller.api;

import id.my.tudemaha.lobos.dto.request.UpdateEmail;
import id.my.tudemaha.lobos.dto.request.UpdatePassword;
import id.my.tudemaha.lobos.dto.request.UserUpdate;
import id.my.tudemaha.lobos.dto.response.HttpResponse;
import id.my.tudemaha.lobos.dto.response.UserProfile;
import id.my.tudemaha.lobos.mapper.UserMapper;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "Current authenticated user profile endpoints")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<HttpResponse<UserProfile>> getProfile(@AuthenticationPrincipal User user) {
        User freshUser = userService.findById(user.getId());
        return ResponseEntity.ok(HttpResponse.success(UserMapper.toProfile(freshUser)));
    }

    @PutMapping
    @Operation(summary = "Update current user profile details")
    public ResponseEntity<HttpResponse<Void>> updateProfile(
            @Valid @RequestBody UserUpdate userUpdate, @AuthenticationPrincipal User user) {
        userService.update(userUpdate, user.getId());
        return ResponseEntity.ok(HttpResponse.success());
    }

    @PatchMapping("/email")
    @Operation(summary = "Update current user email address")
    public ResponseEntity<HttpResponse<Void>> updateEmail(
            @Valid @RequestBody UpdateEmail updateEmail, @AuthenticationPrincipal User user) {
        userService.updateEmail(updateEmail, user.getId());
        return ResponseEntity.ok(HttpResponse.success());
    }

    @PatchMapping("/password")
    @Operation(summary = "Update current user password")
    public ResponseEntity<HttpResponse<Void>> updatePassword(
            @Valid @RequestBody UpdatePassword updatePassword, @AuthenticationPrincipal User user) {
        userService.updatePassword(updatePassword, user.getId());
        return ResponseEntity.ok(HttpResponse.success());
    }

    @DeleteMapping
    @Operation(summary = "Delete current user account")
    public ResponseEntity<HttpResponse<Void>> deleteAccount(@AuthenticationPrincipal User user) {
        userService.delete(user.getId());
        return ResponseEntity.ok(HttpResponse.success());
    }
}
