package bkromhout.fdl.downloaders;

import bkromhout.fdl.*;
import bkromhout.fdl.rx.RxMakeChapters;
import bkromhout.fdl.rx.RxOkHttpCall;
import bkromhout.fdl.rx.RxSortChapters;
import bkromhout.fdl.storys.Story;
import com.squareup.okhttp.Request;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Base class for downloaders which get stories by scraping their site HTML, and then generating story ePUB files by
 * parsing and cleaning the scraped HTML data.
 */
public abstract class ParsingDL extends Downloader {
    /**
     * CSS selector string to extract chapter content from {@link Chapter#rawHtml}.
     */
    protected String chapTextSelector;

    /**
     * Create a new {@link ParsingDL}.
     * @param fictionDL        FictionDL object which owns this downloader.
     * @param storyClass       The concrete subclass of Story which this downloader uses.
     * @param site             Site that this downloader services.
     * @param storyUrls        List of story urls to be downloaded.
     * @param chapTextSelector CSS selector used to extract chapter content from {@link Chapter#rawHtml}. (If all of the
     *                         chapter's text cannot be extracted with one CSS selector, the subclass should pass null
     *                         for this and override {@link #extractChapText(Chapter)}.)
     */
    protected ParsingDL(FictionDL fictionDL, Class<? extends Story> storyClass, Site site, HashSet<String> storyUrls,
                        String chapTextSelector) {
        super(fictionDL, storyClass, site, storyUrls);
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
     * Uses {@link Story#chapterUrls} to create {@link Chapter Chapters}.
     * <p>
     * The {@link Chapter Chapters} that the returned Observable emits are guaranteed to have {@link Chapter#rawHtml}
     * and {@link Chapter#number} populated. <i>However</i>, there is no guarantee that <i>all</i> of the urls will be
     * successfully downloaded and made into {@link Chapter Chapters}, and the numbers assigned may be incorrect if some
     * downloads fail.
     * <p>
     * Side-effect: This method calls rx.Observable#subscribeOn(Scheduler) and passes it Schedulers#newThread().
     * @param story Story to download chapters for.
     * @return Observable which emits {@link Chapter Chapters} that have their {@link Chapter#rawHtml rawHtml} and
     * {@link Chapter#number number} fields filled in.
     * @see Chapter
     */
    private Observable<Chapter> downloadStoryChaps(Story story) {
        // Create an observable to generate chapter numbers.
        Observable<Integer> chapNums = Observable.range(1, story.getChapterCount());
        // Get chapters.
        return Observable
                .from(story.getChapterUrls()) // Create an observable using the chapter urls from the story.
                .doOnSubscribe(() -> Util.logf(C.DL_CONTENT_FOR, Util.unEscapeAmps(story.getTitle())))
                .subscribeOn(Schedulers.newThread())
                .map(url -> new Request.Builder().url(url).build()) // Create OkHttp Requests from urls.
                .compose(new RxOkHttpCall()) // Get Responses by executing Requests.
                .compose(new RxMakeChapters()) // Create Chapter objects.
                .observeOn(Schedulers.computation())
                .filter(chapter -> chapter.rawHtml != null) // Filter out any nulls, they're chapters we failed to make.
                .compose(new RxSortChapters(story::slowChapSort)) // Sort chapters back into the correct order.
                .zipWith(chapNums, this::assignChapNums); // Add numbers to the chapters.
    }

    /**
     * Populates {@link Chapter#number} with the given integer.
     * @param chapter Chapter object.
     * @param number  Number of the chapter in the story.
     * @return The given {@link Chapter} with {@link Chapter#number} populated.
     * @see Chapter
     */
    private Chapter assignChapNums(Chapter chapter, Integer number) {
        chapter.number = number;
        return chapter;
    }

    /**
     * Populates {@link Chapter#title} with a generated title string.
     * <p>
     * By default, the generated titles will be of the form "Chapter 1", "Chapter 2", etc., based on {@link
     * Chapter#number}. Subclasses should override this method in order to generate more specific titles.
     * <p>
     * It is assumed that both {@link Chapter#number} and {@link Chapter#rawHtml} are populated already.
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
     * Takes the given {@link Chapter} and populates {@link Chapter#content} with a subset of the HTML from {@link
     * Chapter#rawHtml}.
     * <p>
     * By default, this method uses the CSS selector string that this ParsingDL currently has set in {@link
     * #chapTextSelector} to choose what the subset of the raw HTML to use.<br/>The selector string is set by a concrete
     * subclass when it calls super() in its constructor to create this ParsingDL.
     * <p>
     * If a subclass needs some additional logic to populate {@link Chapter#content}, it should override this method.
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
     * Cleans up the String in {@link Chapter#content} so that it is safe to put in an ePUB file.
     * <p>
     * This default implementation doesn't do anything. Subclasses which actually need to clean the content string
     * should override this method.
     * @param chapter Chapter object.
     * @see Chapter
     */
    protected void sanitizeChap(Chapter chapter) {
        // Does nothing by default.
    }
}
