package bkromhout.FictionDL.Gui;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.FictionDL;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class Gui extends Application {
    // Preferences object for persisting things across runs.
    Preferences prefs;
    // GUI Controller.
    FDLGuiController controller;
    // Current task for running FictionDL.
    FictionDL.FictionDLTask fictionDLTask = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Do first tasks for getting the GUI ready.
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("FictionDLGui.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        // Tell the controller we own it.
        controller.setGui(this);
        // Configure the stage.
        primaryStage.setTitle(C.VER_STRING);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setMinWidth(C.G_MIN_WIDTH);
        primaryStage.setMinHeight(C.G_MIN_HEIGHT);
        // Get preferences.
        prefs = Preferences.userNodeForPackage(FictionDL.class);
        // Show the stage.
        primaryStage.show();
    }

    /**
     * Create a FictionDL object with the given strings.
     * @param inputFilePath Path to input file.
     * @param outputDirPath Path to output directory.
     */
    protected void runFictionDl(String inputFilePath, String outputDirPath) {
        fictionDLTask = new FictionDL.FictionDLTask(inputFilePath, outputDirPath);
        // Set the task's handlers.
        fictionDLTask.setOnScheduled(handler -> {
            controller.pbProgress.setProgress(0d);
            controller.pbProgress.progressProperty().bind(fictionDLTask.progressProperty());
            controller.taLog.clear();
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
            if (handler.getSource().getException() != null) {
                // FictionDL should only allow an exception to be thrown if one of the paths it was supplied was
                // invalid, so we only print this message in that case.
                System.out.printf(C.INVALID_PATH, handler.getSource().getException().getMessage());
            }
        });
        // Do cool stuff.
        fictionDLTask.run();
    }

    /**
     * Save a string preference.
     * @param key   Preference key.
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
