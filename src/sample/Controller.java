package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable,Log {

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

    @FXML
    private TextArea outputTextArea;

    StringBuilder outputString = new StringBuilder();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        outputTextArea.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                outputTextArea.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
                //use Double.MIN_VALUE to scroll to the top
            }
        });

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                error(e.getMessage());
            }
        });

    }

    @Override
    public void log(String s){
        Platform.runLater(() -> {
            outputTextArea.appendText(s + '\n');
        });
    }

    @Override
    public void error(String s){

        outputTextArea.appendText("EXCEPTION:" +  '\n');
        outputTextArea.appendText(s + '\n');
        outputTextArea.appendText("END EXCEPTION"+ '\n');

    }

//    public void log(String s){
//        Platform.runLater(() -> {
//
//            outputTextArea.appendText(s + '\n');
//        });
//    }


    public void chooseRootFolder() throws Exception {
        DirectoryChooser fileChooser = new DirectoryChooser();
        File selectedFile = fileChooser.showDialog(null);

        if (selectedFile != null) {

            log("Root Dir: " + selectedFile.getPath());
            //System.out.println(selectedFile.getPath());
            rootLabel.setText(selectedFile.getAbsolutePath());
        }


    }

    public void chooseTargetFolder() {
        DirectoryChooser fileChooser = new DirectoryChooser();
        File selectedFile = fileChooser.showDialog(null);

        if (selectedFile != null) {
            log("Target Dir: " + selectedFile.getPath());
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
        syncer.setLogger(this);


        new Thread(new Runnable() {
            @Override
            public void run() {
                syncer.sync(rootString, targetString, keepFiles);
                log("Finished Syncing");
                //System.out.println("finished syncing");
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
                        log(e.getMessage());
                    }
                    double progress = syncer.getCurrentProccess();
                    int progressPercent = (int)(progress * 100);
                    System.out.println("Calc");
                    System.out.println(progress);
                    System.out.println("End Calc");
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
