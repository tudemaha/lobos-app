package id.my.tudemaha.lobos.security;

import id.my.tudemaha.lobos.repository.UserRepository;
import id.my.tudemaha.lobos.service.McpTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class McpAuthFilter extends OncePerRequestFilter {
    private final McpTokenService mcpTokenService;
    private final UserRepository userRepository;

    public McpAuthFilter(McpTokenService mcpTokenService, UserRepository userRepository) {
        this.mcpTokenService = mcpTokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        filterChain.doFilter(request, response);
    }
}
