package uz.mkb.auth.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import uz.mkb.auth.repository.UserRepository
import org.springframework.security.core.userdetails.User as SpringUser

@Service
class CustomUserDetailsService(
    val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userRepository.findByUsername(username) ?: throw UsernameNotFoundException("User $username not found")

        return SpringUser
            .builder()
            .username(user.username)
            .password(user.passwordHash)
            .authorities(SimpleGrantedAuthority("ROLE_${user.role.name}"))
            .disabled(!user.enabled)
            .build()
    }
}