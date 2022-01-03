package io.github.gaming32.chatmonitor;

public final class OSInfo {
	public static final String NAME = System.getProperty("os.name").toLowerCase();
	public static final String ARCH = System.getProperty("os.arch");
	public static final String VERSION = System.getProperty("os.version").toLowerCase();

	public static boolean isWindows() {
		return NAME.contains("win");
	}

	public static boolean isMac() {
		return NAME.contains("mac");
	}

	public static boolean isLinux() {
		return NAME.contains("nix") || NAME.contains("nux") || NAME.contains("aix");
	}
}
