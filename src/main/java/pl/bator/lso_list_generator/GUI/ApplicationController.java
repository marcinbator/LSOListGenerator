package pl.bator.lso_list_generator.GUI;

import pl.bator.lso_list_generator.JSONDatabase.GroupService;
import pl.bator.lso_list_generator.PDFGenerator.PDFService;
import pl.bator.lso_list_generator.models.Group;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class ApplicationController extends JFrame {
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private JScrollPane scrollPane;
    private JButton generatePdfButton;
    private JButton addGroupButton;
    private JPanel groupTilesPanel;
    private JLabel pathLabel;
    private String defaultSavePath = System.getProperty("user.home") + "\\Desktop";
    private final GroupService groupService;
    private final ApplicationService applicationService;

    public ApplicationController() {
        this.groupService = new GroupService();
        this.applicationService = new ApplicationService(groupService);
        initWindow();
    }

    private void initWindow() {
        setTitle("Generator listy LSO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/logo.png"))).getImage());
        setMinimumSize(new Dimension(800, 500));
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel monthAndYearSelector = new JPanel(new FlowLayout());
        JPanel pathSelector = new JPanel(new FlowLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        initializeGroupTilesPanel();
        initializeMonthComboBox(monthAndYearSelector);
        initializeYearComboBox(monthAndYearSelector);
        initializeButtons();

        displayCurrentSavePath(pathSelector);
        addChangePathButton(pathSelector);

        buttonPanel.add(addGroupButton);
        buttonPanel.add(generatePdfButton);

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(monthAndYearSelector);
        mainPanel.add(pathSelector);
        mainPanel.add(buttonPanel);

        mainPanel.add(scrollPane);

        add(mainPanel);
    }

    // events

    public void handleChangePathClick() {
        JFileChooser fileChooser = new JFileChooser(defaultSavePath);
        fileChooser.setDialogTitle("Wybierz folder zapisu");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            defaultSavePath = fileChooser.getSelectedFile().getAbsolutePath();
            pathLabel.setText("Aktualna ścieżka: " + defaultSavePath);
        }
    }


    public void handleGenerateClick() {
        try {
            String selectedMonth = (String) monthComboBox.getSelectedItem();
            Integer selectedYear = (Integer) yearComboBox.getSelectedItem();

            int monthIndex = -1;
            String[] polishMonths = new DateFormatSymbols(new Locale("pl")).getMonths();
            for (int i = 0; i < polishMonths.length; i++) {
                assert selectedMonth != null;
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

            PDFService pdfService = new PDFService();
            for (int i = 0; i < groupService.getGroups().size(); i++) {
                pdfService.generatePdf(groupService.getGroups().get(i), month, Year.of(selectedYear), defaultSavePath);
            }
            JOptionPane.showMessageDialog(this, "Wygenerowano pomyślnie!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd generowania: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    //components


    private void initializeGroupTilesPanel() {
        groupTilesPanel = new JPanel();
        groupTilesPanel.setLayout(new BoxLayout(groupTilesPanel, BoxLayout.Y_AXIS));
        groupTilesPanel.setBorder(BorderFactory.createTitledBorder("Grupy"));
        groupTilesPanel.setMinimumSize(new Dimension(600, 400));

        scrollPane = new JScrollPane(groupTilesPanel);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        var groups = groupService.getGroups().stream().sorted(Comparator.comparingInt(Group::getNumber)).toList();
        for (Group group : groups) {
            JPanel tilePanel = applicationService.createGroupTile(group);
            groupTilesPanel.add(tilePanel);
        }

        applicationService.setGroupTilesPanel(groupTilesPanel);
    }

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
        pathLabel = new JLabel("Aktualna ścieżka: " + defaultSavePath);
        selectionPanel2.add(pathLabel);
    }

    private void addChangePathButton(JPanel selectionPanel2) {
        JButton changePathButton = new JButton("Zmień ścieżkę");
        changePathButton.addActionListener(e -> handleChangePathClick());
        selectionPanel2.add(changePathButton);
    }

    private void initializeButtons() {
        generatePdfButton = new JButton("Generuj");
        generatePdfButton.addActionListener(e -> handleGenerateClick());
        addGroupButton = new JButton("Dodaj nową grupę");
        addGroupButton.addActionListener(e -> applicationService.handleAddNewGroup(groupTilesPanel));
    }
}
