package id.my.tudemaha.lobos.controller.web;

import id.my.tudemaha.lobos.dto.request.UpdateEmail;
import id.my.tudemaha.lobos.dto.request.UpdatePassword;
import id.my.tudemaha.lobos.dto.request.UserUpdate;
import id.my.tudemaha.lobos.exception.DuplicateEmailException;
import id.my.tudemaha.lobos.exception.ForbiddenAccessException;
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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class WebProfileController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;

    public WebProfileController(UserService userService, SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping
    public String profilePage(
            @AuthenticationPrincipal User user, HttpServletRequest request, HttpServletResponse response, Model model) {
        User freshUser = userService.findById(user.getId());
        refreshAuthentication(freshUser, request, response);

        model.addAttribute("currentUser", freshUser);
        model.addAttribute("userUpdate", toUserUpdate(freshUser));
        model.addAttribute("updateEmail", new UpdateEmail());
        model.addAttribute("updatePassword", new UpdatePassword());
        return "auth/profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("userUpdate") UserUpdate userUpdate,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        if (bindingResult.hasErrors()) {
            User freshUser = userService.findById(user.getId());
            model.addAttribute("currentUser", freshUser);
            model.addAttribute("updateEmail", new UpdateEmail());
            model.addAttribute("updatePassword", new UpdatePassword());
            return "auth/profile";
        }

        userService.update(userUpdate, user.getId());

        User freshUser = userService.findById(user.getId());
        refreshAuthentication(freshUser, request, response);

        return "redirect:/profile?updated";
    }

    @PatchMapping("/email")
    public String updateEmail(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("updateEmail") UpdateEmail updateEmail,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        if (bindingResult.hasErrors()) {
            User freshUser = userService.findById(user.getId());
            model.addAttribute("currentUser", freshUser);
            model.addAttribute("userUpdate", toUserUpdate(freshUser));
            model.addAttribute("updatePassword", new UpdatePassword());
            return "auth/profile";
        }

        try {
            userService.updateEmail(updateEmail, user.getId());
        } catch (DuplicateEmailException e) {
            bindingResult.addError(new FieldError("updateEmail", "email", e.getMessage()));
            User freshUser = userService.findById(user.getId());
            model.addAttribute("currentUser", freshUser);
            model.addAttribute("userUpdate", toUserUpdate(freshUser));
            model.addAttribute("updatePassword", new UpdatePassword());
            return "auth/profile";
        }

        User freshUser = userService.findById(user.getId());
        refreshAuthentication(freshUser, request, response);

        return "redirect:/profile?emailPending";
    }

    @PatchMapping("/password")
    public String updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("updatePassword") UpdatePassword updatePassword,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            User freshUser = userService.findById(user.getId());
            model.addAttribute("currentUser", freshUser);
            model.addAttribute("userUpdate", toUserUpdate(freshUser));
            model.addAttribute("updateEmail", new UpdateEmail());
            return "auth/profile";
        }

        try {
            userService.updatePassword(updatePassword, user.getId());
        } catch (ForbiddenAccessException e) {
            bindingResult.addError(new FieldError("updatePassword", "oldPassword", e.getMessage()));
            User freshUser = userService.findById(user.getId());
            model.addAttribute("currentUser", freshUser);
            model.addAttribute("userUpdate", toUserUpdate(freshUser));
            model.addAttribute("updateEmail", new UpdateEmail());
            return "auth/profile";
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("updatePassword", "confirmNewPassword", e.getMessage()));
            User freshUser = userService.findById(user.getId());
            model.addAttribute("currentUser", freshUser);
            model.addAttribute("userUpdate", toUserUpdate(freshUser));
            model.addAttribute("updateEmail", new UpdateEmail());
            return "auth/profile";
        }

        return "redirect:/profile?updated";
    }

    @DeleteMapping
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
}
