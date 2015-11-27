package bkromhout.FictionDL.Story;

import java.io.IOException;

/**
 * Model object for a FanFiction.net story. Despite the word "model", this is not an object with a light initialization
 * cost, as it accesses the internet to retrieve story information.
 */
public class FanFictionStory extends Story {

    /**
     * Create a new FanFictionStory object based off of a URL.
     * @param url URL of the story this model represents.
     */
    public FanFictionStory(String url) throws IOException {
        populateInfo(url);
    }

    /**
     * Populate this model's fields.
     * @param url A FFN URL.
     * @throws IOException Throw for many reasons, but the net result is that we can't build a story model for this.
     */
    private void populateInfo(String url) throws IOException {

    }
}
