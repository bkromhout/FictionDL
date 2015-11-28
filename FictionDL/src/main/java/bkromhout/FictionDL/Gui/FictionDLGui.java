package bkromhout.FictionDL.Gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

public class FictionDLGui {

    public AnchorPane rootView;
    public AnchorPane optsView;
    public TitledPane logView;

    public Label lblInFile;
    public TextField tfInFile;
    public Button btnChooseInFile;

    public Label lblOutDir;
    public TextField tfOutDir;
    public Button btnChooseOutDir;
    public Button btnDefaultOutDir;

    public Button btnStart;
    public ProgressBar pbProgress;

    public TextArea taLog;

    @FXML
    private void initialize() {
        btnStart.setOnAction(event -> taLog.appendText("Started!\n"));
    }
}
