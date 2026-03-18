package org.bukkit.lazybukkit.vanilla;

/**
 * Result of a single vanilla mechanics test.
 */
public final class VanillaTestResult {
    private final String testName;
    private final String category;
    private final boolean passed;
    private final String message;
    private final long durationMillis;

    private VanillaTestResult(String testName, String category,
                              boolean passed, String message,
                              long durationMillis) {
        this.testName = testName;
        this.category = category;
        this.passed = passed;
        this.message = message;
        this.durationMillis = durationMillis;
    }

    public static VanillaTestResult pass(String testName, String category,
                                         String message, long durationMillis) {
        return new VanillaTestResult(testName, category, true, message, durationMillis);
    }

    public static VanillaTestResult fail(String testName, String category,
                                         String message, long durationMillis) {
        return new VanillaTestResult(testName, category, false, message, durationMillis);
    }

    public String getTestName() {
        return testName;
    }

    public String getCategory() {
        return category;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    @Override
    public String toString() {
        return (passed ? "PASS" : "FAIL") + " [" + category + "] "
                + testName + ": " + message
                + " (" + durationMillis + "ms)";
    }
}
