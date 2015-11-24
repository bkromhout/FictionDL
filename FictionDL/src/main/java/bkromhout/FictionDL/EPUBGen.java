package bkromhout.FictionDL;

import bkromhout.FictionDL.Story.Story;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Class which leverages epublib to create ePUB files. This class is meant to be used once and then discarded, so is
 * marked final.
 */
public final class EpubGen {
    // The Story that this EpubGen was created with.
    private Story story;

    /**
     * Create an ePUB generator for the given story.
     * @param story Story.
     */
    public EpubGen(Story story) {
        this.story = story;
    }

    /**
     * Generate an ePUB file and save it at the given location with the given file name.
     * @param saveDir Location to save the ePUB at.
     * @param fileName File name to save the ePUB with.
     * @return True if ePUB generated successfully, otherwise false (and an error message will have been printed).
     */
    public boolean makeEpub(Path saveDir, String fileName) {
        // Generate and save the ePUB.
        File file = saveDir.resolve(fileName).toFile();
        try {
            new EpubWriter().write(generateEpub(), new FileOutputStream(file));
        } catch (IOException e) {
            System.out.printf(C.SAVE_FILE_FAILED, file.getAbsolutePath());
            return false;
        }
        return true;
    }

    /**
     * Using the Story object we have, create a Book object which can be saved as an ePUB file.
     * @return Book object made from Story object.
     */
    private Book generateEpub() {
        // Create CSS.
        Resource css = createCss();
        // Create a title page.
        Resource titlePage = createTitlePage();
        // Create chapters.
        ArrayList<Resource> chapters = story.getChapters().stream().map(this::createChapter).collect(
                Collectors.toCollection(ArrayList::new));
        // Create Book.
        Book book = new Book();
        // Set title, author, and description (summary).
        book.getMetadata().addTitle(story.getTitle());
        book.getMetadata().addAuthor(new Author(story.getAuthor()));
        book.getMetadata().addDescription(story.getSummary()); // TODO Seems appropriate, but unsure what it'll do.
    }

    /**
     * Creates a title page for the story.
     * @return Resource which represents a title page.
     */
    private Resource createTitlePage() {

    }

    /**
     * Creates CSS for the ePUB.
     * @return CSS Resource.
     */
    private Resource createCss() {

    }

    /**
     * Create a chapter Resource from the given Chapter.
     * @param chapter Chapter to create a resource for.
     * @return Chapter Resource.
     */
    private Resource createChapter(Chapter chapter) {

    }

}
