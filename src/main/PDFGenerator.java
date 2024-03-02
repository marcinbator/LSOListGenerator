package main;

import com.lowagie.text.pdf.BaseFont;
import main.models.Group;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.Locale;

public class PDFGenerator {
    public void generatePdf(Group group, Month month, Year year) throws IOException {
        String html = parseThymeleafTemplate(group, month, year);

        String outputFolder = System.getProperty("user.home") + File.separator + "Desktop/thymeleaf.pdf";
        OutputStream outputStream = new FileOutputStream(outputFolder);

        ITextRenderer renderer = new ITextRenderer();

        renderer.getFontResolver().addFont("ARIALUNI.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        renderer.setDocumentFromString(html);
        renderer.layout();

        renderer.createPDF(outputStream);

        outputStream.close();
    }

    //

    public String parseThymeleafTemplate(Group group, Month month, Year year) {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("group", group);
        context.setVariable("month", month.getDisplayName(TextStyle.FULL_STANDALONE, new Locale("PL")));
        context.setVariable("year", year);

        return templateEngine.process("thymeleaf_template", context);
    }

}
