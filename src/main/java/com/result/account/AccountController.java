package com.result.account;

import com.result.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor // @Autowired 생성자 코드를 사용하지 않을 경우 사용
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

//    @Autowired
//    public AccountController(SignUpFormValidator signUpFormValidator, AccountService accountService) {
//        this.signUpFormValidator = signUpFormValidator;
//        this.accountService = accountService;
//    }


    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator); // signUpForm(클래스명을 캐멀케이스로 따라감 -> 변수명 아님)을 받을 때 JSR 303과 더불어 signUpFormValidator를 검증하게 함 (숙제)
    }



    @GetMapping("/sign-up")
    public String signUpForm(Model model) { // 모델의 역할은 데이터를 뷰에 전달해주는 역할이다.
        model.addAttribute("signUpForm", new SignUpForm()); // sign-up을 요청했을 때 model에 signUpForm 객체를 담아서 account/sign-up으로 전달한다.
        model.addAttribute(new SignUpForm()); // signUpForm은 생략가능, 자동으로 캐멀 스타일로 넣어줌
        return "account/sign-up"; // Thymeleaf 설정으로 templates 아래부터 해당 경로를 검색하여 리턴함
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid @ModelAttribute SignUpForm signUpForm, Errors errors) { // 복합객체(SignUpForm)을 모델로 받아올 때는 @ModelAttribute를 사용해야 함
        // Get /sign-up에서 model에 signUpForm 객체를 생성하여 전달하고, Post /sign-up에서 해당 객체를 ModelAttribute SignUpForm 객체러 받아온다.

        if (errors.hasErrors()) { // JSR-303의 방식으로 에러를 체크함 (SignUpForm 변수에 @NotBlank, @Length(min = 3, max = 20)... 등의 조건을 기재해야 함
            // 프론트에서 입력한 데이터가 signUpForm에 입력되는 순간 JSR-303 기반의 애노테이션 조건 체크 진행 후 에러가 존재하는 경우 erorrs 객체에 에러를 담는다.
            return "account/sign-up";
        }

//        signUpFormValidator.validate(signUpForm, errors);
//        if (errors.hasErrors()) { // 이번엔 signUpFormValidator의 결과에 대해 검증
//            return "account/sign-up";
//        }
//        // 위 코드를 다른 방법으로 사용 가능 (@InitBinder("signUpForm") 사용)

//        accountService.processNewAccount(signUpForm); // 아래 코드로 리펙터링
        Account account = accountService.processNewAccount(signUpForm); // MVC(Model에 해당하는 작업은 Service에서 처리하게 함), Controller는 Controller 역할만 수행
        accountService.login(account); // 로그인 기능
        return "redirect:/"; // 성공시 리다이렉션 수행

    }


    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) { // 여기서 모델은 Get 요청시 해당 메서드에서 생성한 값을 전달하기 위함
        // get 요청 파라메터로 받은 token, email을 사용
        Account account = accountRepository.findByEmail(email);// accountRepository를 도메인 계층으로 보고 Controller에서 사용 (각 기능별 계층을 알아보기 숙제)

        String view = "account/checked-email";

        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

//        boolean emailTokenCheck = account.getEmailCheckToken().equals(token);
//        if (!emailTokenCheck) { // 아래 코드로 리펙터링 (isValidToken())
        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }



//        account.setEmailVerified(true);
//        account.setJoinedAt(LocalDateTime.now());
        // 아래 코드로 리펙터링
        account.completeSignUp();
        accountService.login(account); // 로그인 기능

        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        // view에서 사용할 변수를 모델 객체에 넣어줌

        return view;
    }


}
