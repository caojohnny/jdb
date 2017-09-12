package com.gmail.woodyc40.topics;

import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.util.List;

public class ApplicationWindow extends Application {
    private static final String NO_GUI = "-nogui";

    public static void main(String[] args) {
        List<String> lists = Lists.newArrayList(args);
        if (lists.contains(ApplicationWindow.NO_GUI)) {
            LineReader reader = LineReaderBuilder.builder().build();

            while (true) {
                String line = reader.readLine("> ");
                if (line != null && !line.isEmpty()) {
                    
                }
            }
        } else {
            Application.launch(args);
        }
    }

    @Override public void start(Stage primaryStage) throws Exception {
        StackPane main = new StackPane();
        Scene scene = new Scene(main, 400, 400);

        primaryStage.setTitle("Program Helper");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}