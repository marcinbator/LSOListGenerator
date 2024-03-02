import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class initApp extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            initApp app = new initApp();
            app.setVisible(true);
        });
    }

    private initApp() {
        setTitle("Thymeleaf to PDF Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JButton generatePdfButton = new JButton("Generate PDF");
        generatePdfButton.addActionListener(e -> generatePdf());

        mainPanel.add(generatePdfButton, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void generatePdf() {
        String html = parseThymeleafTemplate();

        try {
            String outputFolder = System.getProperty("user.home") + File.separator + "Desktop/thymeleaf.pdf";
            OutputStream outputStream = new FileOutputStream(outputFolder);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);

            outputStream.close();
            JOptionPane.showMessageDialog(this, "PDF generated successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String parseThymeleafTemplate() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("to", "dupa.com");

        return templateEngine.process("thymeleaf_template", context);
    }
}