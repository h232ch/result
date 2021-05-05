package com.result.account;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor // 클래스 생성자의 매개변수가 1개이고, private final로 지정된 변수의 경우 자동으로  @Autowire된 생성자를 생성하는 애노테이션 (Lombok이다)
// 아래의 생성자 처럼 생성해줌 (현재는 해당 애노테이션이 존재하기 때문에 삭제함)
public class SignUpFormValidator implements Validator { // JSR-303의 에러 처리로 확인 불가한 경우(DB에 값을 직접 조회해야하는 경우 등은 Validator로 처리해줌)


//    @Autowired
//    public SignUpFormValidator(AccountRepository accountRepository) {
//        this.accountRepository = accountRepository;
//    }

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(SignUpForm.class); // SignUpForm에 대해 할당해줌
    }

    @Override
    public void validate(Object object, Errors errors) {
        // TODO: 5/5/2021 email, nickname 중복 검증
        SignUpForm signUpForm = (SignUpForm) object; // SignUpForm은 빈으로 등록되지 않음

        if (accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일 입니다.");
        }

        if (accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 닉네임 입니다.");
        }
    }
}
