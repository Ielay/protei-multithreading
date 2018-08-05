package proteisynchronization.task3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * @author lelay
 */
public class LineWriter {

    private final ExecutorService executorService;

    private final BlockingQueue<String> matchedLineQueue;

    LineWriter(BlockingQueue<String> matchedLineQueue) {
        this.matchedLineQueue = matchedLineQueue;
        executorService = Executors.newSingleThreadExecutor();
    }

    public void writeLines(String outPath) {
        Future writerFuture = executorService.submit(() -> {
            try (FileWriter writer = new FileWriter(new File(outPath))) {
                while (true) {
                    String matchedLine = matchedLineQueue.poll(3, TimeUnit.SECONDS);

                    if (matchedLine == null) {
                        break;
                    }

                    //System.out.println(matchedLine);

                    writer.write(matchedLine);
                    writer.write(System.lineSeparator());
                }

                writer.flush();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void close() {
        executorService.shutdown();

        try {
            executorService.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
