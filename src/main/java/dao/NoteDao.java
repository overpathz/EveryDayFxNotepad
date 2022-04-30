package dao;

import config.Configuration;
import model.Note;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NoteDao {

    private static final String NOTES_JSON = Configuration.NOTES;

    public void addNote(Note note) {
        JSONObject jsonRootObject = new JSONObject(getJsonString());
        JSONArray jsonArray = jsonRootObject.getJSONArray("notes");

        JSONObject noteObj = new JSONObject();
        noteObj.put("title", !note.getTitle().isEmpty() ? note.getTitle() : "N/A");
        noteObj.put("text", note.getText());
        noteObj.put("date", note.getDate());
        jsonArray.put(noteObj);

        jsonRootObject.put("notes", jsonArray);

        try (PrintWriter writer = new PrintWriter(NOTES_JSON)) {
            writer.println(jsonRootObject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<Note> getAllNotes() {
        JSONObject jsonRootObject = new JSONObject(getJsonString());
        JSONArray jsonArray = jsonRootObject.getJSONArray("notes");
        List<Note> notes = new ArrayList<>();
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String title = jsonObject.getString("title");
            String text = jsonObject.getString("text");
            String date = jsonObject.getString("date");
            notes.add(new Note(title, text, date));
        }
        return notes;
    }

    public void deleteAll() {
        try (PrintWriter writer = new PrintWriter(NOTES_JSON)) {
            writer.println("{\"notes\":[]}");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Note getNoteByTitle(String title) {
        return getAllNotes().stream().filter(x -> x.getTitle().equals(title)).findAny().orElseThrow();
    }

    public Note getNoteByDate() {
        // not implemented
        return null;
    }

    private String getJsonString() {
        try {
            return new String(Files.readAllBytes(Path.of(NOTES_JSON)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}