package proteisynchronization.task3;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * @author lelay
 */
public class LineMatcher {
    private final int DEFAULT_CAPACITY = 100;

    private final int DEFAULT_BLOCK_SIZE = 50;

    private final boolean DEFAULT_FAIR = true;

    private int blockSize;

    private final BlockingQueue<String> lineBlockQueue;

    private final BlockingQueue<String> matchedLineQueue;

    private final BlockReader reader;

    private final LineWriter writer;

    private final ExecutorService exService;

    public LineMatcher() {
        blockSize = DEFAULT_BLOCK_SIZE;

        exService = Executors.newCachedThreadPool();

        lineBlockQueue = new ArrayBlockingQueue<>(DEFAULT_CAPACITY, DEFAULT_FAIR);
        matchedLineQueue = new ArrayBlockingQueue<>(DEFAULT_BLOCK_SIZE * DEFAULT_CAPACITY);

        reader = new BlockReader(lineBlockQueue);
        writer = new LineWriter(matchedLineQueue);
    }

    public void matchLine(int numberOfCheckingThreads,
                          String regularExpression,
                          String sourcePath,
                          String outPath,
                          boolean saveLinesOrder) {
        //prepare the tasks
        List<Callable<List<String>>> taskList = new ArrayList<>(numberOfCheckingThreads);

        for (int i = 0; i < numberOfCheckingThreads; ++i) {
            taskList.add(() -> {
                Pattern pattern = Pattern.compile(regularExpression);

                String lineBlock = lineBlockQueue.poll(3, TimeUnit.SECONDS);

                if (lineBlock == null) {
                    return null;
                }

                Scanner scanner = new Scanner(lineBlock);
                scanner.useDelimiter(Pattern.compile("[\\r\\n]+"));

                List<String> matchedLinesList = new ArrayList<>();

                while (scanner.hasNext()) {
                    String line = scanner.next();

                    if (pattern.matcher(line).find()) {
                        matchedLinesList.add(line);
                    }
                }

                return matchedLinesList;
            });
        }

        //run reader thread
        reader.readBlocks(sourcePath, blockSize);

        //run writer thread
        writer.writeLines(outPath);

        //run matcher threads
        boolean stopFlag = false;

        try {
            while (!stopFlag) {
                List<Future<List<String>>> futureList = exService.invokeAll(taskList, 4, TimeUnit.SECONDS);

                for (Future<List<String>> future: futureList) {
                    List<String> matcherResultList = future.get();

                    if (matcherResultList == null) {
                        stopFlag = true;
                        break;
                    }

                    for (String line: matcherResultList) {
                        matchedLineQueue.offer(line, 2, TimeUnit.SECONDS);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        reader.close();
        writer.close();

        exService.shutdown();

        try {
            exService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
