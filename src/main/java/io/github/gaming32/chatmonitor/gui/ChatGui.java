package io.github.gaming32.chatmonitor.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.Session;

import io.github.gaming32.chatmonitor.ChatMonitorConstants;
import io.github.gaming32.chatmonitor.LogToMultiplePlaces;

public final class ChatGui extends JFrame {
    public static final String GUI_CLOSED_REASON = "GUI closed";
    public static final String TITLE = "Minecraft Chat Viewer";

    private final Session session;
    private Font useFont;

    private MinecraftColorableTextPane chatOutput;
    private JScrollPane chatOutputScrollPane;

    public static ChatGui makeChatGui(Session session) {
        ChatGui gui = new ChatGui(session);
        SwingUtilities.invokeLater(() -> gui.setVisible(true));
        return gui;
    }

    public ChatGui(Session session) {
        this.session = session;
        try (InputStream is = getClass().getResourceAsStream("Minecraftia-Regular.ttf")) {
            Font minecraftia = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(minecraftia);
            useFont = minecraftia;
        } catch (Exception e) {
            useFont = null;
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        createComponents();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                session.disconnect(GUI_CLOSED_REASON);
            }
        });

        pack();
        setTitle(TITLE);
    }

    private void createComponents() {
        chatOutput = new MinecraftColorableTextPane();
        chatOutput.setEditable(false);
        chatOutput.setAutoscrolls(true);
        chatOutputScrollPane = new JScrollPane(chatOutput);
        chatOutputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        chatOutputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        chatOutputScrollPane.setPreferredSize(new Dimension(640, 400));

        JTextField chatInput = new JTextField();
        chatInput.addActionListener(e -> {
            String message = chatInput.getText();
            chatInput.setText("");

            if (message.length() == 0) {
                return;
            }
            if (message.charAt(0) == '/') {
                LogToMultiplePlaces logger = session.getFlag(ChatMonitorConstants.CHAT_LOGGER_KEY);
                logger.println(message);
            }

            ServerboundChatPacket packet = new ServerboundChatPacket(message);
            session.send(packet);
        });

        if (useFont != null) {
            chatOutput.setFont(useFont);
            chatInput.setFont(useFont);
        } else {
            chatOutput.setFont(chatOutput.getFont().deriveFont(13f));
        }

        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);

        add(chatOutputScrollPane);
        add(chatInput, BorderLayout.PAGE_END);
    }

    public void println(String s) {
        chatOutput.setEditable(true);
        chatOutput.println(s);
        chatOutput.setEditable(false);
    }

    public MinecraftColorableTextPane getChatOutput() {
        return chatOutput;
    }

    public JScrollPane getChatOutputScrollPane() {
        return chatOutputScrollPane;
    }
}
