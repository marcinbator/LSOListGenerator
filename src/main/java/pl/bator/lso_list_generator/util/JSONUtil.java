package pl.bator.lso_list_generator.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JSONUtil {
    public void copyFile(Path sourceFilePath, Path targetFilePath) throws IOException {
        Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
    }
}
