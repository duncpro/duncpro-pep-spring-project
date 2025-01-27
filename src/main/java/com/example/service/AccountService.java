package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import com.example.entity.Account;
import com.example.repository.AccountRepository;
import java.util.Objects;

@Service
public class AccountService {

    private final AccountRepository repository;

    @Autowired
    public AccountService(final AccountRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public static enum RegistrationExceptionKind {
        USERNAME_IS_NULL,
        USERNAME_IS_BLANK,
        PASSWORD_TOO_SHORT,
        USERNAME_IS_RESERVED,
        PASSWORD_IS_NULL
    }

    public static class RegistrationException extends Exception {
        private final RegistrationExceptionKind kind;

        RegistrationException(final RegistrationExceptionKind kind) {
            this.kind = Objects.requireNonNull(kind);
        }

        public RegistrationExceptionKind getKind() { 
            return this.kind;
        }
    }

    @Transactional
    public Account register(final Account template) throws RegistrationException {
        Objects.requireNonNull(template);

        final String username = template.getUsername();
        if (username == null) throw new RegistrationException(RegistrationExceptionKind.USERNAME_IS_NULL);
        if (username.isBlank()) throw new RegistrationException(RegistrationExceptionKind.USERNAME_IS_BLANK);

        if (this.repository.isUsernameReserved(username)) 
            throw new RegistrationException(RegistrationExceptionKind.USERNAME_IS_RESERVED);
        
        final String password = template.getPassword();
        if (password == null) throw new RegistrationException(RegistrationExceptionKind.PASSWORD_IS_NULL);
        if (password.length() < 4) throw new RegistrationException(RegistrationExceptionKind.PASSWORD_TOO_SHORT);

        return this.repository.save(template);
    }

    public static enum LoginExceptionKind {
        NO_USERNAME_PROVIDED,
        NO_PASSWORD_PROVIDED,
        NO_ACCOUNT_FOUND
    }

    public static class LoginException extends Exception {
        private final LoginExceptionKind kind;
        
        LoginException(final LoginExceptionKind kind) {
            this.kind = Objects.requireNonNull(kind);
        }

        public LoginExceptionKind getKind() {
            return this.kind;
        }
    }

    @Transactional
    public Account login(final Account credentials) throws LoginException {
        final String username = credentials.getUsername();
        if (username == null) throw new LoginException(LoginExceptionKind.NO_USERNAME_PROVIDED); 
        
        final String password = credentials.getPassword();
        if (password == null) throw new LoginException(LoginExceptionKind.NO_PASSWORD_PROVIDED);

        final Example<Account> example = Example.of(new Account(
            credentials.getUsername(),
            credentials.getPassword()
        ));
        example.getMatcher().withIgnoreNullValues();

        return this.repository.findOne(example)
            .orElseThrow(() -> new LoginException(LoginExceptionKind.NO_ACCOUNT_FOUND));
    }
}
