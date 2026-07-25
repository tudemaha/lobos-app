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
}
