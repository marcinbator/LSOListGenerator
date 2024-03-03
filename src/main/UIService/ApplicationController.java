package main.UIService;

import main.GroupService;
import main.models.Acolyte;
import main.models.Group;
import main.models.SundayMass;
import main.pdfService.PDFGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class ApplicationController extends JFrame {
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private final GroupService groupService;
    private JScrollPane scrollPane;
    private JButton generatePdfButton;
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
        setMinimumSize(new Dimension(800, 500));
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel monthAndYearSelector = new JPanel(new FlowLayout());
        JPanel pathSelector = new JPanel(new FlowLayout());

        initializeGroupTilesPanel();
        initializeMonthComboBox(monthAndYearSelector);
        initializeYearComboBox(monthAndYearSelector);
        initializeGenerateButton();
        displayCurrentSavePath(pathSelector);
        addChangePathButton(pathSelector);

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(monthAndYearSelector);
        mainPanel.add(pathSelector);
        mainPanel.add(scrollPane);
        mainPanel.add(generatePdfButton);

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

    //components

    private JPanel createGroupTile(Group group) {
        JPanel tilePanel = new JPanel(new BorderLayout());

        JLabel groupNumberLabel = new JLabel("Grupa " + group.getNumber());
        JTextArea acolytesTextArea = new JTextArea(5, 15);
        acolytesTextArea.setEditable(true);

        StringBuilder acolytesText = new StringBuilder();
        for (Acolyte acolyte : group.getAcolytes()) {
            acolytesText.append(acolyte.getName()).append("\n");
        }
        acolytesTextArea.setText(acolytesText.toString());

        // Dodanie komponentów do wyboru numeru grupy, dni i przycisku
        JTextField groupNumberTextField = new JTextField(String.valueOf(group.getNumber()), 10); // Ustawiamy początkową wartość jako numer grupy
        groupNumberTextField.setEditable(true); // Ustawiamy możliwość edycji

        JPanel groupNumberPanel = new JPanel();
        groupNumberPanel.add(new JLabel("Numer grupy: "));
        groupNumberPanel.add(groupNumberTextField);


        JCheckBox day1CheckBox = new JCheckBox(group.getDay1Name());
        JCheckBox day2CheckBox = new JCheckBox(group.getDay2Name());
        day1CheckBox.setSelected(true);
        day2CheckBox.setSelected(true);
        JPanel daysPanel = new JPanel();
        daysPanel.add(new JLabel("Dni: "));
        daysPanel.add(day1CheckBox);
        daysPanel.add(day2CheckBox);

        JButton confirmButton = new JButton("Zatwierdź");
        confirmButton.addActionListener(e -> saveAcolytes(acolytesTextArea.getText(), group, tilePanel));

        // Dodanie komponentów do panelu
        tilePanel.add(groupNumberPanel, BorderLayout.NORTH);
        tilePanel.add(new JScrollPane(acolytesTextArea), BorderLayout.CENTER);
        tilePanel.add(daysPanel, BorderLayout.WEST);
        tilePanel.add(confirmButton, BorderLayout.SOUTH);

        return tilePanel;
    }

    private void initializeGroupTilesPanel() {
        JPanel groupTilesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        groupTilesPanel.setBorder(BorderFactory.createTitledBorder("Grupy"));
        scrollPane = new JScrollPane(groupTilesPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        var groups = groupService.getGroups();
        for (Group group : groups) {
            JPanel tilePanel = createGroupTile(group);
            JButton deleteButton = new JButton("Usuń grupę");
            deleteButton.addActionListener(e -> {
                try {
                    groupService.removeGroup(group.getNumber());
                    groupTilesPanel.remove(tilePanel); // Usunięcie panelu grupy z kontenera
                    groupTilesPanel.revalidate();
                    groupTilesPanel.repaint();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            tilePanel.add(deleteButton, BorderLayout.EAST); // Dodanie przycisku usuwania do panelu grupy
            groupTilesPanel.add(tilePanel);
        }

        // Dodanie przycisku do dodawania nowej grupy
        JButton addGroupButton = new JButton("Dodaj nową grupę");
        addGroupButton.addActionListener(e -> {
            // Utworzenie nowej grupy z domyślnymi danymi i dodanie jej do serwisu
            Group newGroup = new Group(0, "Dzień 1", "Dzień 2", DayOfWeek.MONDAY, DayOfWeek.TUESDAY, SundayMass.S);
            try {
                groupService.addGroup(newGroup);
                // Dodanie nowego panelu dla nowej grupy
                JPanel newTilePanel = createGroupTile(newGroup);
                JButton newDeleteButton = new JButton("Usuń grupę");
                newDeleteButton.addActionListener(evt -> {
                    try {
                        groupService.removeGroup(newGroup.getNumber());
                        groupTilesPanel.remove(newTilePanel); // Usunięcie panelu grupy z kontenera
                        groupTilesPanel.revalidate();
                        groupTilesPanel.repaint();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                newTilePanel.add(newDeleteButton, BorderLayout.EAST); // Dodanie przycisku usuwania do panelu grupy
                groupTilesPanel.add(newTilePanel);
                groupTilesPanel.revalidate();
                groupTilesPanel.repaint();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        groupTilesPanel.add(addGroupButton);
    }




    private void saveAcolytes(String text, Group group, JPanel container) {
        var oldGroup = new Group(group.getNumber(), group.getDay1Name(), group.getDay2Name(), group.getDay1(), group.getDay2(), group.getSunday());
        // Pobieranie tekstu z JTextArea i zapis do listy acolytes w obiekcie Group
        String[] acolytesNames = text.split("\n");
        group.getAcolytes().clear(); // Wyczyszczenie listy acolytes
        for (String name : acolytesNames) {
            if (!name.trim().isEmpty()) { // Sprawdzenie czy nazwa nie jest pusta
                Acolyte acolyte = new Acolyte(name.trim(), group.getNumber());
                group.getAcolytes().add(acolyte); // Dodanie nowego akolity do listy
            }
        }

        // Tutaj możesz wywołać metodę serwisu do zapisu zmienionych akolitów do pliku JSON
        try {
            groupService.removeGroup(oldGroup.getNumber());
            group.setNumber(oldGroup.getNumber());
            groupService.addGroup(group);

            // Odświeżenie widoku grupy
            Component[] components = container.getComponents(); // Pobranie wszystkich komponentów z kontenera
            for (Component component : components) {
                if (component instanceof JPanel panel) { // Sprawdzenie, czy komponent to JPanel
                    Group panelGroup = (Group) panel.getClientProperty("group"); // Pobranie przypisanego obiektu Group z panelu
                    if (panelGroup != null && panelGroup.equals(group)) { // Sprawdzenie czy grupy się zgadzają
                        container.remove(panel); // Usunięcie starego panelu z kontenera
                        JPanel updatedPanel = createGroupTile(group); // Utworzenie nowego panelu z zaktualizowanymi danymi
                        container.add(updatedPanel); // Dodanie nowego panelu do kontenera
                        container.revalidate(); // Ponowne przeliczenie układu kontenera
                        container.repaint(); // Odświeżenie widoku kontenera
                        break; // Przerwanie pętli, ponieważ znaleziono i zaktualizowano odpowiedni panel
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
