package proteisynchronization;

import junit.framework.Assert;
import org.junit.Test;
import proteisynchronization.task1.ContentDownloader;
import proteisynchronization.task1.IContentDownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author lelay
 */
public class ContentDownloaderTest {

    @Test
    public void testDownloadContentCorrect() {
        IContentDownloader contentDownloader = new ContentDownloader(2);

        List<String> urls = Arrays.asList("src/main/resources/task1/testHtml1.html",
                                            "src/main/resources/task1/testHtml2.html",
                                            "src/main/resources/task1/testHtml3.html");

        List<String> expectedHtmlList = new ArrayList<>(3);

        for (String url: urls) {
            try (Scanner scanner = new Scanner(new File(url))) {
                StringBuilder builder = new StringBuilder("");

                while (scanner.hasNext()) {
                    builder.append(scanner.nextLine());
                }

                expectedHtmlList.add(builder.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        //run method downloadContent
        List<String> actualHtmlList = contentDownloader.downloadContent(urls);

        //assert sizes are equal
        Assert.assertEquals(expectedHtmlList.size(), actualHtmlList.size());

        for (String html: expectedHtmlList) {
            int index = actualHtmlList.indexOf(html);

            //assert both lists have equal html
            Assert.assertTrue(index != -1);
        }
    }
}
