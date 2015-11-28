package bkromhout.FictionDL.Gui;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.FictionDL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class Gui extends Application {
    // Preferences object for persisting things across runs.
    Preferences prefs;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Do first tasks for getting the GUI ready.
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("FictionDLGui.fxml"));
        Parent root = loader.load();
        FDLGuiController controller = loader.getController();
        // Tell the controller we own it.
        controller.setGui(this);
        // Configure the stage.
        primaryStage.setTitle(C.VER_STRING);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setMinWidth(C.G_MIN_WIDTH);
        primaryStage.setMinHeight(C.G_MIN_HEIGHT);
        // Get preferences.
        prefs = Preferences.userNodeForPackage(Gui.class);
        // Show the stage.
        primaryStage.show();
    }

    /**
     * Create a FictionDL object with the given strings.
     * @param inputFilePath Path to input file.
     * @param outputDirPath Path to output directory.
     */
    protected void runFictionDl(String inputFilePath, String outputDirPath) {
        // Do cool stuff.
        try {
            new FictionDL(inputFilePath, outputDirPath).run();
        } catch (IllegalArgumentException e) {
            System.out.println(C.INVALID_PATH);
        }
    }

    /**
     * Save a string preference.
     * @param key Preference key.
     * @param value Preference value.
     */
    protected void putPref(String key, String value) {
        prefs.put(key, value);
    }

    /**
     * Get a string preference.
     * @param key Preference key.
     * @return Preference value, or null if the preference doesn't exist yet.
     */
    protected String getPref(String key) {
        return prefs.get(key, null);
    }
}
