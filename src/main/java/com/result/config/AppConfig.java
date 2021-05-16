package com.result.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { // 패스워드 해싱처리를 위해 PasswordEncoder 인터페이스를 구현하고 빈으로 등록함
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // bcrypt 인코더를 사용함
    }
}
