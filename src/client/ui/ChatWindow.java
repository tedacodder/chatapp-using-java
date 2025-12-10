package client.ui;

import client.ChatClient;
import client.ClientReaderThread;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Contest-ready polished ChatWindow with animations, hover effects, avatars, smooth scrolling.
 */
public class ChatWindow extends JFrame {

    private final JPanel messagesPanel;
    private final JScrollPane scrollPane;
    private final JTextField inputField;
    private final JButton sendButton;

    private final ChatClient client;
    private final String username;

    private static final int MAX_BUBBLE_WIDTH = 300;
    private static final int MESSAGE_VERTICAL_SPACE = 4;
    private static final Color WALLPAPER_BG = new Color(48, 48, 48);
    private static final Color WALLPAPER_DOT = new Color(60, 60, 60, 120);

    private static final Color[] PALETTE = new Color[]{
            new Color(37, 211, 102),
            new Color(255, 121, 198),
            new Color(129, 236, 236),
            new Color(255, 234, 167),
            new Color(250, 177, 160),
            new Color(255, 118, 117),
            new Color(189, 195, 199),
            new Color(85, 239, 196)
    };

    private final Map<String, Color> userColors = new HashMap<>();
    private LocalDate lastMessageDate = null;

    public ChatWindow(ChatClient client, String username) {
        this.client = client;
        this.username = username;

        setTitle("Chat - " + username);
        setSize(540, 700);
        setMinimumSize(new Dimension(420, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header with subtle shadow
        JLabel header = new JLabel("  Logged in as: " + username);
        header.setOpaque(true);
        header.setBackground(new Color(35, 39, 42));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,new Color(80,80,80)),
                new EmptyBorder(12,12,12,12)
        ));
        add(header, BorderLayout.NORTH);

