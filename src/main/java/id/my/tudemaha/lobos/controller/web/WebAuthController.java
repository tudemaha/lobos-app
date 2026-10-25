package id.my.tudemaha.lobos.controller.web;

import id.my.tudemaha.lobos.dto.request.ResetPassword;
import id.my.tudemaha.lobos.dto.request.UserLogin;
import id.my.tudemaha.lobos.dto.request.UserRegister;
import id.my.tudemaha.lobos.exception.DuplicateEmailException;
import id.my.tudemaha.lobos.exception.EmailNotVerifiedException;
import id.my.tudemaha.lobos.exception.InvalidTokenException;
import id.my.tudemaha.lobos.exception.LoginException;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebAuthController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;
    private final RememberMeServices rememberMeServices;

    public WebAuthController(
            UserService userService,
            SecurityContextRepository securityContextRepository,
            RememberMeServices rememberMeServices) {
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
        this.rememberMeServices = rememberMeServices;
    }

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/register")
    public String registerPage(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            return "redirect:/collections";
        }

        model.addAttribute("userRegister", new UserRegister());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String register(
            @Valid @ModelAttribute("userRegister") UserRegister userRegister, BindingResult bindingResult) {
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
    public String loginPage(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            return "redirect:/collections";
        }

        model.addAttribute("userLogin", new UserLogin());
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String login(
            @Valid @ModelAttribute("userLogin") UserLogin userLogin,
            @RequestParam(value = "rememberMe", defaultValue = "false") boolean rememberMe,
            HttpServletRequest request,
            HttpServletResponse response) {
        User user;
        try {
            user = userService.authenticate(userLogin);
        } catch (EmailNotVerifiedException e) {
            return "redirect:/login?unverified&email=" + userLogin.getEmail();
        } catch (LoginException e) {
            return "redirect:/login?error";
        }

        refreshAuthentication(user, request, response);

        if (rememberMe) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
            rememberMeServices.loginSuccess(request, response, authToken);
        }

        return "redirect:/collections";
    }

    @GetMapping("/verify-email")
    public String verifyEmailWeb(
            @RequestParam(value = "t", required = false) String t,
            @AuthenticationPrincipal User loggedInUser,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        if (t == null || t.isBlank()) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Verification token is missing.");
            return "auth/verify-email";
        }

        try {
            userService.verifyEmail(t);
            if (loggedInUser != null) {
                User freshUser = userService.findById(loggedInUser.getId());
                refreshAuthentication(freshUser, request, response);
            }
            model.addAttribute("success", true);
            model.addAttribute("message", "Your email has been verified successfully!");
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "The verification link is invalid, has expired, or has already been used.");
        }

        return "auth/verify-email";
    }

    @PostMapping("/auth/resend-verification")
    public String resendVerificationWeb(@RequestParam("email") String email) {
        if (email != null && !email.isBlank()) {
            userService.resendEmailVerification(email);
        }
        return "redirect:/login?resent";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(@AuthenticationPrincipal User user) {
        if (user != null) {
            return "redirect:/collections";
        }
        return "auth/forgot-password";
    }

    @PostMapping("/auth/forgot-password")
    public String forgotPasswordWeb(@RequestParam("email") String email) {
        if (email != null && !email.isBlank()) {
            userService.forgotPassword(email);
        }
        return "redirect:/login?forgot";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam(value = "t", required = false) String t, @AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            return "redirect:/collections";
        }

        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setToken(t);
        model.addAttribute("resetPassword", resetPassword);

        try {
            userService.validateResetToken(t);
            model.addAttribute("tokenValid", true);
        } catch (InvalidTokenException e) {
            model.addAttribute("tokenValid", false);
            model.addAttribute(
                    "errorMessage", "The password reset link is invalid, has expired, or has already been used.");
        }

        return "auth/reset-password";
    }

    @PostMapping("/auth/reset-password")
    public String resetPasswordWeb(
            @Valid @ModelAttribute("resetPassword") ResetPassword resetPassword,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tokenValid", true);
            return "auth/reset-password";
        }

        try {
            userService.resetPassword(resetPassword);
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("resetPassword", "confirmPassword", e.getMessage()));
            model.addAttribute("tokenValid", true);
            return "auth/reset-password";
        } catch (InvalidTokenException e) {
            model.addAttribute("tokenValid", false);
            model.addAttribute(
                    "errorMessage", "The password reset link is invalid, has expired, or has already been used.");
            return "auth/reset-password";
        }

        return "redirect:/login?reset";
    }

    @PostMapping("/auth/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        if (rememberMeServices instanceof LogoutHandler logoutHandler) {
            logoutHandler.logout(
                    request, response, SecurityContextHolder.getContext().getAuthentication());
        }

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login?logout";
    }

    private void refreshAuthentication(User user, HttpServletRequest request, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
