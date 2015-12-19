package bkromhout.fdl.ui;

import bkromhout.fdl.util.C;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.HashMap;

/**
 * Controller class for the GUI.
 */
public class GuiController {
    /* Preference key strings. */
    private static final String PREF_CFG_FILE_PATH = "key_cfg_file_path";
    private static final String PREF_OUT_DIR_PATH = "key_out_dir_path";
    private static final String PREF_IN_FILE_PATH = "key_in_file_path";

    /* Shown in system chooser dialogs */
    private static final String D_CHOOSE_TITLE = "Choose Directory:";
    private static final String F_CHOOSE_TITLE = "Choose File:";

    /* Gui which owns this controller. */
    private Gui gui;

    /* Containers. */
    public AnchorPane rootView;
    public AnchorPane optsView;
    public TitledPane logView;

    /* Input file views. */
    public Label lblInFile;
    public TextField tfInFile;
    public Button btnChooseInFile;

    /* Output dir views. */
    public Label lblOutDir;
    public TextField tfOutDir;
    public Button btnChooseOutDir;
    public Button btnDefaultOutDir;

    /* Config file views. */
    public Label lblCfgFile;
    public TextField tfCfgFile;
    public Button btnChooseCfgFile;

    /* Start button and progress bar. */
    public Button btnStart;
    public ProgressBar pbProgress;

    /* Log. */
    public ScrollPane spLogCont;
    public static TextFlow flowLog;

    @FXML
    private void initialize() {
        // Specifically set the log TextFlow, since it's static.
        flowLog = (TextFlow) spLogCont.getContent();

        // Set up the log TextFlow's ScrollPane to always scroll to the bottom when a new line is added.
        flowLog.getChildren().addListener((ListChangeListener<Node>) listener -> {
            flowLog.layout();
            spLogCont.layout();
            spLogCont.setVvalue(1.0d);
        });

        // Set default button to clear dir text field.
        btnDefaultOutDir.setOnAction(event1 -> tfOutDir.clear());

        // Set choose in file button's action.
        btnChooseInFile.setOnAction(event -> {
            // Create a file chooser.
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(F_CHOOSE_TITLE);
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
            dirChooser.setTitle(D_CHOOSE_TITLE);
            // Set the start directory if the current text field value is a valid directory.
            File currDir = tfOutDir.getText() != null ? new File(tfOutDir.getText()) : null;
            if (currDir != null && currDir.exists() && currDir.isDirectory())
                dirChooser.setInitialDirectory(currDir);
            // Have user choose a directory.
            File selectedDir = dirChooser.showDialog(btnChooseOutDir.getScene().getWindow());
            // If the user selected a directory, put its path into the text field.
            if (selectedDir != null) tfOutDir.setText(selectedDir.getAbsolutePath());
        });

        // Set choose config file button's action.
        btnChooseCfgFile.setOnAction(event -> {
            // Create a file chooser.
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(F_CHOOSE_TITLE);
            // Set start directory if the current text field value is a valid file.
            File currFile = tfCfgFile.getText() != null ? new File(tfCfgFile.getText()) : null;
            if (currFile != null && currFile.exists() && currFile.isFile())
                fileChooser.setInitialDirectory(currFile.getParentFile());
            // Have user choose a file.
            File selectedFile = fileChooser.showOpenDialog(btnChooseCfgFile.getScene().getWindow());
            // If the user selected a file, put its path into the text field.
            if (selectedFile != null) tfCfgFile.setText(selectedFile.getAbsolutePath());
        });

        // Set start button action.
        btnStart.setOnAction(event -> {
            // Store the text field values in the prefs file.
            saveFields();
            // Reset progress bar, then make arguments map and run FictionDL.
            pbProgress.setProgress(0d);
            HashMap<String, String> ficDlArgs = new HashMap<>();
            ficDlArgs.put(C.ARG_IN_PATH, tfInFile.getText());
            ficDlArgs.put(C.ARG_OUT_PATH, tfOutDir.getText());
            ficDlArgs.put(C.ARG_CFG_PATH, tfCfgFile.getText());
            gui.runFictionDl(ficDlArgs);
        });
    }

    /**
     * Set the {@link Gui} which owns this controller.
     * @param gui Gui.
     */
    protected void setGui(Gui gui) {
        this.gui = gui;
        initGui();
    }

    /**
     * Called after we have a reference to the {@link Gui} which owns us.
     */
    private void initGui() {
        // Retrieve the last input and output paths that were used.
        restoreFields();
        // Set the Start button as focused, that way the user can just hit enter.
        btnStart.setDefaultButton(true);
    }

    /**
     * Restore the saved contents of text fields.
     */
    private void restoreFields() {
        tfInFile.setText(gui.getPref(PREF_IN_FILE_PATH));
        tfOutDir.setText(gui.getPref(PREF_OUT_DIR_PATH));
        tfCfgFile.setText(gui.getPref(PREF_CFG_FILE_PATH));
    }

    /**
     * Save the contents of text fields.
     */
    protected void saveFields() {
        gui.putPref(PREF_IN_FILE_PATH, tfInFile.getText());
        gui.putPref(PREF_OUT_DIR_PATH, tfOutDir.getText());
        gui.putPref(PREF_CFG_FILE_PATH, tfCfgFile.getText());
    }

    /**
     * Append text to the log area.
     * @param text The text to append.
     */
    public static void appendLogText(Text text) {
        Platform.runLater(() -> flowLog.getChildren().add(text));
    }

    /**
     * Set certain controls as disabled or enabled.
     * @param areEnabled True if controls should be enabled, otherwise false.
     */
    protected void setControlsEnabled(boolean areEnabled) {
        tfInFile.setEditable(areEnabled);
        tfOutDir.setEditable(areEnabled);
        tfCfgFile.setEditable(areEnabled);
        btnChooseInFile.setDisable(!areEnabled);
        btnChooseOutDir.setDisable(!areEnabled);
        btnDefaultOutDir.setDisable(!areEnabled);
        btnChooseCfgFile.setDisable(!areEnabled);
        btnStart.setDisable(!areEnabled);
    }
}
