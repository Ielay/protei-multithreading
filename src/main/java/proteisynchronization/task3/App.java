package proteisynchronization.task3;

/**
 * @author lelay
 */
public class App {
    public static void main(String[] args) {
        LineMatcher matcher = new LineMatcher();

        matcher.matchLine(5,
                        "война",
                        "src/main/resources/task3/testText1.txt",
                        "src/main/resources/task3/outTestText1.txt",
                        false);
    }
}
