package com.trove.ticket_trove.controller.login;

import com.trove.ticket_trove.dto.jwt.response.JwtLoginResponse;
import com.trove.ticket_trove.dto.member.request.MemberAdminSignupRequest;
import com.trove.ticket_trove.dto.member.request.MemberDeleteRequest;
import com.trove.ticket_trove.dto.member.request.MemberLoginRequest;
import com.trove.ticket_trove.dto.member.request.MemberSignupRequest;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.service.login.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authentication")
public class LoginController {
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtLoginResponse> userAuthenticate(
            @RequestBody
            MemberLoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        var loginResponse = loginService
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
        loginService.adminSignup(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }
    @GetMapping("/logout")
    public ResponseEntity<HttpStatus> logout(
            HttpServletRequest httpServletRequest) {
        loginService.logout(httpServletRequest);
        return ResponseEntity.ok(HttpStatus.OK);
    }
    @PostMapping("/signup")
    public ResponseEntity<HttpStatus> signup(
            @RequestBody
            MemberSignupRequest request) {
        loginService.signup(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    //회원 탈퇴
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteUser(
            @RequestBody
            MemberDeleteRequest request
    ) {
        loginService.deleteMember(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
