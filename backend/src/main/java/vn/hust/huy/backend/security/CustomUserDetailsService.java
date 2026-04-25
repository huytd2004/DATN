package vn.hust.huy.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import vn.hust.huy.backend.exception.AppException;
import vn.hust.huy.backend.exception.ErrorCode;
import vn.hust.huy.backend.model.entity.User;
import vn.hust.huy.backend.repository.UserRepository;

import java.util.List;

/**
 * Loads a {@link User} from the database by email and adapts it to Spring Security's
 * {@link UserDetails} contract.
 *
 * <p>The granted authority is prefixed with {@code ROLE_} so that Spring's
 * {@code hasRole("ADMIN")} / {@code hasRole("USER")} expressions work correctly.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}
