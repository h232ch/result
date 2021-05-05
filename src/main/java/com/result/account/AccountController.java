package com.result.account;

import com.result.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
//@RequiredArgsConstructor // 25 ~29 코드를 사용하지 않을 경우 사용
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    @Autowired
    public AccountController(SignUpFormValidator signUpFormValidator, AccountRepository accountRepository, JavaMailSender javaMailSender) {
        this.signUpFormValidator = signUpFormValidator;
        this.accountRepository = accountRepository;
        this.javaMailSender = javaMailSender;
    }


    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator); // signUpForm(클래스명을 캐멀케이스로 따라감 -> 변수명 아님)을 받을 때 JSR 303과 더불어 signUpFormValidator를 검증하게 함
    }



    @GetMapping("/sign-up")
    public String signUpForm(Model model) { // 모델의 역할은 데이터를 뷰에 전달해주는 역할이다.
        model.addAttribute("signUpForm", new SignUpForm()); // sign-up을 요청했을 때 model에 signUpForm 객체를 담아서 account/sign-up으로 전달한다.
        model.addAttribute(new SignUpForm()); // signUpForm은 생략가능, 자동으로 캐멀 스타일로 넣어줌
        return "account/sign-up"; // Thymeleaf 설정으로 templates 아래부터 해당 경로를 검색하여 리턴함
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid @ModelAttribute SignUpForm signUpForm, Errors errors) { // 복합객체(SignUpForm)을 모델로 받아올 때는 @ModelAttribute를 사용해야 함

        if (errors.hasErrors()) { // JSR-303의 방식으로 에러를 체크함 (SignUpForm 변수에 @NotBlank, @Length(min = 3, max = 20)... 등의 조건을 기재해야 함
            // 프론트에서 입력한 데이터가 signUpForm에 입력되는 순간 JSR-303 기반의 애노테이션 조건 체크 진행 후 에러가 존재하는 경우 erorrs 객체에 에러를 담는다.
            return "account/sign-up";
        }

//        signUpFormValidator.validate(signUpForm, errors);
//        if (errors.hasErrors()) { // 이번엔 signUpFormValidator의 결과에 대해 검증
//            return "account/sign-up";
//        }
//        // 위 코드를 다른 방법으로 사용 가능 (@InitBinder("signUpForm") 사용)


        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(signUpForm.getPassword()) // TODO: Password must be changed to hashcode
                .studyCreatedByWeb(true)
                .studyCreatedByWeb(true)
                .build();

        Account newAccount = accountRepository.save(account);

        newAccount.generateEmailCheckToken();
        SimpleMailMessage mailMessage =  new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("Study management, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());

        javaMailSender.send(mailMessage);
        return "redirect:/";

    }
}
