package main.models;

import lombok.Data;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Data
public class Group {
    private int number;
    private List<Acolyte> acolytes = new ArrayList<>();
    private String day1Name;
    private String day2Name;
    private SundayMass sunday;
    private DayOfWeek day1;
    private DayOfWeek day2;

    public Group(int number, String day1Name, String day2Name,DayOfWeek day1, DayOfWeek day2, SundayMass sunday) {
        this.number = number;
        this.day1Name = day1Name;
        this.day2Name = day2Name;
        this.sunday = sunday;
        this.day1 = day1;
        this.day2 = day2;
    }
}
