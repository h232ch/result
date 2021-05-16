package com.result.account;

import com.result.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional // SpringBootTest에는 Transaction이 존재하지 않아서 추가해줘야함 (도메인 객체에 바로 값을 넣어야하는 경우 Transacton 추가 (숙제))
// "인증 메일 확인 - 입력값 정상" 테스트를 위해 넣어줌
@SpringBootTest // 전체 테스트 환경 (슬라이스 테스트가 아님), 서블릿 엔진만 띄워서 슬라이스 테스트도 가능 (알아보기 숙제)
@AutoConfigureMockMvc
        // MockMvc 가짜 객체를 사용하여 요청하겠다.
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender; // 메일 샌더 목킹


    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkedEmailToken_with_worng_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "asdfdsaf") // GET 파라메터는 이런 형식으로 넣어줌
                .param("email", "email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error")) // Attribute에 error 변수가 존재하는지 확인
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated()); // SpringSecurity가 적용되어있는 MockMvc의 경우 authenticated 기능을 사용 할 수 있음
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkedEmailToken() throws Exception{

        Account account = Account.builder()
                .email("test@email.com")
                .password("123456789")
                .nickname("h232ch")
                .build();

        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated()); // authenticated로 로그인 여부를 확인함
    }

    @DisplayName("회원 가입 화면이 확인되는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk()) // response 는 Okay
                .andExpect(view().name("account/sign-up"))// View 이름이 sign-up이다.
                .andExpect(model().attributeExists("signUpForm")); // 모델 애트리뷰트에 signUpForm 객체가 존재하는지 확인
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "h232ch")
                .param("email", "email...")
                .param("password", "12345")
                .with(csrf())) // CSRF 설정을 하지 않을 경우 _csrf 토큰으로 인해 403 에러가 떨어짐
                // permitAll 페이지에서도 발생함 -> 서버에서 csrf 토큰을 발행해서 클라이언트에 전송하고, 클라이언트의 리쿼스트에 존재하는 csrf 토큰을 비교해서 일치하면 동작시킴
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up")) // 실패시 sign-up 페이지가 다시 보인다.
                .andExpect(unauthenticated());

    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "h232ch")
                .param("email", "h232ch@naver.com")
                .param("password", "123456789")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection()) // 응답이 redirection 인가? (정상일경우 redirection)
                .andExpect(view().name("redirect:/")) // view 이름이 redirect 인지?
                .andExpect(authenticated().withUsername("h232ch")); // username이 h232ch로 되었는지?

        Account account = accountRepository.findByEmail("h232ch@naver.com"); // account를 가져와서
        assertNotNull(account); // account가 존재하는지 확인하고
        assertNotEquals(account.getPassword(), "123456789"); // 패스워드가 해싱되었는지 비교해봄

        assertNotNull(account.getEmailCheckToken()); // email token이 존재하는지?
        assertTrue(accountRepository.existsByEmail("h232ch@naver.com")); // 실제 데이터가 입력되었는지?
        then(javaMailSender).should().send(any(SimpleMailMessage.class)); // SimpleMailMessage 타입의 send가 존재하는지? (메일을 보내는지?)

    }

}