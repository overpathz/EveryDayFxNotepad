package app;

import config.Configuration;
import config.NotepadContext;
import controller.NoteController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

public class Notepad extends Application {

    private NoteController noteController;
    private NotepadContext context;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Notepad.class.getResource("../StyledMain.fxml"));
        fxmlLoader.load();
        noteController = fxmlLoader.getController();
        Parent root = fxmlLoader.getRoot();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        context = new NotepadContext(primaryStage, scene, noteController);
        noteController.setContext(context);
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.show();
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            addTempNote();
            context.getExService().shutdown();
            System.exit(0);
        });
    }

    private void addTempNote() {
        File file = new File(Configuration.TEMP_NOTE_FILE);
        try {
            JSONObject noteJson = new JSONObject(new String(Files.readAllBytes(file.toPath())));
            noteJson.put("date", noteController.getCurrentDate());
            noteJson.put("title", noteController.getNoteTitle().getText());
            noteJson.put("text", noteController.getNoteText().getText());
            try(PrintWriter writer = new PrintWriter(file)) {
                writer.println(noteJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
