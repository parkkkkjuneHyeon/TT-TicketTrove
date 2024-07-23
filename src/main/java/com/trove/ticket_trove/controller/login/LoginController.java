package com.trove.ticket_trove.controller.login;

import com.trove.ticket_trove.dto.member.request.MemberLoginRequest;
import com.trove.ticket_trove.dto.member.request.MemberSignupRequest;
import com.trove.ticket_trove.service.login.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authentication")
public class LoginController {
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<HttpStatus> authenticate(
            @RequestBody
            MemberLoginRequest request) {
        loginService.authenticate(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<HttpStatus> signup(
            @RequestBody
            MemberSignupRequest request) {
        loginService.signup(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