        // Messages panel
        messagesPanel = new JPanel() {
            private final BufferedImage pattern = createWallpaperPattern();

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setColor(WALLPAPER_BG);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    if (pattern != null) {
                        for (int x = 0; x < getWidth(); x += pattern.getWidth()) {
                            for (int y = 0; y < getHeight(); y += pattern.getHeight()) {
                                g2.drawImage(pattern, x, y, null);
                            }
                        }
                    }
                } finally {
                    g2.dispose();
                }
            }
        };
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setOpaque(false);
        messagesPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Input panel with placeholder
        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        inputPanel.setBackground(new Color(40, 40, 40));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        inputField.setBackground(new Color(60, 60, 60));
        inputField.setForeground(Color.GRAY);
        inputField.setText("Type a message...");
        inputField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (inputField.getText().equals("Type a message...")) {
                    inputField.setText("");
                    inputField.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (inputField.getText().isEmpty()) {
                    inputField.setText("Type a message...");
                    inputField.setForeground(Color.GRAY);
                }
            }
        });

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setBackground(new Color(10, 132, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        new ClientReaderThread(client, this).start();

        setVisible(true);
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty() && !msg.equals("Type a message...")) {
            client.send(username + ": " + msg);
            appendMessage(username + ": " + msg);
            inputField.setText("");
        }
    }

    public void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            String sender;
            String content;
            int colonIndex = msg.indexOf(':');
            if (colonIndex > 0) {
                sender = msg.substring(0, colonIndex).trim();
                content = msg.substring(colonIndex + 1).trim();
            } else {
                sender = "Unknown";
                content = msg;
            }
            boolean isOwn = sender.equals(username);
            addMessage(sender, escapeHtml(content), isOwn, new Date());
        });
    }

    private void addMessage(String sender, String message, boolean isOwn, Date date) {
        LocalDate messageDate = toLocalDate(date);
        if (lastMessageDate == null || !messageDate.equals(lastMessageDate)) {
            messagesPanel.add(createDateSeparator(messageDate));
            messagesPanel.add(Box.createVerticalStrut(MESSAGE_VERTICAL_SPACE));
            lastMessageDate = messageDate;
        }

        JPanel wrapper = new JPanel(new FlowLayout(isOwn ? FlowLayout.RIGHT : FlowLayout.LEFT, 6, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        Color bubbleColor = isOwn ? PALETTE[0] : getColorForUser(sender);
        Color textColor = isLight(bubbleColor) ? Color.BLACK : Color.WHITE;
        Color nameColor = darken(textColor, 0.25f);

        BubblePanel bubble = new BubblePanel(isOwn, bubbleColor);
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(6, 10, 6, 10));
        bubble.setMaximumSize(new Dimension(MAX_BUBBLE_WIDTH + 40, Integer.MAX_VALUE));
        bubble.setAlignmentX(isOwn ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        // Avatar + name
        JLabel avatar = new JLabel(getInitials(sender));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(true);
        avatar.setBackground(darken(bubbleColor, 0.2f));
        avatar.setBorder(new EmptyBorder(2,6,2,6));
        avatar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(sender);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(nameColor);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bubble.add(avatar);
        bubble.add(Box.createVerticalStrut(2));
        bubble.add(nameLabel);
        bubble.add(Box.createVerticalStrut(2));

        JLabel textLabel = new JLabel("<html><body style='width:" + MAX_BUBBLE_WIDTH + "px'>" + message + "</body></html>");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setOpaque(false);
        textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textLabel.setForeground(textColor);
        bubble.add(textLabel);

        bubble.add(Box.createVerticalStrut(2));
        JLabel timeLabel = new JLabel(new SimpleDateFormat("HH:mm").format(date));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(darken(nameColor, 0.2f));
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        bubble.add(timeLabel);

        wrapper.add(bubble);
        messagesPanel.add(wrapper);
        messagesPanel.add(Box.createVerticalStrut(MESSAGE_VERTICAL_SPACE));

        messagesPanel.revalidate();
        messagesPanel.repaint();

        // Smooth scroll
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            int target = bar.getMaximum();
            Timer timer = new Timer(5, null);
            timer.addActionListener(e -> {
                int current = bar.getValue();
                int step = Math.max(1, (target - current) / 8);
                bar.setValue(Math.min(current + step, target));
                if (bar.getValue() >= target) timer.stop();
            });
            timer.start();
        });
    }

    private JComponent createDateSeparator(LocalDate date) {
        String text;
        LocalDate today = LocalDate.now();
        if (date.equals(today)) text = "Today";
        else if (date.equals(today.minusDays(1))) text = "Yesterday";
        else text = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(200, 200, 200));
        label.setOpaque(true);
        label.setBackground(new Color(80, 80, 80, 140));
        label.setBorder(new EmptyBorder(6, 12, 6, 12));

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalGlue());
        panel.add(label);
        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    private static LocalDate toLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Color getColorForUser(String user) {
        if (user == null) return PALETTE[6];
        String key = user.toLowerCase();
        if (userColors.containsKey(key)) return userColors.get(key);

        int index = Math.abs(key.hashCode()) % PALETTE.length;
        if (index == 0) index = (index + 1) % PALETTE.length;
        Color c = PALETTE[index];
        userColors.put(key, c);
        return c;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private BufferedImage createWallpaperPattern() {
        int w = 20, h = 20;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setComposite(AlphaComposite.Src);
            g2.setColor(new Color(0, 0, 0, 0));
            g2.fillRect(0, 0, w, h);
            g2.setColor(WALLPAPER_DOT);
            g2.fillRect(2, 2, 2, 2);
            g2.fillRect(10, 6, 2, 2);
            g2.fillRect(16, 12, 2, 2);
        } finally {
            g2.dispose();
        }
        return img;
    }

    private static boolean isLight(Color c) {
        double luminance = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255.0;
        return luminance > 0.7;
    }

    private static Color darken(Color c, float factor) {
        float r = c.getRed() / 255f;
        float g = c.getGreen() / 255f;
        float b = c.getBlue() / 255f;
        r = Math.max(0f, r * (1f - factor));
        g = Math.max(0f, g * (1f - factor));
        b = Math.max(0f, b * (1f - factor));
        return new Color(r, g, b);
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0)));
        return sb.length() > 2 ? sb.substring(0, 2) : sb.toString();
    }

    static class BubblePanel extends JPanel {
        private final boolean isOwn;
        private final Color backgroundColor;
        private final int radius = 14;
        private final int tailSize = 10;

        public BubblePanel(boolean isOwn, Color backgroundColor) {
            this.isOwn = isOwn;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                int rectX = isOwn ? 0 : tailSize;
                int rectW = Math.max(1, w - tailSize);
                int rectY = 0;
                int rectH = Math.max(1, h);

                g2.setColor(backgroundColor);
                g2.fillRoundRect(rectX, rectY, rectW - 1, rectH - 1, radius, radius);

                Path2D tail = new Path2D.Float();
                int ty = 18;
                if (isOwn) {
                    int tx = rectW - 1;
                    tail.moveTo(tx, ty);
                    tail.lineTo(tx + tailSize, ty + tailSize / 2);
                    tail.lineTo(tx, ty + tailSize);
                    tail.closePath();
                } else {
                    int tx = tailSize;
                    tail.moveTo(tx, ty);
                    tail.lineTo(tx - tailSize, ty + tailSize / 2);
                    tail.lineTo(tx, ty + tailSize);
                    tail.closePath();
                }
                g2.fill(tail);

                g2.setColor(new Color(0, 0, 0, 50));
                g2.drawRoundRect(rectX, rectY, rectW - 1, rectH - 1, radius, radius);
                g2.draw(tail);
            } finally {
                g2.dispose();
            }
        }
    }
}
