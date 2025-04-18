package pl.bator.lso_list_generator.GUI;

import lombok.Setter;
import pl.bator.lso_list_generator.JSONDatabase.GroupService;
import pl.bator.lso_list_generator.models.Acolyte;
import pl.bator.lso_list_generator.models.Group;
import pl.bator.lso_list_generator.models.SundayMass;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.DayOfWeek;

public class ApplicationService {
    private final String[] dayNames = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
    private final String[] polishDayNames = {"poniedziałek", "wtorek", "środa", "czwartek", "piątek", "sobota"};
    private final GroupService groupService;
    @Setter
    private JPanel groupTilesPanel;

    public ApplicationService(GroupService groupService) {
        this.groupService = groupService;
    }

    public JPanel createGroupTile(Group group) {
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
        for (Acolyte acolyte : group.getAcolytes()) {
            acolytesText.append(acolyte.getName()).append("\n");
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
            handleSaveGroupClick(acolytesTextArea.getText(), group, tilePanel, dayCheckBoxes);
            confirmButton.setVisible(false);
        });

        tilePanel.add(groupNumberPanel);
        tilePanel.add(daysPanel);
        tilePanel.add(acolytesPanel);
        tilePanel.add(new JScrollPane(acolytesTextArea));
        tilePanel.add(buttonPanel);
        tilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        deleteButton.addActionListener(e -> handleDeleteGroupClick(groupTilesPanel, group, tilePanel));

        for (JCheckBox checkBox : dayCheckBoxes) {
            checkBox.addActionListener(e -> ApplicationService.handleCheckBoxCLick(e, dayCheckBoxes, confirmButton));
        }

        return tilePanel;
    }

    public void handleAddNewGroup(JPanel groupTilesPanel) {
        SundayMass sund = SundayMass.R;
        var gr = !groupService.getGroups().isEmpty() ? groupService.getGroups().get(groupService.getGroups().size() - 1) : null;
        sund = SundayMass.getNext(gr != null ? gr.getSunday() : sund);

        Group newGroup = new Group(groupService.getGroups().size() + 1, "Poniedziałek", "Czwartek", DayOfWeek.MONDAY, DayOfWeek.THURSDAY, sund);
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

    //

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

    private static void handleCheckBoxCLick(ActionEvent e, JCheckBox[] dayCheckBoxes, JButton confirmButton) {
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

    private void handleSaveGroupClick(String text, Group group, JPanel container, JCheckBox[] dayCheckBoxes) {
        var oldGroup = new Group(group.getNumber(), group.getDay1Name(), group.getDay2Name(), group.getDay1(), group.getDay2(), group.getSunday());

        String[] acolytesNames = text.split("\n");
        group.getAcolytes().clear();
        for (String name : acolytesNames) {
            if (!name.trim().isEmpty()) {
                Acolyte acolyte = new Acolyte(name.trim());
                group.getAcolytes().add(acolyte);
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

        refreshGroupContainer(group, container, oldGroup);
    }
}
