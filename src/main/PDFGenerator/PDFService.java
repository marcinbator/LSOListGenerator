package main.PDFGenerator;

import com.lowagie.text.pdf.BaseFont;
import main.models.Day;
import main.models.Group;
import main.models.SundayMass;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PDFService {
    public void generatePdf(Group group, Month month, Year year, String path) throws IOException {
        String html = parseThymeleafTemplate(group, month, year);

        String outputFolder = path +"/lista_"+month.getDisplayName(TextStyle.FULL_STANDALONE, new Locale("PL"))+"_"+year+"_grupa_"+group.getNumber()+".pdf";
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

        YearMonth yearMonthObject = YearMonth.of(year.getValue(), month.getValue());
        int daysInMonth = yearMonthObject.lengthOfMonth();

        List<Day> days = new ArrayList<>();
        SundayMass sundayMass = group.getSunday();
        for (int i = 1; i <= daysInMonth; i++) {
            Day day = new Day();
            day.setDayOfMonth(i);
            day.setDayOfWeek(yearMonthObject.atDay(i).getDayOfWeek());
            day.setObligatory(day.getDayOfWeek().equals(group.getDay1()) || day.getDayOfWeek().equals(group.getDay2()));
            day.setSunday(day.getDayOfWeek().equals(DayOfWeek.SUNDAY));
            day.setSundayMass(sundayMass);
            sundayMass = SundayMass.getNext(sundayMass);
            days.add(day);
        }

        context.setVariable("days", days);
        context.setVariable("date", LocalDateTime.now());

        return templateEngine.process("thymeleaf_template", context);
    }

}
