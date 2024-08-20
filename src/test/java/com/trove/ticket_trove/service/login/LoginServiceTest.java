//package com.trove.ticket_trove.service.login;
//
//import com.trove.ticket_trove.dto.member.request.MemberSignupRequest;
//import com.trove.ticket_trove.model.entity.member.MemberEntity;
//import com.trove.ticket_trove.model.storage.member.MemberRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@SpringBootTest
//class LoginServiceTest {
//
//    @Autowired
//    private LoginService loginService;
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @DisplayName("회원가입을 성공한다.")
//    @Test
//    void signup() {
//        //given
//        MemberSignupRequest memberSignupRequest = new MemberSignupRequest(
//                "홍길동", "aaa@naver.com",
//                "1234", "남", 34);
//
//        //when
//        var memberEntity = loginService.signup(memberSignupRequest);
//        //then
//        assertNotNull(memberEntity);
//        assertEquals("aaa@naver.com", memberEntity.getEmail());
//        assertEquals("1234", memberEntity.getPassword());
//        assertEquals("남", memberEntity.getGender());
//        assertEquals(34, memberEntity.getAge());
//
//    }
//}