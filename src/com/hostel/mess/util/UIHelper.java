package com.hostel.mess.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

/**
 * Utility helper for creating consistent, modern Swing & AWT components.
 * Demonstrates: Encapsulation, static factory methods, custom painting with AWT Graphics2D.
 */
public final class UIHelper {

    private UIHelper() {}

    /**
     * Creates a styled modern JButton.
     */
    public static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppConstants.FONT_BODY_BOLD);
        btn.setForeground(fgColor);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        return btn;
    }

    /**
     * Creates a primary action button.
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, AppConstants.COLOR_PRIMARY, Color.WHITE);
    }

    /**
     * Creates a danger/delete action button.
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, AppConstants.COLOR_DANGER, Color.WHITE);
    }

    /**
     * Creates a success action button.
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, AppConstants.COLOR_SUCCESS, Color.WHITE);
    }

    /**
     * Creates a secondary/light action button.
     */
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, new Color(220, 224, 230), AppConstants.COLOR_TEXT_DARK);
    }

    /**
     * Creates a styled input JTextField with proper padding and border.
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(AppConstants.FONT_BODY);
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 32));
        Border padding = BorderFactory.createEmptyBorder(4, 8, 4, 8);
        Border line = BorderFactory.createLineBorder(new Color(200, 205, 215), 1, true);
        tf.setBorder(BorderFactory.createCompoundBorder(line, padding));
        return tf;
    }

    /**
     * Creates a header banner panel.
     */
    public static JPanel createHeaderBanner(String title, String subtitle) {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle gradient
                g2.setColor(AppConstants.COLOR_PRIMARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setLayout(new java.awt.BorderLayout(10, 5));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JPanel titlePanel = new JPanel(new java.awt.GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppConstants.FONT_HEADER_LARGE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(AppConstants.FONT_BODY);
        lblSubtitle.setForeground(new Color(220, 230, 245));

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);

        header.add(titlePanel, java.awt.BorderLayout.WEST);
        return header;
    }

    /**
     * Creates a styled card container panel.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(AppConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 238), 1, true),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return card;
    }
}
