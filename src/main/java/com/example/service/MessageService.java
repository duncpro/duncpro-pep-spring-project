package com.example.service;

import java.util.Objects;
import java.util.List;
import java.util.Optional;

import org.h2.command.dml.Update;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.Account;
import com.example.entity.Message;
import com.example.repository.AccountRepository;
import com.example.repository.MessageRepository;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final AccountRepository accountRepository;

    public MessageService(
        final MessageRepository messageRepository,
        final AccountRepository accountRepository
    ) {
        this.messageRepository = Objects.requireNonNull(messageRepository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
    }

    public static enum SendMessageExceptionKind {
        MESSAGE_TEXT_IS_NULL,
        MESSAGE_TEXT_IS_BLANK,
        MESSAGE_IS_TOO_LONG,
        MESSAGE_REFERS_TO_NONREAL_USER
    }

    public static class SendMessageException extends Exception {
        private final SendMessageExceptionKind kind;

        SendMessageException(final SendMessageExceptionKind kind) {
            this.kind = Objects.requireNonNull(kind);
        }

        public SendMessageExceptionKind getKind() {
            return this.kind;
        }
    }

    @Transactional
    public Message sendMessage(final Message message) throws SendMessageException {
        Objects.requireNonNull(message);

        if (message.getMessageText() == null) 
            throw new SendMessageException(SendMessageExceptionKind.MESSAGE_TEXT_IS_NULL);
        if (message.getMessageText().isBlank())
            throw new SendMessageException(SendMessageExceptionKind.MESSAGE_TEXT_IS_BLANK);
        if (message.getMessageText().length() >= 255)
            throw new SendMessageException(SendMessageExceptionKind.MESSAGE_IS_TOO_LONG);

        final Example<Account> poster = Example.of(new Account());
        poster.getProbe().setAccountId(message.getPostedBy());
        poster.getMatcher().withIgnoreNullValues();
        final boolean isRealPostedBy = this.accountRepository.count(poster) > 0;
        if (!isRealPostedBy) throw new SendMessageException(SendMessageExceptionKind.MESSAGE_REFERS_TO_NONREAL_USER);

        return this.messageRepository.save(message);
    }

    public static enum UpdateMessageExceptionKind {
        MESSAGE_TEXT_IS_NULL,
        MESSAGE_TEXT_IS_BLANK,
        MESSAGE_IS_TOO_LONG,
        NONEXISTENT_MESSAGE_ID
    }

    public static class UpdateMessageException extends Exception {
        private final UpdateMessageExceptionKind kind;

        UpdateMessageException(final UpdateMessageExceptionKind kind) {
            this.kind = Objects.requireNonNull(kind);
        }

        public UpdateMessageExceptionKind getKind() {
            return this.kind;
        }
    }

    @Transactional
    public void updateMessage(final int messageId, final String text) throws UpdateMessageException {
        if (text == null) 
            throw new UpdateMessageException(UpdateMessageExceptionKind.MESSAGE_TEXT_IS_NULL);
        if (text.isBlank())
            throw new UpdateMessageException(UpdateMessageExceptionKind.MESSAGE_TEXT_IS_BLANK);
        if (text.length() >= 255)
            throw new UpdateMessageException(UpdateMessageExceptionKind.MESSAGE_IS_TOO_LONG);

        final Message message = this.messageRepository.findById(messageId).orElse(null);
        if (message == null)
            throw new UpdateMessageException(UpdateMessageExceptionKind.NONEXISTENT_MESSAGE_ID);

        message.setMessageText(text);    
        this.messageRepository.save(message);
    }

    public List<Message> getMessages() {
        return this.messageRepository.findAll();
    }

    public Optional<Message> getMessageById(int id) {
        return this.messageRepository.findById(id);
    }

    @Transactional
    public boolean deleteMessage(int id) {
        final boolean exists = this.messageRepository.findById(id).isPresent();
        if (exists) this.messageRepository.deleteById(id);
        return exists;
    }

    public List<Message> getMessagesByUser(int userId) {
        final Example<Message> example = Example.of(new Message());
        example.getProbe().setPostedBy(userId);
        example.getMatcher().withIgnoreNullValues();
        return this.messageRepository.findAll(example);
    }
    
}
