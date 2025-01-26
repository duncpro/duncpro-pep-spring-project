package com.example.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.Account;
import com.example.service.AccountService;
import com.example.service.AccountService.LoginException;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller using Spring. The endpoints you will need can be
 * found in readme.md as well as the test cases. You be required to use the @GET/POST/PUT/DELETE/etc Mapping annotations
 * where applicable as well as the @ResponseBody and @PathVariable annotations. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
@Controller
@ResponseBody
public class SocialMediaController {
    private final AccountService accountService;
    
    @Autowired
    public SocialMediaController(final AccountService accountService) {
        this.accountService = Objects.requireNonNull(accountService);
    }

    @PostMapping("login")
    public Account handlePostLogin(@RequestBody final Account reqbody) {
        try {
            return this.accountService.login(reqbody);
        } catch (LoginException e) {
            switch (e.getKind()) {
                case NO_ACCOUNT_FOUND:
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                case NO_PASSWORD_PROVIDED:
                case NO_USERNAME_PROVIDED:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                default:
                    throw new AssertionError();

            }
        }
    }

    @PostMapping("register")
    public Account handlePostRegister(@RequestBody final Account template) {
        try {
            return this.accountService.register(template);
        } catch (AccountService.RegistrationException e) {
            switch (e.getKind()) {
                case PASSWORD_IS_NULL:
                case PASSWORD_TOO_SHORT:
                case USERNAME_IS_BLANK:
                case USERNAME_IS_NULL:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                case USERNAME_IS_RESERVED:
                    throw new ResponseStatusException(HttpStatus.CONFLICT);
                default:
                    throw new AssertionError();
            }
        }
    }
}
