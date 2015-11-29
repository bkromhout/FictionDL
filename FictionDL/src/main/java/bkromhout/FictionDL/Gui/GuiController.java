package bkromhout.FictionDL.Gui;

import bkromhout.FictionDL.C;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Controller class for the GUI.
 */
public class GuiController {
    // The gui which owns this controller.
    private Gui gui;

    // Containers.
    public AnchorPane rootView;
    public AnchorPane optsView;
    public TitledPane logView;

    // Input file views.
    public Label lblInFile;
    public TextField tfInFile;
    public Button btnChooseInFile;

    // Output dir views.
    public Label lblOutDir;
    public TextField tfOutDir;
    public Button btnChooseOutDir;
    public Button btnDefaultOutDir;

    // Start button and progress bar.
    public Button btnStart;
    public ProgressBar pbProgress;

    // Log.
    public TextArea taLog;

    @FXML
    private void initialize() {
        // Redirect System.out to the text area log.
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                appendLogText(String.valueOf((char) b));
            }
        };
        System.setOut(new PrintStream(out, true));
        // Set default button to clear dir text field.
        btnDefaultOutDir.setOnAction(event1 -> tfOutDir.clear());
        // Set choose in file button's action.
        btnChooseInFile.setOnAction(event -> {
            // Create a file chooser.
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(C.F_CHOOSE_TITLE);
            // Set start directory if the current text field value is a valid file.
            File currFile = tfInFile.getText() != null ? new File(tfInFile.getText()) : null;
            if (currFile != null && currFile.exists() && currFile.isFile())
                fileChooser.setInitialDirectory(currFile.getParentFile());
            // Have user choose a file.
            File selectedFile = fileChooser.showOpenDialog(btnChooseInFile.getScene().getWindow());
            // If the user selected a file, put its path into the text field.
            if (selectedFile != null) tfInFile.setText(selectedFile.getAbsolutePath());
        });
        // Set choose out dir button's action.
        btnChooseOutDir.setOnAction(event -> {
            // Create a directory chooser.
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle(C.D_CHOOSE_TITLE);
            // Set the start directory if the current text field value is a valid directory.
            File currDir = tfOutDir.getText() != null ? new File(tfOutDir.getText()) : null;
            if (currDir != null && currDir.exists() && currDir.isDirectory())
                dirChooser.setInitialDirectory(currDir);
            // Have user choose a directory.
            File selectedDir = dirChooser.showDialog(btnChooseOutDir.getScene().getWindow());
            // If the user selected a directory, put its path into the text field.
            if (selectedDir != null) tfOutDir.setText(selectedDir.getAbsolutePath());
        });
        // Set start button action.
        btnStart.setOnAction(event -> {
            // Store the text field values in the prefs file.
            gui.putPref(C.KEY_IN_FILE_PATH, tfInFile.getText());
            gui.putPref(C.KEY_OUT_DIR_PATH, tfOutDir.getText());
            // Reset progress bar, then run FictionDL.
            pbProgress.setProgress(0d);
            gui.runFictionDl(tfInFile.getText(), tfOutDir.getText());
        });
    }

    /**
     * Set the Gui which owns this controller.
     * @param gui Gui.
     */
    protected void setGui(Gui gui) {
        this.gui = gui;
        initGui();
    }

    /**
     * Called after we have a reference to the Gui which owns us.
     */
    private void initGui() {
        // Retrieve the last input and output paths that were used.
        tfInFile.setText(gui.getPref(C.KEY_IN_FILE_PATH));
        tfOutDir.setText(gui.getPref(C.KEY_OUT_DIR_PATH));
        // Set the Start button as focused, that way the user can just hit enter.
        btnStart.setDefaultButton(true);
    }

    /**
     * Append text to the log area.
     * @param text The text to append.
     */
    public void appendLogText(String text) {
        Platform.runLater(() -> taLog.appendText(text));
    }

    /**
     * Set certain controls as disabled or enabled.
     * @param areEnabled True if controls should be enabled, otherwise false.
     */
    protected void setControlsEnabled(boolean areEnabled) {
        tfInFile.setEditable(areEnabled);
        tfOutDir.setEditable(areEnabled);
        btnChooseInFile.setDisable(!areEnabled);
        btnChooseOutDir.setDisable(!areEnabled);
        btnDefaultOutDir.setDisable(!areEnabled);
        btnStart.setDisable(!areEnabled);
    }
}
