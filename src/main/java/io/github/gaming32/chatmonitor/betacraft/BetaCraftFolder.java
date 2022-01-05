package io.github.gaming32.chatmonitor.betacraft;

import io.github.gaming32.chatmonitor.OSInfo;

public final class BetaCraftFolder {
	private static String cachedPath = null;

	public static String get() {
		return cachedPath != null ? cachedPath : (cachedPath = get0());
	}

	private static String get0() {
		if (OSInfo.isWindows()) {
			return System.getenv("APPDATA") + "\\.betacraft";
		}
		if (OSInfo.isLinux()) {
			return System.getProperty("user.home") + "/.betacraft";
		}
		if (OSInfo.isMac()) {
			return System.getProperty("user.home") + "/Library/Application Support/betacraft";
		}
		return null;
	}
}
