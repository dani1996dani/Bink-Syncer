package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.awt.event.ActionEvent;
import java.io.File;

public class Controller {

    @FXML
    private Label rootLabel;

    @FXML
    private Label targetLabel;

    @FXML
    private CheckBox keepTargetFiles;

    @FXML
    private Button rootSelection;

    @FXML
    private Button targetSelection;

    @FXML
    private Button syncButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;


    public void chooseRootFolder() {
        System.out.println("hello from test");


        DirectoryChooser fileChooser = new DirectoryChooser();
        File selectedFile = fileChooser.showDialog(null);

        if (selectedFile != null) {
            System.out.println(selectedFile.getPath());
            rootLabel.setText(selectedFile.getAbsolutePath());
        }
    }

    public void chooseTargetFolder() {
        DirectoryChooser fileChooser = new DirectoryChooser();
        File selectedFile = fileChooser.showDialog(null);

        if (selectedFile != null) {
            targetLabel.setText(selectedFile.getAbsolutePath());
        }
    }

    private void disableUI() {
        rootSelection.setDisable(true);
        targetSelection.setDisable(true);
        syncButton.setDisable(true);
        keepTargetFiles.setDisable(true);

        progressLabel.setVisible(true);
    }

    private void enableUI() {
        rootSelection.setDisable(false);
        targetSelection.setDisable(false);
        syncButton.setDisable(false);
        keepTargetFiles.setDisable(false);
    }

    public void sync() {
        System.out.println("syncing");
        boolean keepFiles = keepTargetFiles.isSelected();
        String rootString = rootLabel.getText();
        String targetString = targetLabel.getText();
        if (rootString == null || rootString.isEmpty() || targetString == null || targetString.isEmpty()) {
            return;
        }
        disableUI();

        Syncer syncer = Syncer.getInstance();


        new Thread(new Runnable() {
            @Override
            public void run() {
                syncer.sync(rootString, targetString, keepFiles);
                System.out.println("finished syncing");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        enableUI();
//                        progressBar.setProgress(100.0);
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bink finished syncing folders successfully!", ButtonType.OK);
                        alert.showAndWait();

                    }
                });

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                syncer.isSyncing = true;
                while (syncer.isSyncing()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    double progress = syncer.getCurrentProccess();
                    int progressPercent = (int)(progress * 100);
                    System.out.println(progress);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progress);
                            progressLabel.setText(progressPercent + "%");
                        }
                    });

                }
            }
        }).start();


    }


}
