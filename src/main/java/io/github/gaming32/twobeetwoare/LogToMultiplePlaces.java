package io.github.gaming32.twobeetwoare;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class LogToMultiplePlaces implements AutoCloseable {
    private static final SimpleDateFormat LOG_FILE_DATE_FORMAT = new SimpleDateFormat("yyyyLLdd-HHmmss");
    private static final SimpleDateFormat LOG_REC_DATE_FORMAT = new SimpleDateFormat("yyyy-LL-dd HH:mm:ss");

    private final PrintWriter fileLogger;

    public LogToMultiplePlaces() throws IOException {
        fileLogger = new PrintWriter(
            new FileOutputStream(
                new StringBuilder("2b2r-chat-")
                    .append(LOG_FILE_DATE_FORMAT.format(new Date()))
                    .append(".log")
                    .toString()
            ), true
        );
    }

    public void println(String text) {
        System.out.println(MessageFormatter.convertToAnsi(text));
        fileLogger.println(
            new StringBuilder(38)
                .append('[')
                .append(LOG_REC_DATE_FORMAT.format(new Date()))
                .append("] ")
                .append(MessageFormatter.stripFormatting(text))
        );
    }

    @Override
    public void close() throws IOException {
        fileLogger.close();
    }
}