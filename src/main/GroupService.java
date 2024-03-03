package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
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
        groups.add(group);
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

    public void saveGroups() throws IOException {
        Writer writer = new FileWriter("groups.json");
        gson.toJson(groups, writer);
        writer.close();
    }
}
