package proteisynchronization.task1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * @author lelay
 */
public class ContentDownloader implements IContentDownloader {

    private final ExecutorService exService;

    private int timeout;

    private final int DEFAULT_TIMEOUT = 10; //seconds

    public ContentDownloader(int poolSize) {
        exService = Executors.newFixedThreadPool(poolSize);
        timeout = DEFAULT_TIMEOUT;
    }

    @Override
    public List<String> downloadContent(List<String> urls) {
        List<String> htmlList = new ArrayList<>(urls.size());

        List<Future<String>> futureList = new ArrayList<>(urls.size());

        for (String url: urls) {
            futureList.add(exService.submit(() -> {
                String resultHtml = null;

                try (Scanner scanner = new Scanner(new File(url))){
                    StringBuilder resultBuilder = new StringBuilder("");

                    while (scanner.hasNext()) {
                        resultBuilder.append(scanner.nextLine());
                    }

                    resultHtml = resultBuilder.toString();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return resultHtml;
            }));
        }

        for (Future<String> future: futureList) {
            try {
                htmlList.add(future.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }

        return htmlList;
    }

    public void close() {
        exService.shutdown();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
