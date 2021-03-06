package bkromhout.fdl;

import bkromhout.fdl.chapter.Chapter;
import bkromhout.fdl.stories.Story;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import nl.siegmann.epublib.domain.*;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.service.MediatypeService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Class which leverages epublib to create ePUB files. This class is meant to be used once and then discarded, so is
 * marked final.
 */
public final class EpubCreator {
    // The Story that this EpubCreator was created with.
    private final Story story;

    /**
     * Create an ePUB generator for the given {@link Story}.
     * @param story Story.
     */
    public EpubCreator(Story story) {
        this.story = story;
    }

    /**
     * Generate an ePUB file and save it at the given location with a filename in the format "[Title] - [Author].epub".
     * @param saveDir Location to save the ePUB at.
     * @return True if ePUB generated successfully, otherwise false (and an error message will have been printed).
     */
    public boolean makeEpub(Path saveDir) {
        return makeEpub(saveDir, Util.makeEpubFname(story.getTitle(), story.getAuthor()));
    }

    /**
     * Generate an ePUB file and save it at the given location with the given file name.
     * @param saveDir  Location to save the ePUB at.
     * @param fileName File name to save the ePUB with. It is assumed that this filename is legal!
     * @return True if ePUB generated successfully, otherwise false (and an error message will have been printed).
     */
    private boolean makeEpub(Path saveDir, String fileName) {
        // Generate and save the ePUB, making sure that the file name is legal for any OS.
        File file = saveDir.resolve(fileName).toFile();
        try {
            new EpubWriter().write(generateEpub(), new FileOutputStream(file));
        } catch (IOException e) {
            Util.logf(C.SAVE_FILE_FAILED, file.getAbsolutePath());
            return false;
        }
        return true;
    }

    /**
     * Using the {@link Story} object we have, create a Book object which can be saved as an ePUB file.
     * @return Book object made from Story object.
     */
    private Book generateEpub() {
        // Create Book.
        Book book = new Book();
        // Set cover image (if present).
        if (story.hasCover()) {
            Resource coverImage = new Resource(story.getCoverImage(),
                    MediatypeService.determineMediaType(story.getCoverImageFileName()));
            book.setCoverImage(coverImage);
        }
        // Add all other image resources.
        // TODO Fix my fork of epublib so that addAll() work the same way as add().
        //book.getResources().addAll(story.getImageResources());
        story.getImageResources().forEach(book::addResource);
        // Set title, author, description (summary), identifier (story url), and publisher (story site).
        book.getMetadata().addTitle(Util.unEscapeAmps(story.getTitle()));
        book.getMetadata().addAuthor(new Author(story.getAuthor()));
        book.getMetadata().addDescription(Util.removeImgTags(Util.cleanHtmlString(story.getSummary())));
        if (story.getUrl() != null)
            book.getMetadata().addIdentifier(new Identifier(Identifier.Scheme.URL, story.getUrl()));
        if (story.getHost() != null) book.getMetadata().addPublisher(story.getHost());
        // Create and add CSS file.
        book.addResource(createCss());
        // Create and add title page.
        Resource titlePage = createTitlePage();
        book.addSection("Title Page", titlePage);
        book.getGuide().addReference(new GuideReference(titlePage, GuideReference.TITLE_PAGE, "Title Page"));
        // Create and add chapter pages.
        ArrayList<Chapter> chapters = story.getChapters();
        for (int i = 0; i < chapters.size(); i++)
            book.addSection(Util.unEscapeAmps(
                    Util.cleanHtmlString(chapters.get(i).title)), createChapter(chapters.get(i), i + 1));
        // Done, should be ready to save now.
        return book;
    }

    /**
     * Creates a title page for the ePUB.
     * @return Resource which represents a title page.
     */
    private Resource createTitlePage() {
        // Build the title page HTML first.
        StringBuilder titleHtml = new StringBuilder();
        // Add the top part with the title and author.
        titleHtml.append(String.format(C.TITLE_PAGE_START, story.getTitle(), story.getAuthor()));

        // Now we add story details if, unless they are null.
        // Add the summary.
        String detail = story.getSummary();
        if (detail != null) titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Summary", detail));

        // Add the series.
        detail = story.getSeries();
        if (detail != null) titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Series", detail));

        // Add the fic type.
        detail = story.getFicType();
        if (detail != null) titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Fic Type", detail));

        // Add the warnings.
        detail = story.getWarnings();
        if (detail != null) titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Warnings", detail));

        // Add the rating.
        detail = story.getRating();
        if (detail != null) titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Rated", detail));

        // Add the genres.
        detail = story.getGenres();
        if (detail != null) titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Genres", detail));

        // Add the characters.
        detail = story.getCharacters();
        if (detail != null) titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Characters", detail));

        // Add the word count.
        int value = story.getWordCount();
        if (value != -1) titleHtml.append(String.format(C.TITLE_PAGE_D_PART, "Word Count", value));

        // Add the chapter count.
        value = story.getChapterCount();
        if (value != -1) titleHtml.append(String.format(C.TITLE_PAGE_D_PART, "Chapter Count", value));

        // Add the date published.
        detail = story.getDatePublished();
        if (detail != null) titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Date Published", detail));

        // Add the date updated.
        detail = story.getDateUpdated();
        if (detail != null) titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Date Last Updated", detail));

        // Add the status.
        detail = story.getStatus();
        if (detail != null) titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Status", detail));

        // Add link to the story, if we have a valid URL (which we might not for local stories).
        detail = story.getUrl();
        if (detail != null && !detail.isEmpty()) {
            try {
                URL url = new URL(detail);
                String linkStr = String.format("<a href=\"%s\">%s</a>", url.toString(), url.toString());
                titleHtml.append(String.format(C.TITLE_PAGE_S_PART, "Story Link", linkStr));
            } catch (MalformedURLException e) {
                Util.loudf(C.NOT_A_URL, detail);
            }
        }

        // Add the bottom part that closes the HTML.
        titleHtml.append(C.TITLE_PAGE_END);
        // Escape pesky characters, because ugh.
        String cleanTitleHtml = Util.cleanHtmlString(titleHtml.toString());
        // Return a new Resource for the title page.
        return new Resource(cleanTitleHtml.getBytes(StandardCharsets.UTF_8), "title.xhtml");
    }

    /**
     * Creates CSS for the ePUB.
     * @return CSS Resource.
     */
    private Resource createCss() {
        // Nothing too fancy for the time being.
        return new Resource(C.CSS.getBytes(StandardCharsets.UTF_8), "style.css");
    }

    /**
     * Create a chapter Resource from the given {@link Chapter}.
     * @param chapter    Chapter to create a resource for.
     * @param chapterNum Number of the chapter.
     * @return Chapter Resource.
     */
    private Resource createChapter(Chapter chapter, int chapterNum) {
        // Nothing too fancy here either.
        return new Resource(Util.cleanHtmlString(chapter.content).getBytes(StandardCharsets.UTF_8),
                String.format("Chapter%d.xhtml", chapterNum));
    }
}
