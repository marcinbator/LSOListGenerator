package pl.bator.lso_list_generator.controller;

import org.jetbrains.annotations.NotNull;
import pl.bator.lso_list_generator.repository.GroupJSONRepository;
import pl.bator.lso_list_generator.service.PDFGenerationService;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.text.DateFormatSymbols;
import java.time.Year;
import java.util.Arrays;
import java.util.Locale;

public class NavbarController {
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private JLabel pathLabel;
    private Path pdfSavePath = Path.of(System.getProperty("user.home"), "Desktop");

    private final PDFGenerationService pdfGenerationService;
    private final Component parent;

    public NavbarController(GroupJSONRepository groupJSONRepository, Component parent) {
        this.pdfGenerationService = new PDFGenerationService(groupJSONRepository);
        this.parent = parent;
    }

    public void initView(JPanel monthAndYearSelectorPanel, JPanel pathSelectorPanel, JPanel buttonPanel) {
        initializeMonthComboBox(monthAndYearSelectorPanel);
        initializeYearComboBox(monthAndYearSelectorPanel);
        displayCurrentSavePath(pathSelectorPanel);
        addChangePathButton(pathSelectorPanel);
        initializeButtons(buttonPanel);
    }

    private void initializeMonthComboBox(@NotNull JPanel monthAndYearSelectorPanel) {
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        String[] polishMonths = new DateFormatSymbols(new Locale("pl")).getMonths();
        monthComboBox = new JComboBox<>(Arrays.copyOf(polishMonths, polishMonths.length - 1));
        monthComboBox.setSelectedItem(polishMonths[currentMonth - 1]);
        monthAndYearSelectorPanel.add(new JLabel("Generuj dla miesiąca: "));
        monthAndYearSelectorPanel.add(monthComboBox);
    }

    private void initializeYearComboBox(JPanel monthAndYearSelectorPanel) {
        int currentYear = Year.now().getValue();
        Integer[] years = new Integer[10];
        for (int i = 0; i < years.length; i++) {
            years[i] = currentYear - 5 + i;
        }
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem(currentYear);
        monthAndYearSelectorPanel.add(new JLabel("roku: "));
        monthAndYearSelectorPanel.add(yearComboBox);
    }

    private void displayCurrentSavePath(@NotNull JPanel pathSelectorPanel) {
        pathLabel = new JLabel("Aktualna ścieżka: " + pdfSavePath);
        pathSelectorPanel.add(pathLabel);
    }

    private void addChangePathButton(@NotNull JPanel pathSelectorPanel) {
        JButton changePathButton = new JButton("Zmień ścieżkę");
        changePathButton.addActionListener(e -> changeSavePath());
        pathSelectorPanel.add(changePathButton);
    }

    private void initializeButtons(@NotNull JPanel buttonPanel) {
        var generatePdfButton = new JButton("Generuj");
        generatePdfButton.addActionListener(e -> handleGenerateClick());
        buttonPanel.add(generatePdfButton);
    }

    private void changeSavePath() {
        JFileChooser fileChooser = new JFileChooser(pdfSavePath.toString());
        fileChooser.setDialogTitle("Wybierz folder zapisu");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int userSelection = fileChooser.showSaveDialog(parent);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            pdfSavePath = Path.of(fileChooser.getSelectedFile().getAbsolutePath());
            pathLabel.setText("Aktualna ścieżka: " + pdfSavePath);
        }
    }

    private void handleGenerateClick() {
        try {
            var selectedMonth = (String) monthComboBox.getSelectedItem();
            var selectedYear = (Integer) yearComboBox.getSelectedItem();
            if (selectedMonth == null || selectedYear == null) {
                JOptionPane.showMessageDialog(parent, "Błędny miesiąc lub rok.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            pdfGenerationService.handleGenerateClick(selectedMonth, selectedYear, pdfSavePath);
            JOptionPane.showMessageDialog(parent, "Wygenerowano pomyślnie!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Błąd generowania: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
