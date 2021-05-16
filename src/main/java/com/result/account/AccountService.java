package com.result.account;

import com.result.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    // 기존에는 서비스 로직이 코드(Model 작업의 코드)가 Controller에 존재했음
    // 리펙토링을 수행해서 서비스 클래스를 생성하고 서비스 코드를 모두 옮긴뒤에 Controller에서는
    // Controller 코드만 집중할 수 있도록 함

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional // saveNewAccount에서 JPA 작업시 Transactional이 적용되어 Persist 상태에 있지만 이후 다시 processNewAccount로 집입하면 Transaction의 범위에서 벗어나기 때문에 persist 상테가 종료됨
    // 이에 따라서 @Transactional 옵션을 붙여주고 아래 작업인 newAccount.generateEmailCheckToken을 해야 generate한 토큰이 DB에 저장된다. (숙제)
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken(); // 저장한 계정에 대해 이메일 체크 토큰 생성
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder() // 오류가 없다면 파라메터를 값으로 받아서 account에 저장하고 accountRepository를 통해 DB에 저장
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
//                .password(signUpForm.getPassword()) // TODO: Password must be changed to hashcode
                .password(passwordEncoder.encode(signUpForm.getPassword())) // 패스워드 일방향 알고리즘 적용 (bcrypt)
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

        Account newAccount = accountRepository.save(account);
        return newAccount;
    }

    private void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage =  new SimpleMailMessage(); // 메일 메세지 객체를 생성하고
        mailMessage.setTo(newAccount.getEmail()); // 계정 이메일에 전송
        mailMessage.setSubject("Study management, 회원 가입 인증"); // 이메일 제목
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() + // 이메일 본문
                "&email=" + newAccount.getEmail());
        javaMailSender.send(mailMessage); // 메일 발송 (실제 메일은 외부 서비스에서 발송함)
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
    }
}
