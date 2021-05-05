package com.result.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest // 전체 테스트 환경 (슬라이스 테스트가 아님), 서블릿 엔진만 띄워서 슬라이스 테스트도 가능 (알아보기 숙제)
@AutoConfigureMockMvc // MockMvc 가짜 객체를 사용하여 요청하겠다.
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("회원 가입 화면이 확인되는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk()) // response 는 Okay
                .andExpect(view().name("account/sign-up"))// View 이름이 sign-up이다.
                .andExpect(model().attributeExists("signUpForm")); // 모델 애트리뷰트에 signUpForm 객체가 존재하는지 확인
    }

}