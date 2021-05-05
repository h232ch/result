package com.result.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity // @Id(Primary Key)를 지정하기 전까지 에러로 표기됨
@Getter @Setter @EqualsAndHashCode(of = "id")
// @EqualsAndHashCode : id에 대해서만 equals, hashcode 메서드 자동 생성
// id 한개에서만 적용하는 이유 찾아보기 (숙제)
@Builder @AllArgsConstructor @NoArgsConstructor
// AllArgsConstructor : 인자를 모두 받는 생성자
// NoArgsConstructor : 인자를 받지 않는 생성자
public class Account {

    // User Information
    @Id @GeneratedValue
    private Long id;
    
    @Column(unique = true) // 유일한 값만 입력 가능
    private String email;
    
    @Column(unique = true) // 유일한 값만 입력 가능
    private String nickname;
    private String password;
    private boolean emailVerified; // 이메일 인증이 되었는지?
    private String emailCheckToken; // 이메일 검증 시 사용할 토큰값 : DB에 저장된 값과 비교
    private LocalDateTime joinedAt; // 이메일 검증 완료 시점

    // Profile
    // String은 보통 varchar로 저장됨 varchar(255)
    private String bio;
    private String url;
    private String occupation;
    private String location;
    @Lob @Basic(fetch = FetchType.EAGER) // @Lob : varchar(255) 보다 길게 받겠다. @Basic~ : DB에서 데이터를 꺼내오는 방식을 Eager로 정함
    private String profileImage;

    // Alarm
    private boolean studyCreatedByEmail; // 스터디가 만들어진 소식을 이메일 or 웹으로 받겠다.
    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail; // 스터디 모임의 가입 신청을 이메일 or 웹으로 받겠다.
    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail; // 스터디 변경 사항을 이메일 or 웹으로 받겠다.
    private boolean studyUpdatedByWeb;


    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }
}
