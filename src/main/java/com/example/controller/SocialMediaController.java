package com.example.controller;

import java.util.Objects;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.Account;
import com.example.entity.Message;
import com.example.service.AccountService;
import com.example.service.MessageService;
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
    private final MessageService messageService;
    
    @Autowired
    public SocialMediaController(final AccountService accountService, final MessageService messageService) {
        this.accountService = Objects.requireNonNull(accountService);
        this.messageService = Objects.requireNonNull(messageService);
    }

    @GetMapping("accounts/{account_id}/messages")
    public List<Message> getMessagesByUser(@PathVariable("account_id") int accountId) {
        return this.messageService.getMessagesByUser(accountId);
    }

    @PatchMapping("messages/{message_id}")
    public Integer handlePatchMessage(@PathVariable("message_id") int messageId, @RequestBody Message message) {
        try {
            this.messageService.updateMessage(messageId, message.getMessageText());
            return 1;
        } catch (MessageService.UpdateMessageException e) {
            switch (e.getKind()) {
                case MESSAGE_IS_TOO_LONG:
                case MESSAGE_TEXT_IS_BLANK:
                case MESSAGE_TEXT_IS_NULL:
                case NONEXISTENT_MESSAGE_ID:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                default:
                    throw new AssertionError();
            }
        }
    }

    @DeleteMapping("messages/{message_id}")
    public Integer handleDeleteMessage(@PathVariable("message_id") int messageId) {
        if (this.messageService.deleteMessage(messageId)) {
            return 1;
        } else {
            return null;
        }
    }

    @GetMapping("messages/{message_id}")
    public Message handleGetMessage(@PathVariable("message_id") int messageId) {
        return this.messageService.getMessageById(messageId).orElse(null);
    }

    @GetMapping("messages")
    public List<Message> handleGetMessages() {
        return this.messageService.getMessages();
    }

    @PostMapping("messages")
    public Message handPostMessages(@RequestBody final Message message) {
        try {
            return this.messageService.sendMessage(message);
        } catch (MessageService.SendMessageException e) {
            switch (e.getKind()) {
                case MESSAGE_IS_TOO_LONG:
                case MESSAGE_REFERS_TO_NONREAL_USER:
                case MESSAGE_TEXT_IS_BLANK:
                case MESSAGE_TEXT_IS_NULL:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                default:
                    throw new AssertionError();
            }
        }
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
