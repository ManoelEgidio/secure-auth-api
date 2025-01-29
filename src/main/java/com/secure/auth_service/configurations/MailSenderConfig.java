package com.secure.auth_service.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailSenderConfig {

    @Value("${app.mail.host}")
    private String MAIL_HOST;

    @Value("${app.mail.port}")
    private Integer MAIL_PORT;

    @Value("${app.mail.username}")
    private String MAIL_USERNAME;

    @Value("${app.mail.password}")
    private String MAIL_PASSWORD;

    @Value("${app.mail.protocol}")
    private String MAIL_PROTOCOL;

    @Value("${app.mail.auth}")
    private String MAIL_AUTH;

    @Value("${app.mail.tls}")
    private String MAIL_TLS;

    @Bean
    JavaMailSender getJavaMailSender() {

        final var mailSender = new JavaMailSenderImpl();

        mailSender.setHost(MAIL_HOST);
        mailSender.setPort(MAIL_PORT);
        mailSender.setUsername(MAIL_USERNAME);
        mailSender.setPassword(MAIL_PASSWORD);

        final var props = mailSender.getJavaMailProperties();

        props.put("mail.transport.protocol", MAIL_PROTOCOL);
        props.put("mail.smtp.from", MAIL_USERNAME);
        props.put("mail.smtp.auth", MAIL_AUTH);
        props.put("mail.smtp.starttls.enable", MAIL_TLS);
        props.put("mail.debug", "false");

        return mailSender;
    }
}