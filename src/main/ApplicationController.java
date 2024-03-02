package main;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.Month;
import java.time.Year;

public class ApplicationController extends JFrame {
    private Month month;
    private Year year;
    private final GroupService groupService;
    public ApplicationController() {
        this.groupService = new GroupService();
        initSampleValues();
        initWindow();
    }

    //

    private void initSampleValues() {
        month = Month.MARCH;
        year = Year.of(2024);
    }

    private void initWindow() {
        setTitle("Generator listy LSO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JButton generatePdfButton = new JButton("Generuj");
        generatePdfButton.addActionListener(e -> handleGenerateClick());

        mainPanel.add(generatePdfButton, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void handleGenerateClick() {
        try {
            PDFGenerator pdfGenerator = new PDFGenerator();
            pdfGenerator.generatePdf(groupService.getGroups().get(0), month, year);
            JOptionPane.showMessageDialog(this, "Wygenerowano pomyślnie!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd generowania: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
