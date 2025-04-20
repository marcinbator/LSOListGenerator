package pl.bator.lso_list_generator.view;

import org.jetbrains.annotations.NotNull;
import pl.bator.lso_list_generator.model.Group;
import pl.bator.lso_list_generator.repository.GroupJSONRepository;
import pl.bator.lso_list_generator.service.GroupsService;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;

public class GroupsView {
    private final GroupsService groupsService;
    private final GroupJSONRepository groupJSONRepository;

    public GroupsView(GroupJSONRepository groupJSONRepository) {
        this.groupsService = new GroupsService(groupJSONRepository);
        this.groupJSONRepository = groupJSONRepository;
    }

    public void initView(Component parent, @NotNull JPanel groupPanel, @NotNull JScrollPane scrollPane, @NotNull JPanel buttonPanel) {
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.setBorder(BorderFactory.createTitledBorder("Grupy"));
        groupPanel.setMinimumSize(new Dimension(600, 400));

        scrollPane.setPreferredSize(new Dimension(600, 400));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        var addGroupButton = new JButton("Dodaj nową grupę");
        addGroupButton.addActionListener(e -> groupsService.handleAddNewGroup(groupPanel));
        buttonPanel.add(addGroupButton);

        var groups = groupJSONRepository.getGroups().stream().sorted(Comparator.comparingInt(Group::getNumber)).toList();
        for (Group group : groups) {
            JPanel tilePanel = groupsService.createGroupTile(group, groupPanel);
            groupPanel.add(tilePanel);
        }

        groupsService.initButtons(parent, buttonPanel, groupPanel);
    }
}
