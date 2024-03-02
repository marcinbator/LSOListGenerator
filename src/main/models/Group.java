package main.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Group {
    private int number;
    private List<Acolyte> acolytes = new ArrayList<>();
    private String day1;
    private String day2;
    private String sunday;

    public Group(int number, String day1, String day2, String sunday) {
        this.number = number;
        this.day1 = day1;
        this.day2 = day2;
        this.sunday = sunday;
    }
}
