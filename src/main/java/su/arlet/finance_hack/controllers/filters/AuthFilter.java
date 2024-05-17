package su.arlet.finance_hack.controllers.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.arlet.finance_hack.exceptions.InvalidAuthorizationHeaderException;
import su.arlet.finance_hack.services.AuthService;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Component
@RequiredArgsConstructor

public class AuthFilter implements Filter {
    private AuthService authService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;

        String auth = req.getHeader("Authorization");

        String[] parsedHeader = auth.split(" ");
        if (parsedHeader[0].equalsIgnoreCase("bearer")) {
            String username = authService.decodeJwtToken(parsedHeader[1]);
            servletRequest.setAttribute(username);
        } else {
            throw new InvalidAuthorizationHeaderException();
        }
        // auth = "Bearer auwehrsduhfsakfnak" // bearer is case in-sensitive
        // auwhuahewuhasdu
        // <-(jwt potroshit')->
        // nickname

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
