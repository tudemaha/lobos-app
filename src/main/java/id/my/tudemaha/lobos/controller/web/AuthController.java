package id.my.tudemaha.lobos.controller.web;

import id.my.tudemaha.lobos.dto.request.UpdateEmail;
import id.my.tudemaha.lobos.dto.request.UpdatePassword;
import id.my.tudemaha.lobos.dto.request.UserLogin;
import id.my.tudemaha.lobos.dto.request.UserRegister;
import id.my.tudemaha.lobos.dto.request.UserUpdate;
import id.my.tudemaha.lobos.exception.DuplicateEmailException;
import id.my.tudemaha.lobos.exception.ForbiddenAccessException;
import id.my.tudemaha.lobos.exception.LoginException;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;

@Controller
public class AuthController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(UserService userService, SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
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
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        User user;
        try {
            user = userService.authenticate(userLogin);
        } catch(LoginException e) {
            return "redirect:/login?error";
        }

        refreshAuthentication(user, request, response);

        return "redirect:/collections";
    }

    @PostMapping("/auth/logout")
    public String logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login?logout";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("userUpdate", toUserUpdate(user));
        model.addAttribute("updateEmail", toUpdateEmail(user));
        model.addAttribute("updatePassword", new UpdatePassword());
        return "auth/profile";
    }

    @PostMapping("/auth/update")
    public String updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("userUpdate") UserUpdate userUpdate,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("updateEmail", toUpdateEmail(user));
            model.addAttribute("updatePassword", new UpdatePassword());
            return "auth/profile";
        }

        userService.update(userUpdate, user.getId());

        user.setFirstName(userUpdate.getFirstName());
        user.setLastName(userUpdate.getLastName());
        refreshAuthentication(user, request, response);

        return "redirect:/profile?updated";
    }

    @PatchMapping("/auth/email")
    public String updateEmail(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("updateEmail") UpdateEmail updateEmail,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("userUpdate", toUserUpdate(user));
            model.addAttribute("updatePassword", new UpdatePassword());
            return "auth/profile";
        }

        try {
            userService.updateEmail(updateEmail, user.getId());
        } catch (DuplicateEmailException e) {
            bindingResult.addError(new FieldError("updateEmail", "email", e.getMessage()));
            model.addAttribute("userUpdate", toUserUpdate(user));
            model.addAttribute("updatePassword", new UpdatePassword());
            return "auth/profile";
        }

        user.setEmail(updateEmail.getEmail());
        refreshAuthentication(user, request, response);

        return "redirect:/profile?updated";
    }

    @PatchMapping("/auth/password")
    public String updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("updatePassword") UpdatePassword updatePassword,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("userUpdate", toUserUpdate(user));
            model.addAttribute("updateEmail", toUpdateEmail(user));
            return "auth/profile";
        }

        try {
            userService.updatePassword(updatePassword, user.getId());
        } catch (ForbiddenAccessException e) {
            bindingResult.addError(new FieldError("updatePassword", "oldPassword", e.getMessage()));
            model.addAttribute("userUpdate", toUserUpdate(user));
            model.addAttribute("updateEmail", toUpdateEmail(user));
            return "auth/profile";
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("updatePassword", "confirmNewPassword", e.getMessage()));
            model.addAttribute("userUpdate", toUserUpdate(user));
            model.addAttribute("updateEmail", toUpdateEmail(user));
            return "auth/profile";
        }

        return "redirect:/profile?updated";
    }

    @DeleteMapping("/auth")
    public String deleteAccount(@AuthenticationPrincipal User user, HttpServletRequest request) {
        userService.delete(user.getId());

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login?deleted";
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

    private UserUpdate toUserUpdate(User user) {
        UserUpdate userUpdate = new UserUpdate();
        userUpdate.setFirstName(user.getFirstName());
        userUpdate.setLastName(user.getLastName());
        return userUpdate;
    }

    private UpdateEmail toUpdateEmail(User user) {
        UpdateEmail updateEmail = new UpdateEmail();
        updateEmail.setEmail(user.getEmail());
        return updateEmail;
    }
}
