package pl.bator.lso_list_generator.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import pl.bator.lso_list_generator.model.Group;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GroupJSONRepository {
    @Getter
    @Setter
    private List<Group> groups;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path dbFilePath = Path.of(System.getProperty("user.home"), "lso-groups.json");

    public GroupJSONRepository() throws IOException {
        groups = readGroups();
    }

    public void addGroup(Group group) throws IOException {
        groups.add(group);
        saveGroups();
    }

    public void removeGroup(int groupNumber) throws IOException {
        groups.removeIf(group -> group.getNumber() == groupNumber);
        saveGroups();
    }

    //

    private ArrayList<Group> readGroups() throws IOException {
        if (!Files.exists(dbFilePath)) {
            Files.createFile(dbFilePath);
            return new ArrayList<>();
        }
        String json = Files.readString(dbFilePath);
        return gson.fromJson(json, new TypeToken<List<Group>>() {
        }.getType());
    }

    private void saveGroups() throws IOException {
        Writer writer = Files.newBufferedWriter(dbFilePath);
        gson.toJson(groups, writer);
        writer.close();
    }
}
