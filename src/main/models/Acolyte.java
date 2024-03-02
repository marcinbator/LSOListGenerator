package main.models;

import lombok.Data;

import java.util.UUID;

@Data
public class Acolyte {
    private UUID id = UUID.randomUUID();
    private String name;
    private int groupNumber;

    public Acolyte(String name, int groupNumber) {
        this.name = name;
        this.groupNumber = groupNumber;
    }
}
