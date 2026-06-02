package com.project.student.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.project.student.dto.StudentReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PdfService {
    private final TemplateEngine templateEngine;

    public byte[] generateCertificate(String studentName, String courseTitle, Double grade, String date) {
        Context context = new Context();
        context.setVariable("name", studentName);
        context.setVariable("course", courseTitle);
        context.setVariable("grade", grade);
        context.setVariable("date", date);
        String htmlContent = templateEngine.process("Certificate_template", context);
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlContent, target);
        return target.toByteArray();
    }
    public byte[] generateTranscriptPdf(List<StudentReport> reports){
        Context context=new Context();
        context.setVariable("reports",reports);
        String html=templateEngine.process("transcript_template",context);
        try(ByteArrayOutputStream os=new ByteArrayOutputStream()){
            PdfRendererBuilder builder=new PdfRendererBuilder();
            builder.withHtmlContent(html, "/");            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }catch(Exception e){
            throw new RuntimeException("PDF Generation failed",e);

        }


    }

}
