package com.kms.utils.mail;

import com.kms.domain.user.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class MailUtils {
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final MailProperties mailProperties;

  public void sendVerificationEmail(User user, HttpServletRequest request) {
    // Setting dynamic verification url based on protocol, host and port
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();

    String verificationUrl =
        scheme
            + "://"
            + serverName
            + ":"
            + serverPort
            + "/auth/verify?code="
            + user.getVerificationCode();
    String emailContent =
        "Please verify your account by clicking the following link: " + verificationUrl;
    MailRequest mailRequest = new MailRequest();
    mailRequest.setToEmail(user.getEmail());
    mailRequest.setSubject("Registration Successful");
    mailRequest.setMessage(emailContent);
    mailRequest.setHTML(true); // Set to true if using HTML template

    try {
      sendMail(mailRequest);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send verification email to " + user.getEmail(), e);
    }
  }

  @Async
  public void sendMail(MailRequest request) throws MessagingException {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

    mimeMessageHelper.setFrom(mailProperties.getUsername());
    mimeMessageHelper.setTo(request.getToEmail());
    mimeMessageHelper.setSubject(request.getSubject());

    if (request.isHTML()) {
      Context context = new Context();
      /*
      content is the variable defined in our HTML template within the div tag
      */
      context.setVariable("username", request.getToEmail());
      context.setVariable("content", request.getMessage());
      String processedString = templateEngine.process("verificationEmailTemplate", context);

      mimeMessageHelper.setText(processedString, true);
    } else {
      mimeMessageHelper.setText(request.getMessage(), false);
    }

    mailSender.send(mimeMessage);
  }
}
