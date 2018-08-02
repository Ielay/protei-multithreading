package proteisynchronization.task1;

import java.util.List;

/**
 * @author lelay
 */
public interface IContentDownloader {

    List<String> downloadContent(List<String> urls);

}
