package id.my.tudemaha.lobos.controller.web;

import id.my.tudemaha.lobos.dto.request.UserLogin;
import id.my.tudemaha.lobos.dto.request.UserRegister;
import id.my.tudemaha.lobos.dto.response.AccessToken;
import id.my.tudemaha.lobos.exception.DuplicateEmailException;
import id.my.tudemaha.lobos.exception.LoginException;
import id.my.tudemaha.lobos.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.Duration;

@Controller
public class AuthController {

    private static final String AUTH_COOKIE = "access_token";
    private static final Duration AUTH_COOKIE_TTL = Duration.ofHours(1);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userRegister", new UserRegister());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute("userRegister") UserRegister userRegister, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(userRegister);
        } catch (DuplicateEmailException e) {
            bindingResult.addError(new FieldError("userRegister", "email", e.getMessage()));
            return "auth/register";
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("userRegister", "confirmPassword", e.getMessage()));
            return "auth/register";
        }

        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String login(@ModelAttribute UserLogin userLogin, HttpServletResponse response) {
        AccessToken accessToken;
        try {
            accessToken = userService.login(userLogin);
        } catch (LoginException e) {
            return "redirect:/login?error";
        }

        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(accessToken.getToken(), AUTH_COOKIE_TTL).toString());
        return "redirect:/collections";
    }

    @PostMapping("/auth/logout")
    public String logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie("", Duration.ZERO).toString());
        return "redirect:/login?logout";
    }

    private ResponseCookie buildAuthCookie(String token, Duration ttl) {
        return ResponseCookie.from(AUTH_COOKIE, token)
                .httpOnly(true)
                .path("/")
                .maxAge(ttl)
                .sameSite("Lax")
                .build();
    }
}
