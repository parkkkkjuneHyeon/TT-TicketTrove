package com.trove.ticket_trove.service.login;

import com.trove.ticket_trove.dto.member.request.MemberLoginRequest;
import com.trove.ticket_trove.dto.member.request.MemberSignupRequest;
import com.trove.ticket_trove.exception.ClientErrorException;
import com.trove.ticket_trove.exception.member.MemberExistsException;
import com.trove.ticket_trove.exception.member.MemberNotFoundException;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.storage.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@SpringBootTest
class LoginServiceDynamicTest {

    @Autowired
    private LoginService loginService;
    @Autowired
    private MemberRepository memberRepository;

    @TestFactory
    Stream<DynamicTest> signupTest() {
        final List<MemberEntity> members = new ArrayList<>();
        //given

        return Stream.of(
                dynamicTest("[0] 회원가입을 합니다.", () -> {
                    //given
                    var memberSignupRequest = new MemberSignupRequest(
                            "홍길동", "aaa@naver.com",
                            "1234", "남", 34);
                    //when
                    var member = loginService.signup(memberSignupRequest);
                    members.add(member);
                    //then
                    assertNotNull(member);
                    assertEquals("aaa@naver.com", member.getEmail());
                    assertEquals("1234", member.getPassword());
                    assertEquals("남", member.getGender());
                    assertEquals(34, member.getAge());
                }),
                dynamicTest("[0] email이 존재합니다.", () -> {

                    //given
                    var existMember = memberRepository.save(members.get(0));
                    var memberSignupRequest = new MemberSignupRequest(
                            "홍길동", "aaa@naver.com",
                            "1234", "남", 34);
                    //when
                    assertThatThrownBy(() -> loginService.signup(memberSignupRequest))
                    .isInstanceOf(MemberExistsException.class)
                            .hasMessage("email : " +
                                    memberSignupRequest.email() +
                                    " already exists");
                    //then
                    assertEquals(existMember.getEmail(), memberSignupRequest.email());
                }),
                dynamicTest("[1] 로그인을 합니다.", () -> {
                    //given
                    var loginRequest = new MemberLoginRequest(
                            members.get(0).getEmail(),members.get(0).getPassword());
                    //when
                    var memberEntity = loginService.authenticate(loginRequest);
                    //then
                    assertNotNull(memberEntity);
                    assertEquals(loginRequest.email(), memberEntity.getEmail());
                    assertEquals(loginRequest.password(), memberEntity.getPassword());
                }),
                dynamicTest("[1] 로그인 아이디가 달라 실패 합니다.", () -> {
                    //given
                    var loginRequest = new MemberLoginRequest("aa@naver.com", "1234");
                    //when & then
                    assertThatThrownBy(() -> loginService.authenticate(loginRequest))
                            .isInstanceOf(MemberNotFoundException.class)
                            .hasMessage("아이디나 비밀번호가 맞지 않습니다.");
                }),
                dynamicTest("[1] 로그인 비밀번호가 달라 실패 합니다.", () -> {
                    //given
                    var loginRequest = new MemberLoginRequest("aaa@naver.com", "12345");
                    //when & then
                    assertThatThrownBy(() -> loginService.authenticate(loginRequest))
                            .isInstanceOf(ClientErrorException.class)
                            .hasMessage("아이디나 비밀번호가 맞지 않습니다.");
                })
        );

    }
}