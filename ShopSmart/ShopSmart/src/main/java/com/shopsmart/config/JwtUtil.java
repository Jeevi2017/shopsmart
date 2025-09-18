package com.shopsmart.config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Removed @Value as SECRET will now come from SecurityConstants
// import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// Removed Customer import as User is sufficient for ID/Email extraction now that Customer extends User
// import com.example.Ecomm.entitiy.Customer;
import com.shopsmart.entity.User; // Import User entity

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component // Keep @Component as it's a Spring-managed bean
public class JwtUtil {

    // Removed: @Value("${jwt.secret}") private String SECRET;
    // Now using static constant from SecurityConstants

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put("roles", roles);

        System.out.println("DEBUG (JwtService): UserDetails type in generateToken: " + userDetails.getClass().getName());

        // Simplified logic: Since Customer extends User, and User implements UserDetails,
        // we can cast to User directly to get ID and email.
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("id", user.getId());
            claims.put("email", user.getEmail());
            System.out.println("DEBUG (JwtService): Adding User ID to JWT claims: " + user.getId());
        } else {
            System.err.println("WARNING (JwtService): UserDetails is not an instance of User. Cannot add ID/email to JWT.");
        }

        return createToken(claims, userDetails.getUsername()); // Corrected to getUsername
    }

    private String createToken(Map<String, Object> claims, String username) { // Corrected parameter name
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username) // Corrected to username
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME)) // Using constant
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SecurityConstants.SECRET); // Using constant
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
