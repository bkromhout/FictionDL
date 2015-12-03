package bkromhout.FictionDL.Story;

import java.io.IOException;

/**
 * Model object for a MuggleNet story. Despite the word "model", this is not an object with a light initialization cost,
 * as it accesses the internet to retrieve story information.
 */
public class MuggleNetStory extends Story {

    /**
     * Create a new MuggleNetStory object based off of a URL.
     * @param url URL of the story this model represents.
     */
    public MuggleNetStory(String url) throws IOException {
        populateInfo(url);
    }

    /**
     * Populate fields.
     */
    private void populateInfo(String url) throws IOException {
        
    }
}
