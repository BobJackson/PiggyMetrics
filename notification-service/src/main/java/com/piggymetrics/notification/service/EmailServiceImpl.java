package com.piggymetrics.notification.service;

import com.piggymetrics.notification.domain.NotificationType;
import com.piggymetrics.notification.domain.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.text.MessageFormat;
import java.util.Objects;

@Service
@RefreshScope
public class EmailServiceImpl implements EmailService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private Environment env;

    @Override
    public void send(NotificationType type, Recipient recipient, String attachment) throws MessagingException {

        final String subject = Objects.requireNonNull(env.getProperty(type.getSubject()));
        final String text = MessageFormat.format(Objects.requireNonNull(env.getProperty(type.getText())),
                recipient.getAccountName());

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(recipient.getEmail());
        helper.setSubject(subject);
        helper.setText(text);

        if (StringUtils.hasLength(attachment)) {
            helper.addAttachment(Objects.requireNonNull(env.getProperty(type.getAttachment())),
                    new ByteArrayResource(attachment.getBytes()));
        }

        mailSender.send(message);

        log.info("{} email notification has been send to {}", type, recipient.getEmail());
    }
}
