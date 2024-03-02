import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ApplicationController extends JFrame {
    public ApplicationController() {
        setTitle("Generator listy LSO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JButton generatePdfButton = new JButton("Generuj");
        generatePdfButton.addActionListener(e -> generatePdf());

        mainPanel.add(generatePdfButton, BorderLayout.CENTER);

        add(mainPanel);
    }

    //

    private void generatePdf() {
        try {
            PDFGenerator pdfGenerator = new PDFGenerator();
            pdfGenerator.generatePdf();
            JOptionPane.showMessageDialog(this, "Wygenerowano pomyślnie!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd generowania: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
