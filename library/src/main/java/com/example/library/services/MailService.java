package com.example.library.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private static final String SUBJECT = "Book API";
    private final JavaMailSender sender;
    @Value("${mail.from}")
    private String from;

    @Async
    public void sendConfirmation(String address, String text) {
        log.info("sending...");

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(from);
        mailMessage.setTo(address);
        mailMessage.setSubject(SUBJECT);
        mailMessage.setText(text);

        sender.send(mailMessage);
        log.info("send");
    }
}