package proteisynchronization.task3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * @author lelay
 */
public class BlockReader {

    private final BlockingQueue<String> lineBlockQueue;

    private final ExecutorService exService;

    BlockReader(final BlockingQueue<String> lineBlockQueue) {
        this.lineBlockQueue = lineBlockQueue;
        this.exService = Executors.newSingleThreadExecutor();
    }

    public void readBlocks(String sourcePath, int blockSize) {
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
    }

    public void close() {
        exService.shutdown();

        try {
            exService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
