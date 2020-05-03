import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class TestSuite {
    int successes = 0;
    int failures = 0;
    int errors = 0;

    private static String red(String s) { return "\u001B[31m" + s + "\u001B[0m"; }
    private static String green(String s) { return "\u001B[32m" + s + "\u001B[0m"; }
    private static String error(String s) { return "\u001B[31m\u001B[1m" + s + "\u001B[0m"; }

    static class Test {
        String name;
        Runnable code;
        Test(String name, Runnable code) { this.name = name; this.code = code; }
    }

    abstract Test[] getTests();

    void expect(boolean condition, String message) {
        if (condition) {
            successes++;
            System.out.print(green(" [PASS]"));
        } else {
            failures++;
            System.err.print(red(" [FAIL] " + message));
        }
    }

    void expectEqual(int actual, int expected) {
        expect(actual == expected, "EXPECTED " + expected + " BUT GOT " + actual);
    }

    void expectWithin(double value, double expected, double tolerance) {
        System.out.print(" should be within " + tolerance + " of " + expected);
        expect(Math.abs(expected - value) <= tolerance, "BUT GOT " + value);
    }

    void expectEqual(Object actual, Object expected) {
        expect(Objects.deepEquals(actual, expected), "EXPECTED " + expected + " BUT GOT " + actual);
    }

    void expectMatch(String string, String pattern) {
        expect(string.matches(pattern), string + " DOES NOT MATCH " + pattern);
    }

    void expectThrows(Runnable code, Class<?> expectedClass, String expectedMessage) {
        System.out.print(" should throw " + expectedClass.getSimpleName());
        try {
            code.run();
            expect(false, "BUT DID NOT THROW");
        } catch (Exception e) {
            expect(expectedClass.isInstance(e), "BUT THREW " + e.getClass().getSimpleName());
            System.out.print("\n    with message \"" + expectedMessage + "\"");
            expect(e.getMessage().equals(expectedMessage), " BUT GOT \"" + e.getMessage() + "\"");
        }
    }

    void expectStandardErrorToHave(Runnable code, String expected) {
        System.out.print(" should write \"" + expected + "\" to stderr");
        var oldSystemErr = System.err;
        var bytes = new ByteArrayOutputStream();
        System.setErr(new PrintStream(bytes));
        code.run();
        System.out.flush();
        System.setErr(oldSystemErr);
        var actual = bytes.toString();
        expect(actual.startsWith(expected), "BUT WROTE \"" + actual + "\"");
    }

    void expectStandardOutput(Runnable code, Consumer<String> consumer) {
        System.out.print(" stdout");
        var oldSystemOut = System.out;
        var bytes = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bytes));
        code.run();
        System.out.flush();
        System.setOut(oldSystemOut);
        var actual = bytes.toString();
        consumer.accept(actual);
    }

    public static void run(TestSuite suite) {
        suite.successes = 0;
        suite.failures = 0;
        suite.errors = 0;
        for (var test: suite.getTests()) {
            System.out.println();
            System.out.print("• " + test.name);
            try {
                test.code.run();
            } catch (Exception e) {
                suite.errors++;
                System.err.print(" " + error("[ERROR] " + e.getMessage()));
            }
        }
        System.out.println();
        System.out.println("-".repeat(80));
        System.out.println("Total  : " + (suite.successes + suite.failures + suite.errors));
        if (suite.successes > 0) System.out.println(green("Passed : " + suite.successes));
        if (suite.failures > 0) System.out.println(red("Failed : " + suite.failures));
        if (suite.errors > 0) System.out.println(error("Errors : " + suite.errors));
    }
}
