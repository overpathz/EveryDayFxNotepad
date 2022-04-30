package controller;

import config.Configuration;
import config.NotepadContext;
import dao.NoteDao;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import lombok.Getter;
import model.Note;
import org.json.JSONObject;
import util.JsonUtil;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static config.Configuration.PREFERENCES;
import static config.Configuration.TEMP_NOTE_FILE;

@Getter
public class NoteController {

    private static final Logger LOGGER = Logger.getLogger(NoteController.class.getSimpleName());

    @FXML private Text noteDate;
    @FXML private TextArea noteTitle;
    @FXML private TextArea noteText;
    @FXML private VBox vboxNoteContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private Button uploadNotesBtn;
    @FXML private Button clearHistoryBtn;
    @FXML private Label noNotesText;
    @FXML private Text infoText;

    private NotepadContext context;
    private final NoteDao noteDao = new NoteDao();
    private final FileChooser fileChooser = new FileChooser();
    private final ExecutorService exService = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduledExService = Executors.newScheduledThreadPool(1);

    @FXML
    void initialize() throws IOException {
        startCheckForFixedTitleLength();
        initFileChooser();
        checkForNewDate();
        refreshNotesHistory();
        initDate();
    }

    @FXML
    void uploadNotes() {
        File chosenFile = chooseFile();
        if (chosenFile != null) {
            List<Note> allNotes = noteDao.getAllNotes();
            try(PrintWriter writer = new PrintWriter(chosenFile.getAbsoluteFile())) {
                for (Note allNote : allNotes)
                    writer.println(allNote + "\n");
            } catch (FileNotFoundException e) {
                LOGGER.warning("File not found: " + e.getMessage());
            }
            showMessage("Notes have been uploaded", MessageType.INFO);
        } else {
            LOGGER.warning("File has not been chosen.");
        }
    }

    @FXML
    void clearNotes() {
        noteDao.deleteAll();
        noteTitle.setText("");
        noteText.setText("");
        refreshNotesHistory();
        showMessage("Notes have been cleared", MessageType.INFO);
    }

    private void startCheckForFixedTitleLength() {
        scheduledExService.scheduleAtFixedRate(new CheckTitleLengthRunnable(),
                0, 50, TimeUnit.MILLISECONDS);
    }

    private void setNoNotesText() {
        List<Note> notes = noteDao.getAllNotes();
        if (notes.isEmpty()) {
            noNotesText.setVisible(true);
            noNotesText.setManaged(true);
        } else {
            noNotesText.setVisible(false);
            noNotesText.setManaged(false);
        }
    }

    private File chooseFile() {
        return fileChooser.showSaveDialog(null);
    }

    private void initDate() {
        String format = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        noteDate.setWrappingWidth(new Text(format).getWrappingWidth());
        noteDate.setText(format);
    }

    private void checkForNewDate() throws IOException {
        JSONObject jsonObj = JsonUtil.getJsonObj(PREFERENCES);
        String date = jsonObj.getString("date");
        if (date.equals("")) {
            saveCurrentDateToPrefs();
        } else {
            Note tmpNote = loadTmpNote();
            if (!getCurrentDate().equals(date)) {
                if (isNoteAvailableToSave(tmpNote)) {
                    noteTitle.clear();
                    noteText.clear();
                    noteDao.addNote(tmpNote);
                    saveCurrentDateToPrefs();
                }
            } else {
                noteTitle.setText(tmpNote.getTitle());
                noteText.setText(tmpNote.getText());
            }
        }
    }

    private boolean isNoteAvailableToSave(Note tmpNote) {
        return !tmpNote.getText().isEmpty();
    }

    private void saveCurrentDateToPrefs() {
        try(PrintWriter writer = new PrintWriter(new FileWriter(PREFERENCES))) {
            writer.println(String.format("{\"date\": \"%s\"}", getCurrentDate()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Note loadTmpNote() throws IOException {
        JSONObject noteJson = JsonUtil.getJsonObj(TEMP_NOTE_FILE);
        String date = noteJson.getString("date");
        String title = noteJson.getString("title");
        String text = noteJson.getString("text");
        return new Note(title, text, date);
    }

    public String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void initFileChooser() {
        fileChooser.setTitle("Choose or create a file you want to save notes in");
        fileChooser.setInitialFileName("saved_notes.txt");
    }

    private void showMessage(String msg, MessageType msgType) {
        String msgToSet = msgType.toString() + ": " + msg;
        context.getExService().execute(() -> {
            Platform.runLater(() -> {
                infoText.setWrappingWidth(new Text(msgToSet).getWrappingWidth());
                infoText.setText(msgToSet);
                infoText.setVisible(true);
            });
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> infoText.setVisible(false));
        });
    }

    private void refreshNotesHistory() {
        vboxNoteContainer.getChildren().removeIf(next -> !(next instanceof Label));
        List<Note> allNotes = noteDao.getAllNotes();
        for (Note allNote : allNotes) {
            HistoryNoteItem historyNoteItem = createHistoryNoteItem();
            historyNoteItem.init(allNote, this);
            vboxNoteContainer.getChildren().add(historyNoteItem.getNoteBox());
        }
        setNoNotesText();
    }

    private HistoryNoteItem createHistoryNoteItem() {
        FXMLLoader loader = new FXMLLoader(NoteController.class.getResource("../NoteHistory.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            System.out.println("trouble");
        }
        return loader.getController();
    }

    private class CheckTitleLengthRunnable implements Runnable {
        @Override
        public void run() {
            if (noteTitle.getText().length() > Configuration.MAX_TITLE_LENGTH) {
                Platform.runLater(() -> {
                    showMessage("Title should be 20 symbol length", MessageType.WARNING);
                    noteTitle.setText(noteTitle.getText().substring(0, Configuration.MAX_TITLE_LENGTH));
                });
            }
        }
    }

    private enum MessageType {
        INFO,
        WARNING,
        ERROR
    }

    public Text getNoteDate() {
        return noteDate;
    }

    public TextArea getNoteTitle() {
        return noteTitle;
    }

    public TextArea getNoteText() {
        return noteText;
    }

    public VBox getVboxNoteContainer() {
        return vboxNoteContainer;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public void setContext(NotepadContext context) {
        this.context = context;
    }
}