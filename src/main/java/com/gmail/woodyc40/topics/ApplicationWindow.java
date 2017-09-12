/*
 * JDB - Java Debugger
 * Copyright 2017 Johnny Cao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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