package utils.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Per-thread buffer of captured log lines for the current test method.
 * Populated by a console/log capture mechanism (e.g. ConsoleCaptureManager)
 * and drained by ExtentTestListener to attach an "Execution Logs" node
 * on each test result.
 */
public class TestLogBuffer {

	private static final ThreadLocal<List<String>> BUFFER = ThreadLocal.withInitial(ArrayList::new);

	private TestLogBuffer() {
	}

	public static void append(String line) {
		BUFFER.get().add(line);
	}

	public static void clear() {
		BUFFER.get().clear();
	}

	public static List<String> drainAndClear() {
		List<String> lines = new ArrayList<>(BUFFER.get());
		BUFFER.get().clear();
		return Collections.unmodifiableList(lines);
	}
}
