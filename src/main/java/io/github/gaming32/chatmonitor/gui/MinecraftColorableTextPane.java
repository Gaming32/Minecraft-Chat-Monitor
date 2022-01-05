package io.github.gaming32.chatmonitor.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Consumer;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public final class MinecraftColorableTextPane extends JTextPane {
    private static final Consumer<MinecraftColorableTextPane> RESET_TEXT = pane -> pane.setCharacterAttributes(SimpleAttributeSet.EMPTY, true);
    private static final char COLOR_CHAR = '§';
    private static final String LOOKUP = "0123456789abcdefklmnor";
    private static final Consumer<?>[] COLORS = new Consumer<?>[] {
            colorAttribute(0, 0, 0),                          // Black §0
            colorAttribute(240, 100, 67),                     // Dark Blue §1
            colorAttribute(120, 100, 67),                     // Dark Green §2
            colorAttribute(180, 100, 67),                     // Dark Aqua §3
            colorAttribute(0, 100, 67),                       // Dark Red §4
            colorAttribute(300, 100, 67),                     // Dark Purple §5
            colorAttribute(40, 100, 100),                     // Gold §6
            colorAttribute(0, 0, 67),                         // Gray §7
            colorAttribute(0, 0, 33),                         // Dark Gray §8
            colorAttribute(240, 67, 100),                     // Blue §9
            colorAttribute(120, 67, 100),                     // Green §a
            colorAttribute(180, 67, 100),                     // Aqua §b
            colorAttribute(0, 67, 100),                       // Red §c
            colorAttribute(300, 67, 100),                     // Light Purple §d
            colorAttribute(60, 67, 100),                      // Yellow §e
            colorAttribute(0, 0, 100),                        // White §f
            setAttribute(StyleConstants.StrikeThrough, true), // Obfuscated §k
            setAttribute(StyleConstants.Bold, true),          // Bold §l
            setAttribute(StyleConstants.Bold, true),          // Strikethrough §m
            setAttribute(StyleConstants.Underline, true),     // Underline §n
            setAttribute(StyleConstants.Italic, true),        // Italic §o
            RESET_TEXT,                                       // Reset §r
    };

    private static Consumer<MinecraftColorableTextPane> colorAttribute(int h, int s, int v) {
        Color color = Color.getHSBColor(h / 365f, s / 100f, v / 100f);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet attr = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
        return pane -> pane.setCharacterAttributes(attr, false);
    }

    private static Consumer<MinecraftColorableTextPane> setAttribute(Object name, Object value) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet attr = sc.addAttribute(SimpleAttributeSet.EMPTY, name, value);
        return pane -> pane.setCharacterAttributes(attr, false);
    }

    public MinecraftColorableTextPane() {
        super();
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
    }

    @Override
    public void paintComponent(Graphics g) {
        if (g instanceof Graphics2D) {
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        super.paintComponent(g);
    }

    @SuppressWarnings("unchecked")
    public void println(String text) {
        // text = text.replace("\n", System.lineSeparator());
        setCaretPosition(getDocument().getLength());

        int next = text.indexOf(COLOR_CHAR);
        int last = text.length() - 1;
        if (next == -1 || next == last) {
            replaceSelection(text + '\n');
            return;
        }

        int pos = next;
        do {
            int format = LOOKUP.indexOf(Character.toLowerCase(text.charAt(next + 1)));
            if (format != -1) {
                if (pos != next) {
                    replaceSelection(text.substring(pos, next));
                }
                ((Consumer<MinecraftColorableTextPane>)COLORS[format]).accept(this);
                // result.append(COLORS[format]);
                pos = next += 2;
            } else {
                next++;
            }

            next = text.indexOf(COLOR_CHAR, next);
        } while (next != -1 && next < last);

        replaceSelection(text.substring(pos) + '\n');
        RESET_TEXT.accept(this);
    }
}
