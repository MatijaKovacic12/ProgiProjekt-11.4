package touristAgency.manualTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import touristAgency.services.MailService;

class MailServiceManualTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "MAIL_USERNAME", matches = ".+")
    void sendsTestMailToMailtrap() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("sandbox.smtp.mailtrap.io");
        sender.setPort(2525);
        sender.setUsername(System.getenv("MAIL_USERNAME"));
        sender.setPassword(System.getenv("MAIL_PASSWORD"));
        sender.getJavaMailProperties().put("mail.smtp.auth", "true");
        sender.getJavaMailProperties().put("mail.smtp.starttls.enable", "true");

        MailService mailService = new MailService(sender);

        mailService.sendMail("ana@test.com", "Test", "Radi napokon!");
    }
}