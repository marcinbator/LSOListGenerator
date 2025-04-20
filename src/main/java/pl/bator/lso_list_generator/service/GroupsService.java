package pl.bator.lso_list_generator.service;

import org.jetbrains.annotations.NotNull;
import pl.bator.lso_list_generator.model.Group;
import pl.bator.lso_list_generator.model.Person;
import pl.bator.lso_list_generator.model.SundayMass;
import pl.bator.lso_list_generator.repository.GroupJSONRepository;
import pl.bator.lso_list_generator.util.JSONUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.Comparator;

import static pl.bator.lso_list_generator.view.NavbarView.pdfSavePath;

public class GroupsService {
    private final String[] dayNames = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
    private final String[] polishDayNames = {"poniedziałek", "wtorek", "środa", "czwartek", "piątek", "sobota"};

    private GroupJSONRepository groupJSONRepository;
    private final JSONUtil jsonUtil = new JSONUtil();

    public GroupsService(GroupJSONRepository groupJSONRepository) {
        this.groupJSONRepository = groupJSONRepository;
    }

    public void handleAddNewGroup(JPanel groupTilesPanel) {
        SundayMass sund = SundayMass.R;
        var gr = !groupJSONRepository.getGroups().isEmpty() ? groupJSONRepository.getGroups().get(groupJSONRepository.getGroups().size() - 1) : null;
        sund = SundayMass.getNext(gr != null ? gr.getSunday() : sund);

        Group newGroup = new Group(groupJSONRepository.getGroups().size() + 1, "Poniedziałek", "Czwartek", DayOfWeek.MONDAY, DayOfWeek.THURSDAY, sund);
        try {
            groupJSONRepository.addGroup(newGroup);
            JPanel newTilePanel = createGroupTile(newGroup, groupTilesPanel);
            groupTilesPanel.add(newTilePanel);
            groupTilesPanel.revalidate();
            groupTilesPanel.repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Błąd krytyczny", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    @NotNull
    public JPanel createGroupTile(Group group, JPanel groupTilesPanel) {
        JPanel tilePanel = new JPanel();
        tilePanel.setLayout(new BoxLayout(tilePanel, BoxLayout.Y_AXIS));
        tilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 265));
        tilePanel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 265));

        int verticalPadding = 25;
        tilePanel.setBorder(new EmptyBorder(verticalPadding, 0, verticalPadding, 0));

        JPanel daysPanel = new JPanel();
        daysPanel.add(new JLabel("Służenie obowiązkowe: "));
        JCheckBox[] dayCheckBoxes = new JCheckBox[dayNames.length];

        for (int i = 0; i < dayNames.length; i++) {
            dayCheckBoxes[i] = new JCheckBox(polishDayNames[i]);
            dayCheckBoxes[i].setSelected(group.getDay1().toString().equals(dayNames[i]) || group.getDay2().toString().equals(dayNames[i]));
            daysPanel.add(dayCheckBoxes[i]);
        }

        JPanel acolytesPanel = new JPanel(new FlowLayout());

        JLabel acolytesLabel = new JLabel("Podaj ministrantów, oddzielając ich za pomocą [ENTER]: ");
        acolytesPanel.add(acolytesLabel);
        JTextArea acolytesTextArea = new JTextArea(5, 15);
        acolytesTextArea.setEditable(true);

        StringBuilder acolytesText = new StringBuilder();
        for (Person person : group.getPeople()) {
            acolytesText.append(person.getName()).append("\n");
        }
        acolytesTextArea.setText(acolytesText.toString());

        JPanel groupNumberPanel = new JPanel();
        groupNumberPanel.add(new JLabel("Grupa " + group.getNumber()));

        JButton confirmButton = new JButton("Zapisz zmiany");
        confirmButton.setVisible(false);
        JButton deleteButton = new JButton("Usuń grupę");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(confirmButton);
        buttonPanel.add(deleteButton);


        acolytesTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                confirmButton.setVisible(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                confirmButton.setVisible(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                confirmButton.setVisible(true);
            }
        });

        confirmButton.addActionListener(e -> {
            try {
                handleSaveGroupClick(acolytesTextArea.getText(), group, tilePanel, dayCheckBoxes, groupTilesPanel);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            confirmButton.setVisible(false);
        });

        tilePanel.add(groupNumberPanel);
        tilePanel.add(daysPanel);
        tilePanel.add(acolytesPanel);
        tilePanel.add(new JScrollPane(acolytesTextArea));
        tilePanel.add(buttonPanel);
        tilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        deleteButton.addActionListener(e -> {
            try {
                handleDeleteGroupClick(groupTilesPanel, group, tilePanel);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        for (JCheckBox checkBox : dayCheckBoxes) {
            checkBox.addActionListener(e -> handleCheckBoxCLick(e, dayCheckBoxes, confirmButton));
        }

        return tilePanel;
    }

    public void initButtons(Component parent, @NotNull JPanel buttonPanel, @NotNull JPanel groupTilesPanel) {
        var exportJsonButton = new JButton("Eksportuj listę");
        exportJsonButton.addActionListener(e -> {
            try {
                jsonUtil.copyFile(GroupJSONRepository.dbFilePath, pdfSavePath.resolve("lso-groups.json"));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonPanel.add(exportJsonButton);

        var importJsonButton = new JButton("Importuj listę");
        importJsonButton.addActionListener(e -> {
            try {
                JFileChooser fileChooser = new JFileChooser(pdfSavePath.toString());
                fileChooser.setDialogTitle("Wybierz plik JSON");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int userSelection = fileChooser.showSaveDialog(parent);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    jsonUtil.copyFile(Path.of(fileChooser.getSelectedFile().getAbsolutePath()), GroupJSONRepository.dbFilePath);
                    refreshGroups(groupTilesPanel);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonPanel.add(importJsonButton);
    }

    //

    private void handleSaveGroupClick(@NotNull String text, @NotNull Group group, JPanel container, JCheckBox[] dayCheckBoxes, JPanel groupTilePanel) throws IOException {
        var oldGroup = new Group(group.getNumber(), group.getDay1Name(), group.getDay2Name(), group.getDay1(), group.getDay2(), group.getSunday());

        String[] acolytesNames = text.split("\n");
        group.getPeople().clear();
        for (String name : acolytesNames) {
            if (!name.trim().isEmpty()) {
                Person person = new Person(name.trim());
                group.getPeople().add(person);
            }
        }

        group.setDay1(null);
        group.setDay2(null);

        for (int i = 0; i < dayCheckBoxes.length; i++) {
            if (dayCheckBoxes[i].isSelected()) {
                if (group.getDay1() == null) {
                    group.setDay1(DayOfWeek.valueOf(dayNames[i]));
                    group.setDay1Name(polishDayNames[i]);
                } else {
                    group.setDay2(DayOfWeek.valueOf(dayNames[i]));
                    group.setDay2Name(polishDayNames[i]);
                }
            }
        }

        refreshGroupContainer(group, container, oldGroup, groupTilePanel);
    }

    private void refreshGroupContainer(Group group, @NotNull JPanel container, @NotNull Group oldGroup, JPanel groupTilePanel) throws IOException {
        groupJSONRepository.removeGroup(oldGroup.getNumber(), false);
        groupJSONRepository.addGroup(group);

        Component[] components = container.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel panel) {
                Group panelGroup = (Group) panel.getClientProperty("group");
                if (panelGroup != null && panelGroup.equals(group)) {
                    container.remove(panel);
                    JPanel updatedPanel = createGroupTile(group, groupTilePanel);
                    container.add(updatedPanel);
                    container.revalidate();
                    container.repaint();
                    break;
                }
            }
        }
    }

    private void handleDeleteGroupClick(@NotNull JPanel groupTilesPanel, @NotNull Group newGroup, JPanel newTilePanel) throws IOException {
        groupJSONRepository.removeGroup(newGroup.getNumber(), true);
        groupTilesPanel.remove(newTilePanel);
        groupTilesPanel.revalidate();
        groupTilesPanel.repaint();
        refreshGroups(groupTilesPanel);
    }

    private void refreshGroups(@NotNull JPanel container) throws IOException {
        container.removeAll();
        groupJSONRepository = new GroupJSONRepository();
        var groups = groupJSONRepository.getGroups().stream().sorted(Comparator.comparingInt(Group::getNumber)).toList();
        for (Group group : groups) {
            JPanel tilePanel = createGroupTile(group, container);
            container.add(tilePanel);
        }
        container.revalidate();
        container.repaint();
    }

    private static void handleCheckBoxCLick(ActionEvent e, JCheckBox @NotNull [] dayCheckBoxes, JButton confirmButton) {
        int selectedCount = 0;
        for (JCheckBox cb : dayCheckBoxes) {
            if (cb.isSelected()) {
                selectedCount++;
            }
        }
        if (selectedCount > 2) {
            ((JCheckBox) e.getSource()).setSelected(false);
        } else {
            confirmButton.setVisible(true);
        }
    }
}
