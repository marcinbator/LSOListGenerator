package pl.bator.lso_list_generator.model;

import lombok.Data;

import java.time.DayOfWeek;

@Data
public class Day {
    private int dayOfMonth;
    private DayOfWeek dayOfWeek;
    private boolean isObligatory;
    private boolean isSunday;
    private SundayMass sundayMass;
}
