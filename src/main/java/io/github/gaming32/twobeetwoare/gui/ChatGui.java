package io.github.gaming32.twobeetwoare.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.Session;

import io.github.gaming32.twobeetwoare.ChatMonitor;
import io.github.gaming32.twobeetwoare.LogToMultiplePlaces;

public final class ChatGui extends JFrame {
    public static final String GUI_CLOSED_REASON = "GUI closed";
    public static final String TITLE = "2b2r Chat Viewer";

    private final Session session;
    private final Font minecraftiaFont;

    private JTextArea chatOutput;
    private JScrollPane chatOutputScrollPane;

    public static ChatGui makeChatGui(Session session) {
        ChatGui gui = new ChatGui(session);
        SwingUtilities.invokeLater(() -> gui.setVisible(true));
        return gui;
    }

    public ChatGui(Session session) {
        this.session = session;
        // Font theFont;
        // try {
        //     theFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("Minecraftia-Regular.ttf"));
        // } catch (Exception e) {
        //     theFont = null;
        // }
        // minecraftiaFont = theFont;
        minecraftiaFont = null;

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
        chatOutput = new JTextArea();
        chatOutput.setLineWrap(true);
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
                LogToMultiplePlaces logger = session.getFlag(ChatMonitor.CHAT_LOGGER_KEY);
                logger.println(message);
            }

            ServerboundChatPacket packet = new ServerboundChatPacket(message);
            session.send(packet);
        });

        if (minecraftiaFont != null) {
            chatOutput.setFont(
                minecraftiaFont
                    .deriveFont(chatOutput.getFont().getStyle())
                    .deriveFont(chatOutput.getFont().getSize())
            );
            chatInput.setFont(
                minecraftiaFont
                    .deriveFont(chatInput.getFont().getStyle())
                    .deriveFont(chatInput.getFont().getSize())
            );
        }

        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);

        add(chatOutputScrollPane);
        add(chatInput, BorderLayout.PAGE_END);
    }

    public void println(String s) {
        chatOutput.append(s + "\n");
        chatOutput.setCaretPosition(chatOutput.getDocument().getLength());
    }

    public JTextArea getChatOutput() {
        return chatOutput;
    }

    public JScrollPane getChatOutputScrollPane() {
        return chatOutputScrollPane;
    }
}
