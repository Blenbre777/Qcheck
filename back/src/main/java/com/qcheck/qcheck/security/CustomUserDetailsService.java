package com.qcheck.qcheck.security;

import com.qcheck.qcheck.entity.Emp;
import com.qcheck.qcheck.repository.EmpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Spring Security의 UserDetailsService 구현
 *
 * 책임:
 * - 사용자명(이메일)으로 사용자 정보를 데이터베이스에서 조회
 * - UserDetails 객체 생성 및 반환
 * - 권한(Authority) 설정
 *
 * 사용처:
 * - JwtAuthenticationFilter에서 토큰으로부터 추출한 이메일로 사용자 조회
 * - Spring Security의 AuthenticationManager에서 인증 시 사용
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmpRepository empRepository;

    /**
     * 이메일을 기준으로 사용자 정보 조회
     *
     * @param email 사원 이메일 (username으로 사용)
     * @return UserDetails 객체 (Spring Security에서 사용)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일로 사원 정보 조회
        Emp emp = empRepository.findByEmpEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // 사용 중단 상태 확인
        if ("N".equals(emp.getUseYn())) {
            log.warn("User account is disabled: {}", email);
            throw new UsernameNotFoundException("User account is disabled: " + email);
        }

        // 권한 설정 (ROLE_ 접두사 추가)
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + emp.getEmpRole().toString()));

        log.debug("User loaded successfully: {}", email);

        // Spring Security의 User 객체 반환
        // - username: 이메일
        // - password: 암호화된 비밀번호 (인증 시 사용하지 않음, JWT 필터에서 처리)
        // - enabled: 활성 여부 (emp.getUseYn()이 'Y'인 경우만)
        // - accountNonExpired: true (계정 만료 무제한)
        // - credentialsNonExpired: true (자격증명 만료 무제한)
        // - accountNonLocked: true (계정 잠금 없음)
        return User.builder()
                .username(email)
                .password(emp.getEmpPw()) // JWT 필터에서는 사용하지 않음
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
