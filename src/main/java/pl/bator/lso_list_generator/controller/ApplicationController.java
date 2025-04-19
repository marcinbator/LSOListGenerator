package pl.bator.lso_list_generator.controller;

import pl.bator.lso_list_generator.repository.GroupJSONRepository;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class ApplicationController extends JFrame {
    private final NavbarController navbarController;
    private final GroupsPanelController groupsPanelController;
    private GroupJSONRepository groupJSONRepository;

    public ApplicationController() {
        try {
            groupJSONRepository = new GroupJSONRepository();
        } catch (IOException e) {
            showErrorAndExit("Nie można otworzyć pliku JSON.");
        }
        navbarController = new NavbarController(groupJSONRepository, this);
        groupsPanelController = new GroupsPanelController(groupJSONRepository);
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
        JPanel monthAndYearSelectorPanel = new JPanel(new FlowLayout());
        JPanel pathSelectorPanel = new JPanel(new FlowLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JPanel groupsPanel = new JPanel(new FlowLayout());
        JScrollPane scrollPane = new JScrollPane(groupsPanel);

        navbarController.initView(monthAndYearSelectorPanel, pathSelectorPanel, buttonPanel);
        groupsPanelController.initView(groupsPanel, scrollPane, buttonPanel);

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(monthAndYearSelectorPanel);
        mainPanel.add(pathSelectorPanel);
        mainPanel.add(buttonPanel);
        mainPanel.add(scrollPane);

        add(mainPanel);
    }

    public void showErrorAndExit(String message) {
        JOptionPane.showMessageDialog(null, message, "Błąd krytyczny", javax.swing.JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
