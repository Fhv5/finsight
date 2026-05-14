package io.github.fhv5.finsight.security;

import io.github.fhv5.finsight.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        return userRepository.findByEmail(email)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public UserDetails loadUserById(UUID id) throws UsernameNotFoundException{
        return userRepository.findById(id)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }
}
