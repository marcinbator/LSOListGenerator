package main.UIService;

import main.GroupService;
import main.models.Acolyte;
import main.models.Group;
import main.models.SundayMass;
import main.pdfService.PDFGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class ApplicationController extends JFrame {
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private final GroupService groupService;
    private JScrollPane scrollPane;
    private JButton generatePdfButton;
    private JPanel groupTilesPanel;
    private String defaultSavePath = System.getProperty("user.home") + "\\Desktop";
    String[] dayNames = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
    String[] polishDayNames = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota"};

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
        setMinimumSize(new Dimension(800, 500));
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel monthAndYearSelector = new JPanel(new FlowLayout());
        JPanel pathSelector = new JPanel(new FlowLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout()); // Dodaj nowy panel dla przycisków

        initializeGroupTilesPanel();
        initializeMonthComboBox(monthAndYearSelector);
        initializeYearComboBox(monthAndYearSelector);
        initializeGenerateButton();
        displayCurrentSavePath(pathSelector);
        addChangePathButton(pathSelector);

        JButton addGroupButton = new JButton("Dodaj nową grupę");
        addGroupButton.addActionListener(e -> {
            handleAddNewGroup(groupTilesPanel);
        });

        // Dodaj przycisk 'Dodaj nową grupę' do panelu przycisków
        buttonPanel.add(addGroupButton);
        buttonPanel.add(generatePdfButton); // Dodaj również przycisk 'Generuj' do panelu przycisków

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(monthAndYearSelector);
        mainPanel.add(pathSelector);
        mainPanel.add(buttonPanel); // Dodaj panel przycisków do głównego panelu

        mainPanel.add(scrollPane);

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

            PDFGenerator pdfGenerator = new PDFGenerator();
            for (int i = 0; i < groupService.getGroups().size(); i++) {
                pdfGenerator.generatePdf(groupService.getGroups().get(i), month, Year.of(selectedYear), defaultSavePath);
            }
            JOptionPane.showMessageDialog(this, "Wygenerowano pomyślnie!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd generowania: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSaveGroupClick(String text, Group group, JPanel container, int newGroupNumber, JCheckBox[] dayCheckBoxes) {
        var oldGroup = new Group(group.getNumber(), group.getDay1Name(), group.getDay2Name(), group.getDay1(), group.getDay2(), group.getSunday());

        String[] acolytesNames = text.split("\n");
        group.getAcolytes().clear();
        for (String name : acolytesNames) {
            if (!name.trim().isEmpty()) {
                Acolyte acolyte = new Acolyte(name.trim(), group.getNumber());
                group.getAcolytes().add(acolyte);
            }
        }

        group.setDay1(null);
        group.setDay2(null);

        for(int i=0; i<dayCheckBoxes.length; i++) {
            if(dayCheckBoxes[i].isSelected()) {
                if(group.getDay1() == null) {
                    group.setDay1(DayOfWeek.valueOf(dayNames[i]));
                    group.setDay1Name(polishDayNames[i]);
                } else {
                    group.setDay2(DayOfWeek.valueOf(dayNames[i]));
                    group.setDay2Name(polishDayNames[i]);
                }
            }
        }
        group.setNumber(newGroupNumber);

        refreshGroupContainer(group, container, oldGroup);
    }

    private void handleAddNewGroup(JPanel groupTilesPanel) {
        SundayMass sund = SundayMass.R;
        var gr = !groupService.getGroups().isEmpty() ? groupService.getGroups().get(groupService.getGroups().size() - 1) : null;
        sund = SundayMass.getNext(gr != null ? gr.getSunday() : sund);

        Group newGroup = new Group(groupService.getGroups().size()+1, "Poniedziałek", "Czwartek", DayOfWeek.MONDAY, DayOfWeek.THURSDAY, sund);
        try {
            groupService.addGroup(newGroup);
            JPanel newTilePanel = createGroupTile(newGroup);
            groupTilesPanel.add(newTilePanel);
            groupTilesPanel.revalidate();
            groupTilesPanel.repaint();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleDeleteGroupClick(JPanel groupTilesPanel, Group newGroup, JPanel newTilePanel) {
        try {
            groupService.removeGroup(newGroup.getNumber());
            groupTilesPanel.remove(newTilePanel);
            groupTilesPanel.revalidate();
            groupTilesPanel.repaint();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void refreshGroupContainer(Group group, JPanel container, Group oldGroup) {
        try {
            groupService.removeGroup(oldGroup.getNumber());
            groupService.addGroup(group);

            Component[] components = container.getComponents();
            for (Component component : components) {
                if (component instanceof JPanel panel) {
                    Group panelGroup = (Group) panel.getClientProperty("group");
                    if (panelGroup != null && panelGroup.equals(group)) {
                        container.remove(panel);
                        JPanel updatedPanel = createGroupTile(group);
                        container.add(updatedPanel);
                        container.revalidate();
                        container.repaint();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //components

    private JPanel createGroupTile(Group group) {
        JPanel tilePanel = new JPanel();
        tilePanel.setLayout(new BoxLayout(tilePanel, BoxLayout.Y_AXIS)); // Ustawienie layoutu na BoxLayout w pionie
        tilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 265)); // Ustawienie maksymalnej wysokości

        // Ustawienie pionowego paddingu
        int verticalPadding = 30;
        tilePanel.setBorder(new EmptyBorder(verticalPadding, 0, verticalPadding, 0));

        // Panel dni tygodnia
        JPanel daysPanel = new JPanel();
        daysPanel.add(new JLabel("Dni tygodnia: "));
        JCheckBox[] dayCheckBoxes = new JCheckBox[dayNames.length];

        for (int i = 0; i < dayNames.length; i++) {
            dayCheckBoxes[i] = new JCheckBox(polishDayNames[i]);
            dayCheckBoxes[i].setSelected(group.getDay1().toString().equals(dayNames[i]) || group.getDay2().toString().equals(dayNames[i]));
            daysPanel.add(dayCheckBoxes[i]);
        }

        for (JCheckBox checkBox : dayCheckBoxes) {
            checkBox.addActionListener(e -> {
                int selectedCount = 0;
                for (JCheckBox cb : dayCheckBoxes) {
                    if (cb.isSelected()) {
                        selectedCount++;
                    }
                }
                if (selectedCount > 2) {
                    ((JCheckBox) e.getSource()).setSelected(false);
                }
            });
        }

        // Pozostałe elementy
        JTextArea acolytesTextArea = new JTextArea(5, 15);
        acolytesTextArea.setEditable(true);

        StringBuilder acolytesText = new StringBuilder();
        for (Acolyte acolyte : group.getAcolytes()) {
            acolytesText.append(acolyte.getName()).append("\n");
        }
        acolytesTextArea.setText(acolytesText.toString());

        JTextField groupNumberTextField = new JTextField(String.valueOf(group.getNumber()), 10);
        groupNumberTextField.setEditable(true);

        JPanel groupNumberPanel = new JPanel();
        groupNumberPanel.add(new JLabel("Numer grupy: "));
        groupNumberPanel.add(groupNumberTextField);

        JButton confirmButton = new JButton("Zapisz zmiany");
        JButton deleteButton = new JButton("Usuń grupę");

        // Ustawienie układu FlowLayout dla panelu przycisków
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(confirmButton);
        buttonPanel.add(deleteButton);

        confirmButton.addActionListener(e -> handleSaveGroupClick(acolytesTextArea.getText(), group, tilePanel, Integer.parseInt(groupNumberTextField.getText()), dayCheckBoxes));

        // Dodanie wszystkich komponentów do panelu głównego
        tilePanel.add(groupNumberPanel); // Panel numeru grupy
        tilePanel.add(daysPanel); // Panel dni tygodnia
        tilePanel.add(new JScrollPane(acolytesTextArea)); // Panel tekstowy z acolytes
        tilePanel.add(buttonPanel); // Panel przycisków
        tilePanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Wyrównanie do lewej krawędzi

        deleteButton.addActionListener(e -> {
            handleDeleteGroupClick(groupTilesPanel, group, tilePanel);
        });

        return tilePanel;
    }



    private void initializeGroupTilesPanel() {
        groupTilesPanel = new JPanel();
        groupTilesPanel.setLayout(new BoxLayout(groupTilesPanel, BoxLayout.Y_AXIS));
        groupTilesPanel.setBorder(BorderFactory.createTitledBorder("Grupy"));
        groupTilesPanel.setMinimumSize(new Dimension(600, 500));

        scrollPane = new JScrollPane(groupTilesPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        var groups = groupService.getGroups().stream().sorted(Comparator.comparingInt(Group::getNumber)).toList();
        for (Group group : groups) {
            JPanel tilePanel = createGroupTile(group);
            groupTilesPanel.add(tilePanel);
        }
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
        JLabel pathLabel = new JLabel("Aktualna ścieżka: " + defaultSavePath);
        selectionPanel2.add(pathLabel);
    }

    private void addChangePathButton(JPanel selectionPanel2) {
        JButton changePathButton = new JButton("Zmień ścieżkę");
        changePathButton.addActionListener(e -> handleChangePathClick());
        selectionPanel2.add(changePathButton);
    }

    private void initializeGenerateButton() {
        generatePdfButton = new JButton("Generuj");
        generatePdfButton.addActionListener(e -> handleGenerateClick());
    }
}
