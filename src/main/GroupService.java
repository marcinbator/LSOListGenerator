package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import main.models.Acolyte;
import main.models.Group;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GroupService {
    @Getter
    @Setter
    private List<Group> groups = new ArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public GroupService() {
        groups = readGroups();
        if(groups == null) {
            groups = new ArrayList<>();
        }
    }

    public void addGroup(Group group) throws IOException {
        groups.stream().filter(g -> g.getNumber() == group.getNumber()).findFirst().ifPresent(g -> {
            throw new IllegalArgumentException("Group already exists");
        });
        groups.add(group);
        saveGroups();
    }

    public void addAcolyte(Acolyte acolyte, int groupNumber) throws IOException, IllegalArgumentException {
        Group group = getGroup(groupNumber);
        if (group == null) {
            throw new IllegalArgumentException("Group not found");
        }
        group.getAcolytes().add(acolyte);
        saveGroups();
    }

    public void removeAcolyte(Acolyte acolyte, int groupNumber) throws IOException {
        Group group = getGroup(groupNumber);
        if (group == null) {
            throw new IllegalArgumentException("Group not found");
        }
        group.getAcolytes().remove(acolyte);
        saveGroups();
    }

    public void removeGroup(int groupNumber) throws IOException {
        groups.removeIf(group -> group.getNumber() == groupNumber);
        saveGroups();
    }

    public ArrayList<Group> readGroups() {
        try {
            String json = Files.readString(Path.of("groups.json"));
            return gson.fromJson(json, new TypeToken<List<Group>>(){}.getType());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    //

    private Group getGroup(int groupNumber) {
        return groups.stream().filter(group -> group.getNumber() == groupNumber).findFirst().orElse(null);
    }

    public void saveGroups() throws IOException {
        Writer writer = new FileWriter("groups.json");
        gson.toJson(groups, writer);
        writer.close();
    }
}
