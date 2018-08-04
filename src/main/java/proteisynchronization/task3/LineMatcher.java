package proteisynchronization.task3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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

    public LineMatcher(){
        lineBlockQueue = new ArrayBlockingQueue<>(DEFAULT_CAPACITY, DEFAULT_FAIR);
        matchedLineQueue = new ArrayBlockingQueue<>(DEFAULT_BLOCK_SIZE * DEFAULT_CAPACITY);

        blockSize = DEFAULT_BLOCK_SIZE;
    }

    public void matchLine(int numberOfCheckingThreads,
                          String regularExpression,
                          String sourcePath,
                          String outPath,
                          boolean saveLinesOrder) {

        ExecutorService exService = Executors.newFixedThreadPool(numberOfCheckingThreads + 1);

        //run a reader thread
        Future readerThread = exService.submit(() -> {
            try (Scanner scanner = new Scanner(new File(sourcePath))) {
                while (scanner.hasNext()) {
                    StringBuilder builder = new StringBuilder("");

                    for (int i = 0; i < blockSize; ++i) {
                        if (!scanner.hasNext()) {
                            break;
                        }

                        builder.append(scanner.nextLine());
                        builder.append(System.lineSeparator());
                    }

                    lineBlockQueue.offer(builder.toString(), 3, TimeUnit.SECONDS);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        //run the n of matcher threads
        List<Future> matcherThreadList = new ArrayList<>(numberOfCheckingThreads);

        for (int i = 0; i < numberOfCheckingThreads; ++i) {
            matcherThreadList.add(exService.submit(() -> {
                Pattern pattern = Pattern.compile(regularExpression);

                try {
                    String lineBlock;

                    while ((lineBlock = lineBlockQueue.poll(5, TimeUnit.SECONDS)) != null) {

                        Scanner scanner = new Scanner(lineBlock);
                        scanner.useDelimiter(Pattern.compile("[\\r\\n]+"));

                        while (scanner.hasNext()) {
                            String line = scanner.next();

                            if (pattern.matcher(line).find()) {
                                matchedLineQueue.offer(line, 5, TimeUnit.SECONDS);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("Thread is interrupted");
                    e.printStackTrace();
                }
            }));
        }

        //force main thread to write data to out file
        try (FileWriter writer = new FileWriter(new File(outPath))) {
            while(true) {
                String matchedLine = matchedLineQueue.poll(3, TimeUnit.SECONDS);

                if (matchedLine == null) {
                    break;
                }

                //System.out.println(matchedLine);

                writer.write(matchedLine);
                writer.write(System.lineSeparator());

                writer.flush();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //waiting for threads finished the work and then shutdown the ExecutorService

        while(!readerThread.isDone() && !readerThread.isCancelled()) {
            //do nothing
        }

        for (Future<String> matcherFuture: matcherThreadList) {
            while (!matcherFuture.isDone() && !matcherFuture.isCancelled()) {
                //do nothing
            }
        }

        exService.shutdown();
    }
}
