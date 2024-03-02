package main.UIService;

import main.GroupService;
import main.pdfService.PDFGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class ApplicationController extends JFrame {
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private final GroupService groupService;
    private String defaultSavePath = System.getProperty("user.home") + "\\Desktop";

    public ApplicationController() {
        this.groupService = new GroupService();
        initWindow();
    }

    private void initWindow() {
        setTitle("Generator listy LSO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 300);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/logo.png"))).getImage());

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel monthAndYearSelector = new JPanel(new FlowLayout());
        JPanel pathSelector = new JPanel(new FlowLayout());

        initializeMonthComboBox(monthAndYearSelector);
        initializeYearComboBox(monthAndYearSelector);
        displayCurrentSavePath(pathSelector);
        addChangePathButton(pathSelector);

        mainPanel.add(monthAndYearSelector, BorderLayout.NORTH);
        mainPanel.add(pathSelector, BorderLayout.CENTER);

        addGeneratePdfButton(mainPanel);

        add(mainPanel);
    }

    // events

    private void handleChangePathClick() {
        JFileChooser fileChooser = new JFileChooser(defaultSavePath);
        fileChooser.setDialogTitle("Wybierz folder zapisu");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            defaultSavePath = fileChooser.getSelectedFile().getAbsolutePath();
        }
    }

    private void handleGenerateClick() {
        try {
            String selectedMonth = (String) monthComboBox.getSelectedItem();
            Integer selectedYear = (Integer) yearComboBox.getSelectedItem();

            int monthIndex = -1;
            String[] polishMonths = new DateFormatSymbols(new Locale("pl")).getMonths();
            for (int i = 0; i < polishMonths.length; i++) {
                if (selectedMonth.equals(polishMonths[i])) {
                    monthIndex = i;
                    break;
                }
            }

            if (monthIndex == -1) {
                JOptionPane.showMessageDialog(this, "Błąd: Nie można odnaleźć wybranego miesiąca.", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Month month = Month.of(monthIndex + 1);

            PDFGenerator pdfGenerator = new PDFGenerator();
            for (int i = 0; i < groupService.getGroups().size(); i++) {
                pdfGenerator.generatePdf(groupService.getGroups().get(i), month, Year.of(selectedYear), defaultSavePath);
            }
            JOptionPane.showMessageDialog(this, "Wygenerowano pomyślnie!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd generowania: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //components

    private void initializeMonthComboBox(JPanel selectionPanel1) {
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        String[] polishMonths = new DateFormatSymbols(new Locale("pl")).getMonths();
        monthComboBox = new JComboBox<>(Arrays.copyOf(polishMonths, polishMonths.length - 1));
        monthComboBox.setSelectedItem(polishMonths[currentMonth - 1]);
        selectionPanel1.add(new JLabel("Generuj dla miesiąca: "));
        selectionPanel1.add(monthComboBox);
    }

    private void initializeYearComboBox(JPanel selectionPanel1) {
        int currentYear = Year.now().getValue();
        Integer[] years = new Integer[10];
        for (int i = 0; i < years.length; i++) {
            years[i] = currentYear - 5 + i;
        }
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem(currentYear);
        selectionPanel1.add(new JLabel("roku: "));
        selectionPanel1.add(yearComboBox);
    }

    private void displayCurrentSavePath(JPanel selectionPanel2) {
        JLabel pathLabel = new JLabel("Aktualna ścieżka: " + defaultSavePath);
        selectionPanel2.add(pathLabel);
    }

    private void addChangePathButton(JPanel selectionPanel2) {
        JButton changePathButton = new JButton("Zmień ścieżkę");
        changePathButton.addActionListener(e -> handleChangePathClick());
        selectionPanel2.add(changePathButton);
    }

    private void addGeneratePdfButton(JPanel mainPanel) {
        JButton generatePdfButton = new JButton("Generuj");
        generatePdfButton.addActionListener(e -> handleGenerateClick());
        generatePdfButton.setPreferredSize(new Dimension(100, 30));
        mainPanel.add(generatePdfButton, BorderLayout.SOUTH);
    }
}
