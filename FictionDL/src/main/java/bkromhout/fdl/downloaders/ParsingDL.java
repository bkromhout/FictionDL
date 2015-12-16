package bkromhout.fdl.downloaders;

import bkromhout.fdl.*;
import bkromhout.fdl.rx.RxMakeChapters;
import bkromhout.fdl.rx.RxOkHttpCall;
import bkromhout.fdl.rx.RxSortChapters;
import bkromhout.fdl.storys.Story;
import com.squareup.okhttp.Request;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Base class for downloaders which get stories by scraping their site HTML, and then generating story EPUB files by
 * parsing and cleaning the scraped HTML data.
 */
public abstract class ParsingDL extends Downloader {
    /**
     * CSS selector to extract chapter content from a chapter's original, raw HTML.
     */
    protected String chapTextSelector;

    /**
     * Create a new ParsingDL.
     * @param fictionDL        FictionDL object which owns this downloader.
     * @param storyClass       The concrete subclass of Story which this downloader uses.
     * @param siteName         Human-readable site name for this downloader.
     * @param storyUrls        List of story URLs to be downloaded.
     * @param chapTextSelector CSS selector used to extract chapter content from chapters' raw HTML. (If all of the
     *                         chapter's text cannot be extracted with one CSS selector, the subclass should pass null
     *                         for this and override {@link #extractChapText(Chapter)}.)
     */
    protected ParsingDL(FictionDL fictionDL, Class<? extends Story> storyClass, String siteName,
                        HashSet<String> storyUrls, String chapTextSelector) {
        super(fictionDL, storyClass, siteName, storyUrls);
        this.chapTextSelector = chapTextSelector;
    }

    /**
     * Download the chapters of a story, get their titles, extract their content, then process everything and save the
     * story as an ePUB file.
     * @param story Story to download and save.
     * @see Story
     */
    @Override
    protected void downloadStory(Story story) {
        // Create Chapter objects.
        ArrayList<Chapter> chapters = (ArrayList<Chapter>) downloadStoryChaps(story)
                .doOnNext(this::generateChapTitle) // Make titles for the chapters.
                .doOnNext(this::extractChapText) // Extract chapter content from raw HTML.
                .doOnNext(this::sanitizeChap) // Clean up chapter content.
                .doOnCompleted(() -> Util.log(C.SANITIZING_CHAPS))
                .observeOn(Schedulers.immediate())
                .toList() // Get the chapters as a list.
                .toBlocking()
                .single();
        // Make sure we got all of the chapters. If we didn't we won't continue.
        if (chapters == null || story.getChapterUrls().size() != chapters.size()) {
            Util.log(C.SOME_CHAPS_FAILED);
            storyProcessed(); // Update progress.
            return;
        }
        // Associate the chapters with the story.
        story.setChapters(chapters);
        // Save the story as an ePUB file.
        Util.logf(C.SAVING_STORY);
        new EpubCreator(story).makeEpub(FictionDL.outPath);
        Util.log(C.DONE + "\n");
        storyProcessed(); // Update progress.
    }

    /**
     * Downloads the raw HTML from the chapter URLs in the given story, uses them to create Chapter objects, and makes
     * sure that those Chapter objects are assigned the correct chapter numbers.
     * <p>
     * Important! This method calls {@link rx.Observable#subscribeOn(Scheduler)} and passes it {@link
     * Schedulers#newThread()}.
     * <p>
     * Note that there is no guarantee that all of the chapter HTMLs will be successfully downloaded, and this method
     * will allow that to occur silently. Therefore, at some point a check must be made to ensure that the number of
     * Chapters in the stream is equal to the number of chapter URLs in the story.
     * @param story Story to download chapters for.
     * @return Observable which emits Chapters that have their {@link Chapter#rawHtml rawHtml} and {@link Chapter#number
     * number} fields filled in.
     * @see Chapter
     */
    private Observable<Chapter> downloadStoryChaps(Story story) {
        // Create an observable to generate chapter numbers.
        Observable<Integer> chapNums = Observable.range(1, story.getChapterCount());
        // Get chapters.
        return Observable
                .from(story.getChapterUrls()) // Create an observable using the chapter URLs from the story.
                .doOnSubscribe(() -> Util.logf(C.DL_CONTENT_FOR, Util.unEscapeAmps(story.getTitle())))
                .subscribeOn(Schedulers.newThread())
                .map(url -> new Request.Builder().url(url).build()) // Create OkHttp Requests from URLs.
                .compose(new RxOkHttpCall()) // Get Responses by executing Requests.
                .compose(new RxMakeChapters()) // Create Chapter objects.
                .observeOn(Schedulers.computation())
                .filter(chapter -> chapter.rawHtml != null) // Filter out any nulls, they're chapters we failed to make.
                .compose(new RxSortChapters(story::slowChapSort)) // Sort chapters back into the correct order.
                .zipWith(chapNums, this::assignChapNums); // Add numbers to the chapters.
    }

    /**
     * Assigns the given number to the given chapter.
     * @param chapter Chapter object.
     * @param number  Number of the chapter in the story.
     * @return The given Chapter object with its {@link Chapter#number number} field filled in.
     * @see Chapter
     */
    private Chapter assignChapNums(Chapter chapter, Integer number) {
        chapter.number = number;
        return chapter;
    }

    /**
     * Creates a title string for the story chapter that the given Chapter object represents and stores it in its {@link
     * Chapter#title title} field.
     * <p>
     * It is assumed that the given Chapter object has had both the chapter number and the raw chapter html filled in.
     * <p>
     * By default, the generated titles will be of the form "Chapter 1", "Chapter 2", etc., based on the chapter number.
     * Subclasses should override this method in order to generate more specific titles.
     * @param chapter Chapter object.
     * @see Chapter
     */
    protected void generateChapTitle(Chapter chapter) {
        // Make sure the chapter was assigned.
        if (chapter.number == -1) throw new IllegalStateException(C.CHAP_NUM_NOT_ASSIGNED);
        // Create a chapter title using the chapter number.
        chapter.title = String.format("Chapter %d", chapter.number);
    }

    /**
     * Takes the given Chapter objects and fills in its {@link Chapter#content content} field by extracting a portion of
     * its raw HTML.
     * <p>
     * By default, this method uses {@link #chapTextSelector the selector string which this ParsingDL currently has set}
     * to choose what part of the raw HTML to extract. This selector string is set by a concrete subclass when it calls
     * super in its constructor to create this ParsingDL. If a subclass needs some additional logic to extract chapter
     * content, it should override this method.
     * @param chapter Chapter object.
     * @see Chapter
     */
    protected void extractChapText(Chapter chapter) {
        // Get the chapter's text, keeping all HTML formatting intact.
        String chapterText = chapter.rawHtml.select(chapTextSelector).first().html();
        // Put the chapter's text into a chapter HTML template.
        chapter.content = String.format(C.CHAPTER_PAGE, chapter.title, chapter.title, chapterText);
    }

    /**
     * Cleans up the String in the given Chapter's {@link Chapter#content content} field so that it is safe to put in an
     * ePUB file.
     * <p>
     * This default implementation doesn't do anything. Subclasses which need to do some sanitizing tasks should
     * override this method.
     * @param chapter Chapter object.
     * @see Chapter
     */
    protected void sanitizeChap(Chapter chapter) {
        // Does nothing by default.
    }
}
