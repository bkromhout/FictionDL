package bkromhout.fdl.ui;

import bkromhout.fdl.C;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.Util;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * Gui for running {@link FictionDL} with.
 */
public class Gui extends Application {
    // Preferences object for persisting things across runs.
    Preferences prefs;
    // GUI Controller.
    GuiController controller;
    // Current task.
    FictionDL.FictionDLTask fictionDLTask = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Get preferences.
        prefs = Preferences.userNodeForPackage(FictionDL.class);
        // Do first tasks for getting the GUI ready.
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("FictionDLGui.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        // Tell the controller we own it.
        controller.setGui(this);
        // Configure the stage.
        primaryStage.setTitle(C.VER_STRING);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setMinWidth(525.0);
        primaryStage.setMinHeight(300.0);
        primaryStage.setOnHiding(handler -> {
            controller.saveFields();
            fictionDLTask.cancel(); // Make sure we actually stop the JVM when our GUI closes.
        }); // Save text fields' contents when closing stage.
        // Show the stage.
        primaryStage.show();
    }

    /**
     * Create a {@link FictionDL} object with the given strings.
     * @param args Arguments, mapped to keys.
     */
    protected void runFictionDl(HashMap<String, String> args) {
        fictionDLTask = new FictionDL.FictionDLTask(args);
        // Set the task's handlers.
        fictionDLTask.setOnScheduled(handler -> {
            controller.pbProgress.setProgress(0d);
            controller.pbProgress.progressProperty().bind(fictionDLTask.progressProperty());
            GuiController.flowLog.getChildren().clear();
            controller.setControlsEnabled(false);
        });
        fictionDLTask.setOnSucceeded(handler -> {
            controller.pbProgress.progressProperty().unbind();
            controller.setControlsEnabled(true);
        });
        fictionDLTask.setOnCancelled(handler -> {
            controller.pbProgress.progressProperty().unbind();
            controller.pbProgress.setProgress(0d);
            controller.setControlsEnabled(true);
            if (fictionDLTask.getException() != null) {
                // FictionDL should only allow an exception to be thrown if one of the paths it was supplied was
                // invalid, so we only print this message in that case.
                Util.logf(C.INVALID_PATH, fictionDLTask.getException().getMessage());
            }
        });
        // Do cool stuff.
        Thread fictionDLThread = new Thread(fictionDLTask);
        fictionDLThread.setDaemon(true);
        fictionDLThread.start();
    }

    /**
     * Save a string preference. If null is passed for the value, then the value is removed from the preference.
     * @param key   Preference key.
     * @param value Preference value.
     */
    protected void putPref(String key, String value) {
        if (value != null) prefs.put(key, value);
        else prefs.remove(key);
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
