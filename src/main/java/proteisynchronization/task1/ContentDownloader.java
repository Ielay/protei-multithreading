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

    private ExecutorService exService;

    public ContentDownloader(int poolSize) {
        exService = Executors.newFixedThreadPool(poolSize);
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

        while(!futureList.isEmpty()) {
            for (Iterator<Future<String>> it = futureList.iterator(); it.hasNext(); ) {
                Future<String> task = it.next();

                if (task.isDone()) {
                    try {
                        htmlList.add(task.get());
                        it.remove();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        exService.shutdown();

        return htmlList;
    }
}
