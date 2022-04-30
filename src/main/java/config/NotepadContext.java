package config;

import controller.NoteController;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AllArgsConstructor
@Getter
public class NotepadContext {
    private final Stage stage;
    private final Scene scene;
    private final NoteController noteController;
    private final ExecutorService exService = Executors.newCachedThreadPool();
}
