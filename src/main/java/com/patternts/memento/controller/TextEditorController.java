package com.patternts.memento.controller;

import com.patternts.memento.FileInfo;
import com.patternts.memento.FileInfoMemento;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.SEVERE;

public class TextEditorController {
    private File loadedFileReference;
    private FileTime lastModifiedTime;
    public FileInfo fileInfo = new FileInfo();
    public TextArea textArea;
    public Label statusMessage;
    public ProgressBar progressBar;
    public Button loadChangesButton;
    @FXML
    public VBox savedFiles;

    private final List<FileInfoMemento> fileInfoMementos = new ArrayList<>();


    public void initialize() {
        loadChangesButton.setVisible(false);
    }

    public void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
        );

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File fileToLoad = fileChooser.showOpenDialog(null);

        if (fileToLoad != null) {
            loadFileToTextArea(fileToLoad);
        }
    }

    private void loadFileToTextArea(File fileToLoad) {
        Task<String> loadTask = fileLoaderTask(fileToLoad);
        progressBar.progressProperty().bind(loadTask.progressProperty());
        loadTask.run();
    }

    private Task<String> fileLoaderTask(File fileToLoad) {
        Task<String> loadFileTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                BufferedReader reader = new BufferedReader(new FileReader(fileToLoad));

                long lineCount;
                try (Stream<String> stream = Files.lines(fileToLoad.toPath())) {
                    lineCount = stream.count();
                }

                String line;
                StringBuilder totalFile = new StringBuilder();
                long linesLoaded = 0;

                while ((line = reader.readLine()) != null) {
                    totalFile.append(line);
                    totalFile.append("\n");
                    updateProgress(++linesLoaded, lineCount);
                }

                return totalFile.toString();
            }
        };

        loadFileTask.setOnSucceeded(workerStateEvent -> {
            try {
                fileInfo.setFileTextAndDate(loadFileTask.get(), new Date());
                FileInfoMemento currentScreen = fileInfo.save();
                fileInfoMementos.add(currentScreen);

                textArea.setText(currentScreen.getFileText());

                addNewFileSaveButton(currentScreen.getDate().toString());

                statusMessage.setText("File loaded: " + fileToLoad.getName());
                loadedFileReference = fileToLoad;
                lastModifiedTime = Files.readAttributes(fileToLoad.toPath(), BasicFileAttributes.class).lastModifiedTime();
            } catch (InterruptedException | ExecutionException | IOException e) {
                Logger.getLogger(getClass().getName()).log(SEVERE, null, e);
                textArea.setText("Could not load file from:\n " + fileToLoad.getAbsolutePath());
            }

            scheduleFileChecking(loadedFileReference);

        });

        loadFileTask.setOnFailed(workerStateEvent -> {
            textArea.setText("Could not load file from:\n " + fileToLoad.getAbsolutePath());
            statusMessage.setText("Failed to load file");
        });

        return loadFileTask;
    }

    private void scheduleFileChecking(File file) {
        ScheduledService<Boolean> fileChangeCheckingService = createFileChangesCheckingService(file);

        fileChangeCheckingService.setOnSucceeded(workerStateEvent -> {
            if (fileChangeCheckingService.getLastValue() == null) {
                return;
            }

            if (fileChangeCheckingService.getLastValue()) {
                fileChangeCheckingService.cancel();
                notifyUserOfChanges();
            }
        });

        System.out.println("Starting Checking Service...");
        fileChangeCheckingService.start();
    }

    private ScheduledService<Boolean> createFileChangesCheckingService(File file) {
        ScheduledService<Boolean> scheduledService = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        FileTime lastModifiedAsOfNow = Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime();
                        return lastModifiedAsOfNow.compareTo(lastModifiedTime) > 0;
                    }
                };
            }
        };

        scheduledService.setPeriod(Duration.seconds(1));

        return scheduledService;
    }

    private void notifyUserOfChanges() {
        loadChangesButton.setVisible(true);
    }

    public void loadChanges(ActionEvent event) {
        loadFileToTextArea(loadedFileReference);
        loadChangesButton.setVisible(false);
    }

    public void saveFile(ActionEvent event) {
        try {
            FileWriter myWriter = new FileWriter(loadedFileReference);
            myWriter.write(textArea.getText());

            saveFileInfoProcess();
            myWriter.close();

            lastModifiedTime = FileTime.fromMillis(System.currentTimeMillis() + 3000);

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(SEVERE, null, e);
        }
    }

    @FXML
    void saveButtonPressed(ActionEvent event) {
        saveFileInfoProcess();
    }

    public void saveFileInfoProcess() {
        fileInfo.setFileTextAndDate(textArea.getText(), new Date());
        fileInfoMementos.add(fileInfo.save());
        addNewFileSaveButton(fileInfo.save().getDate().toString());
    }

    public void addNewFileSaveButton(String buttonText) {
        Button button = new Button(buttonText);
        savedFiles.getChildren().add(button);

        button.setOnAction(event -> {
            int buttonIndex = savedFiles.getChildren().indexOf(button);
            FileInfoMemento fileInfoMemento = fileInfoMementos.get(buttonIndex);

            fileInfo.load(fileInfoMemento);

            textArea.setText(fileInfoMemento.getFileText());

            //saveFileInfoProcess();
        });

        savedFiles.requestLayout();
    }
}
