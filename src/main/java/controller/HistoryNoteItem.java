package controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.Note;

public class HistoryNoteItem {

    @FXML private VBox noteBox;
    @FXML private Text hNoteTitle;
    @FXML private Text hNoteText;
    @FXML private Pane pane;

    private Note note;
    private NoteController controller;

    public Parent createItemActionBar() {
        return noteBox;
    }

    public void init(Note note, NoteController noteController) {
        this.note = note;
        hNoteTitle.setText(note.getTitle());
        hNoteTitle.setFont(Font.font("Arial"));
        hNoteText.setText(note.getDate());
        hNoteText.setFont(Font.font("Arial"));
        this.controller = noteController;

        noteBox.setOnMouseClicked(event -> {
                controller.getNoteTitle().setText(note.getTitle());
                controller.getNoteText().setText(note.getText());
        });
    }

    public VBox getNoteBox() {
        return noteBox;
    }

    public Note getNote() {
        return note;
    }

    public Pane getPane() {
        return pane;
    }
}
