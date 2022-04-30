package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class Note {
    private String title;
    private String text;
    private String date;

    @Override
    public String toString() {
        return "Date: " + date + "\n" +
                "Title: " + title + "\n" +
                "Note body:\n" + text;
    }
}
