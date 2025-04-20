package pl.bator.lso_list_generator.view;

import pl.bator.lso_list_generator.repository.GroupJSONRepository;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class ApplicationView extends JFrame {
    private NavbarView navbarView;
    private GroupsView groupsView;

    public ApplicationView() {
        try {
            GroupJSONRepository groupJSONRepository = new GroupJSONRepository();
            navbarView = new NavbarView(groupJSONRepository, this);
            groupsView = new GroupsView(groupJSONRepository);
            initWindow();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Błąd krytyczny", javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initWindow() throws IOException {
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

        navbarView.initView(monthAndYearSelectorPanel, pathSelectorPanel, buttonPanel);
        groupsView.initView(this, groupsPanel, scrollPane, buttonPanel);

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(monthAndYearSelectorPanel);
        mainPanel.add(pathSelectorPanel);
        mainPanel.add(buttonPanel);
        mainPanel.add(scrollPane);

        add(mainPanel);
    }
}
