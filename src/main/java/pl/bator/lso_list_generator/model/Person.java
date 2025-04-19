package pl.bator.lso_list_generator.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class Person {
    private final UUID id = UUID.randomUUID();
    private final String name;
}
