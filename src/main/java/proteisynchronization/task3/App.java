package proteisynchronization.task3;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author lelay
 */
public class App {
    public static void main(String[] args) {
        LineMatcher matcher = new LineMatcher();

        for (int i = 1; i <= 10; ++i) {
            long startTime = System.currentTimeMillis();
            matcher.matchLine(i,
                    "война",
                    "src/main/resources/task3/testText1.txt",
                    "src/main/resources/task3/outTestText1.txt",
                    false);
            System.out.println(System.currentTimeMillis() - startTime  + " Millis");
        }
    }
}
