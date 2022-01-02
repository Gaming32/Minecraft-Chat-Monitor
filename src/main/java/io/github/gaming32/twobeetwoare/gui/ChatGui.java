package io.github.gaming32.twobeetwoare.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import com.github.steveice10.packetlib.Session;

public final class ChatGui extends JFrame {
    public static final String TITLE = "2b2r Chat Viewer";

    private final Session session;
    private JTextArea chatOutput;
    private JScrollPane chatOutputScrollPane;

    public static ChatGui makeChatGui(Session session) {
        ChatGui gui = new ChatGui(session);
        SwingUtilities.invokeLater(() -> gui.setVisible(true));
        return gui;
    }

    public ChatGui(Session session) {
        this.session = session;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        createComponents();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                session.disconnect("GUI closed");
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

        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);

        add(chatOutputScrollPane);
    }

    public JTextArea getChatOutput() {
        return chatOutput;
    }

    public JScrollPane getChatOutputScrollPane() {
        return chatOutputScrollPane;
    }
}
