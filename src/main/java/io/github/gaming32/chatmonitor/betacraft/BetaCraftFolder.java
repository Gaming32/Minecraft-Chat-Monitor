package io.github.gaming32.chatmonitor.betacraft;

import java.io.File;

import io.github.gaming32.chatmonitor.OSInfo;

public final class BetaCraftFolder {
	private static String cachedPath = null;

	public static String get() {
		if (cachedPath != null) {
			return cachedPath;
		}
		if (OSInfo.isWindows()) {
			return cachedPath = windowsPath();
		} else {
			return cachedPath = nixPath();
		}
	}

	private static String windowsPath() {
		return System.getenv("APPDATA") + "\\.betacraft";
	}

	private static String nixPath() {
		String folder;
		if (OSInfo.isLinux()) {
			folder = System.getProperty("user.home") + "/.betacraft";
		} else if (OSInfo.isMac()) {
			folder = System.getProperty("user.home") + "/Library/Application Support/betacraft";
		} else {
			return null;
		}
		new File(folder).mkdirs();
		return folder;
	}
}
