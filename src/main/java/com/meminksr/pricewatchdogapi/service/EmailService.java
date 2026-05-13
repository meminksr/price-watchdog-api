package com.meminksr.pricewatchdogapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    // It automatically injects the username from the application.properties file here
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPriceDropEmail(String toEmail, String productName, BigDecimal targetPrice, BigDecimal currentPrice, String url) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("🚨 PRICE ALERT: " + productName + " It Has Dropped to the Target Price!");

        String mailText = "Hello,\n\n" +
                "You follow '" + productName + "' The price of this product has dropped!\n\n" +
                "Your Target Price: " + targetPrice + " TL\n" +
                "Current Price: " + currentPrice + " TL\n\n" +
                "Click here to purchase the product: " + url + "\n\n" +
                "Happy shopping, \nPrice Watchdog Bot";

        message.setText(mailText);

        try {
            mailSender.send(message);
            log.info("📧 The email was sent successfully -> {}", toEmail);
        } catch (Exception e) {
            log.error("❌ An error occurred while sending the email: {}", e.getMessage());
        }
    }
}