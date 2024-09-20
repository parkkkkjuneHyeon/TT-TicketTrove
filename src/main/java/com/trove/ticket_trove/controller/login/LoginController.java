package com.trove.ticket_trove.controller.login;

import com.trove.ticket_trove.dto.jwt.response.JwtLoginResponse;
import com.trove.ticket_trove.dto.member.request.MemberAdminSignupRequest;
import com.trove.ticket_trove.dto.member.request.MemberDeleteRequest;
import com.trove.ticket_trove.dto.member.request.MemberLoginRequest;
import com.trove.ticket_trove.dto.member.request.MemberSignupRequest;
import com.trove.ticket_trove.service.auth.LoginReadService;
import com.trove.ticket_trove.service.auth.LoginWriteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authentication")
@Slf4j
public class LoginController {
    private final LoginReadService loginReadService;
    private final LoginWriteService loginWriteService;

    public LoginController(
            LoginReadService loginReadService,
            LoginWriteService loginWriteService) {
        this.loginReadService = loginReadService;
        this.loginWriteService = loginWriteService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtLoginResponse> userAuthenticate(
            @RequestBody
            MemberLoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        var loginResponse = loginReadService
                .authenticate(
                        request,
                        httpServletRequest,
                        httpServletResponse);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/admin-signup")
    public ResponseEntity<HttpStatus> adminSignup(
            @RequestBody
            MemberAdminSignupRequest request) {
        loginWriteService.adminSignup(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<HttpStatus> logout(
            HttpServletRequest httpServletRequest) {
        loginReadService.logout(httpServletRequest);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<HttpStatus> signup(
            @RequestBody
            MemberSignupRequest request) {
        loginWriteService.signup(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    //회원 탈퇴
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteUser(
            @RequestBody
            MemberDeleteRequest request
    ) {

        log.info("들어옴.");
        loginWriteService.deleteMember(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
