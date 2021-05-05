package com.result.account;

import com.result.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true) // 성능 향상을 위해 readOnly true 사용 (숙제 사용이유 찾아보기)
public interface AccountRepository extends JpaRepository<Account, Long> { // Account 객체의 Repository이고 id값은 Long이다.


    boolean existsByNickname(String nickname); // 자동으로 해당 조건에 맞는 메서드 생성
    boolean existsByEmail(String email);
}
