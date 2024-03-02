package main;

import main.models.Acolyte;
import main.models.Group;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ApplicationController extends JFrame {
    private final GroupService groupService;
    public ApplicationController() {
        this.groupService = new GroupService();
        try{
            groupService.addGroup(new Group(1, "MONDAY", "THURSDAY", "R"));
            groupService.addAcolyte(new Acolyte("Jan Kowalski", 1));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd pobierania danych: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Błąd dodawania danych: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        initWindow();
    }

    //

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
            pdfGenerator.generatePdf();
            JOptionPane.showMessageDialog(this, "Wygenerowano pomyślnie!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd generowania: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
