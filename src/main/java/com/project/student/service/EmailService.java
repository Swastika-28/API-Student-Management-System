package com.project.student.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendCertificate(String toEmail, String studentName, byte[] pdfBytes)
            throws MessagingException {
        MimeMessage message=mailSender.createMimeMessage();
        MimeMessageHelper helper=new MimeMessageHelper(message,true);
        helper.setTo(toEmail);
        helper.setSubject("Your Course Completion Certificate");
        helper.setText("Dear "+ studentName +",\n\nCongratulations! Please find attached your certificate of completion.");
        helper.addAttachment(studentName + "_Certificate.pdf",new ByteArrayResource(pdfBytes));
        mailSender.send(message);

    }
}
