package pl.bator.lso_list_generator.models;

import lombok.Data;

import java.util.UUID;

@Data
public class Acolyte {
    private UUID id = UUID.randomUUID();
    private String name;

    public Acolyte(String name) {
        this.name = name;
    }
}
