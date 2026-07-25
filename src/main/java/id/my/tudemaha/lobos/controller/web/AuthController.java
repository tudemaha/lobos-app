package id.my.tudemaha.lobos.controller.web;

import id.my.tudemaha.lobos.dto.request.UserLogin;
import id.my.tudemaha.lobos.dto.request.UserRegister;
import id.my.tudemaha.lobos.exception.DuplicateEmailException;
import id.my.tudemaha.lobos.exception.LoginException;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    public String loginPage(Model model) {
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

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return "redirect:/";
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

}
