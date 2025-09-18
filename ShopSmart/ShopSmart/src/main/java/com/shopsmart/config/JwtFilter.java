    package com.shopsmart.config;


    import com.shopsmart.service.UserService;
    import jakarta.servlet.FilterChain;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
    import org.springframework.stereotype.Component;
    import org.springframework.web.filter.OncePerRequestFilter;

    import java.io.IOException;

    @Component
    public class JwtFilter extends OncePerRequestFilter {

        @Autowired
        private JwtUtil jwtService;

        @Autowired
        private UserService userService;

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;

            System.out.println("DEBUG (JwtFilter): Processing request to: " + request.getRequestURI());

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                try {
                    username = jwtService.extractUsername(token);
                    System.out.println("DEBUG (JwtFilter): Token found. Extracted username: " + username);
                } catch (Exception e) {
                    System.out.println("DEBUG (JwtFilter): Error extracting username from token: " + e.getMessage());
                    
                }
            } else {
                System.out.println("DEBUG (JwtFilter): No Bearer token found in Authorization header.");
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(username);
                System.out.println("DEBUG (JwtFilter): UserDetails loaded for: " + username + ". Roles: " + userDetails.getAuthorities());

                if (jwtService.validateToken(token, userDetails)) {
                    System.out.println("DEBUG (JwtFilter): Token is VALID for user: " + username);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("DEBUG (JwtFilter): SecurityContext updated for: " + username);
                } else {
                    System.out.println("DEBUG (JwtFilter): Token is INVALID for user: " + username);
                }
            } else if (username == null) {
                 System.out.println("DEBUG (JwtFilter): Username is null or SecurityContext already has authentication.");
            }

            filterChain.doFilter(request, response);
        }
    }
    