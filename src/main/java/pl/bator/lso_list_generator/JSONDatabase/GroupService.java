package pl.bator.lso_list_generator.JSONDatabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import pl.bator.lso_list_generator.models.Group;

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
        if (groups == null) {
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
            Path path = Path.of(System.getProperty("user.home"), "lso-groups.json");
            if (!Files.exists(path)) {
                Files.createFile(path);
                return new ArrayList<>();
            }
            String json = Files.readString(path);
            return gson.fromJson(json, new TypeToken<List<Group>>() {
            }.getType());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    //

    public void saveGroups() throws IOException {
        
        Path path = Path.of(System.getProperty("user.home"), "lso-groups.json");
        Writer writer = Files.newBufferedWriter(path);
        gson.toJson(groups, writer);
        writer.close();
    }
}
