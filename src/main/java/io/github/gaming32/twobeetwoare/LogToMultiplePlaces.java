package io.github.gaming32.twobeetwoare;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextArea;

import com.github.steveice10.packetlib.Session;

import io.github.gaming32.twobeetwoare.gui.ChatGui;

public final class LogToMultiplePlaces implements AutoCloseable {
    private static final SimpleDateFormat LOG_FILE_DATE_FORMAT = new SimpleDateFormat("yyyyLLdd-HHmmss");
    private static final SimpleDateFormat LOG_REC_DATE_FORMAT = new SimpleDateFormat("yyyy-LL-dd HH:mm:ss");

    private final PrintWriter fileLogger;
    private final Session session;

    public LogToMultiplePlaces(Session session) throws IOException {
        fileLogger = new PrintWriter(
            new FileOutputStream(
                new StringBuilder("2b2r-chat-")
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
            ChatGui chatGui = session.getFlag(ChatMonitor.CHAT_GUI_KEY);
            if (chatGui != null) {
                JTextArea chatOutput = chatGui.getChatOutput();
                // JScrollBar chatOutputScrollBar = chatGui.getChatOutputScrollPane().getVerticalScrollBar();
                // boolean scrollDown = chatOutputScrollBar.getValue() + chatOutputScrollBar.getModel().getExtent() >= chatOutputScrollBar.getMaximum();
                chatOutput.append(strippedFormatting + "\n");
                chatOutput.setCaretPosition(chatOutput.getDocument().getLength());
                // if (scrollDown) {
                // }
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
