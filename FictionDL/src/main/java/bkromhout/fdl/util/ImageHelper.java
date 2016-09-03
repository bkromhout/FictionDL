package bkromhout.fdl.util;

import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.service.MediatypeService;
import okhttp3.HttpUrl;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * This class assists with downloading images referenced by {@code <img>} tags in html strings and turning them into
 * Resources which can be used in an ePub file. Any images which cannot be downloaded will be stripped instead.
 */
public class ImageHelper {
    /**
     * Element to scan for images to inline. Will be modified if images are found by either changing the src
     * attribute's value (if the image is downloaded) or removing the tag (if the image cannot be downloaded).
     */
    private Element element;
    /**
     * Base name to use for resource names.
     */
    private String baseName;
    /**
     * List of image resources which have been inlined.
     */
    private List<Resource> imageResources = new ArrayList<>();

    /**
     * Creates a new {@link ImageHelper} instance.
     * @param element  The element to scan for {@code <img>} tags to inline.
     * @param baseName The base name to use for the image resources in the ePub. Should be unique for each new {@link
     *                 ImageHelper} instance.
     */
    public ImageHelper(Element element, String baseName) {
        this.element = element;
        this.baseName = baseName;
    }

    /**
     * Triggers the inlining process and eventually returns a list of {@code Resource}s. Any images which cannot be
     * downloaded and inlined will be ignored and have their associated {@code <img>} tags removed.
     * @return List of {@code Resource}s.
     */
    public List<Resource> getImageResources() {
        // Get all img tags and loop through them.
        Elements imgTags = element.select("img");
        for (int i = 0; i < imgTags.size(); i++) handleImg(imgTags.get(i), i);
        // Return image resources which were successfully inlined.
        return imageResources;
    }

    /**
     * Tries to download the image that an {@code <img>} element points to. If we are able to download the image, and it
     * is one of the image formats supported in ePubs, then we modify the {@code <img>} tag's {@code src} attribute to
     * point to a new resource name we generate based on {@link #baseName}. Otherwise, the {@code <img>} tag is removed.
     * @param img    {@code <img>} tag to try and inline.
     * @param number Number of the tag, used to help generate a resource name if needed.
     */
    private void handleImg(Element img, int number) {
        // Get image URL. If we can't figure it out, just remove the tag.
        String imgSrc = img.absUrl("src");
        if (imgSrc.isEmpty()) {
            img.remove();
            return;
        }

        // Try to download the image. If we can't, just remove the tag.
        HttpUrl imgUrl = HttpUrl.parse(imgSrc);
        byte[] imageData = Util.getBinary(imgUrl.toString());
        if (imageData == null) {
            img.remove();
            return;
        }

        // Determine the media type based off of the original file name.
        String imgFileName = imgUrl.pathSegments().get(imgUrl.pathSegments().size() - 1);
        MediaType imgMediaType = MediatypeService.determineMediaType(imgFileName);
        // Ensure this is a valid image media type.
        if (!MediatypeService.isBitmapImage(imgMediaType) && !MediatypeService.SVG.equals(imgMediaType)) {
            img.remove();
            return;
        }

        // Replace element's src value with new resource name.
        String resourceName = baseName + number + getExtension(imgFileName);
        img.attr("src", resourceName);

        // Create resource name and resource, then add resource to list.
        imageResources.add(new Resource(null, imageData, resourceName, imgMediaType));
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.'), fileName.length());
    }
}
