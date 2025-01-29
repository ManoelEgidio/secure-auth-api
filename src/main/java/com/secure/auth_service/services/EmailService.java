package com.secure.auth_service.services;

import com.secure.auth_service.dtos.EmailDTO;
import com.secure.auth_service.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(EmailDTO email) {

        try {
            final var message = new SimpleMailMessage();

            message.setTo(email.to());
            message.setSubject(email.subject());
            message.setText(email.text());

            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Erro ao enviar e-mail para {} \t {}", email.to(), ex.getMessage());
            throw new CustomException("Erro ao enviar e-mail para " + email.to());
        }
    }
}