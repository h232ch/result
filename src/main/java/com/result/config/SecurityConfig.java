package com.result.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity // Security 설정을 온전히 이 파일에서 진행하겠다.
public class SecurityConfig extends WebSecurityConfigurerAdapter { // 손쉽게 설정하기 위해 상속받음 (이미 설정값들이 존재하기 때문에 오버라이딩하여 사용하면 됨)


    @Override
    protected void configure(HttpSecurity http) throws Exception { // http 접속과 관련한 Security 설정 
        http.authorizeRequests()
                .mvcMatchers("/", "/login", "/sign-up", "/check-email", "/check-email-token",
                        "/email-login", "/check-email-login", "/login-link").permitAll() // 해당 url 요청은 모두 security를 적용하지 않음
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll() // profile의 경우 get 요청만 모두 security를 적용하지 않음
                .anyRequest().authenticated(); // 그외 요청은 모두 security를 적용하겠다.
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()); // 스프링부트에서 제공하는 기본 staticResource에 대해 스프링시큐리티 제외 처리, 위의 코드에서도 처리 가능
    }
}
