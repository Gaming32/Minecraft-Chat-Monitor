package io.github.gaming32.chatmonitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.github.steveice10.packetlib.Session;

import io.github.gaming32.chatmonitor.gui.ChatGui;

public final class LogToMultiplePlaces implements AutoCloseable {
    private static final SimpleDateFormat LOG_FILE_DATE_FORMAT = new SimpleDateFormat("yyyyLLdd-HHmmss");
    private static final SimpleDateFormat LOG_REC_DATE_FORMAT = new SimpleDateFormat("yyyy-LL-dd HH:mm:ss");

    private final PrintWriter fileLogger;
    private final Session session;

    public LogToMultiplePlaces(Session session) throws IOException {
        fileLogger = new PrintWriter(
            new FileOutputStream(
                new StringBuilder(session.getFlag(ChatMonitorConstants.CHAT_SERVER_ADDRESS_KEY))
                    .append("-chat-")
                    .append(LOG_FILE_DATE_FORMAT.format(new Date()))
                    .append(".log")
                    .toString()
            ), true
        );
        this.session = session;
    }

    public void println(String text) {
        System.out.println(MessageFormatter.convertToAnsi(text));
        String strippedFormatting = MessageFormatter.stripFormatting(text);
        fileLogger.println(
            new StringBuilder(38)
                .append('[')
                .append(LOG_REC_DATE_FORMAT.format(new Date()))
                .append("] ")
                .append(strippedFormatting)
        );
        try {
            ChatGui chatGui = session.getFlag(ChatMonitorConstants.CHAT_GUI_KEY);
            if (chatGui != null) {
                chatGui.println(strippedFormatting);
            }
        } catch (Exception e) {
            // Maybe the GUI was closed
        }
    }

    @Override
    public void close() throws IOException {
        fileLogger.close();
    }
}
