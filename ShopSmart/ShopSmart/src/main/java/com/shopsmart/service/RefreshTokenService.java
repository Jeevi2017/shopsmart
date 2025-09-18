package com.shopsmart.service;

import com.shopsmart.entity.RefreshToken;
import com.shopsmart.entity.User;
import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    Optional<RefreshToken> findByToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteRefreshToken(RefreshToken token);
    void deleteByUserId(Long userId); 
}
