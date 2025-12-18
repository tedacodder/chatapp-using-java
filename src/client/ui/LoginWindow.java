package client.ui;

import client.ChatClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginWindow extends JFrame {

    public LoginWindow() {

        setTitle("Chat Login");
        setSize(440, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Root background panel
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(25, 25, 28));
        add(root);

        // Card panel
        JPanel card = new RoundedPanel(20);
        card.setPreferredSize(new Dimension(360, 220));
        card.setBackground(new Color(38, 38, 42));
        card.setBorder(new EmptyBorder(25, 30, 25, 30));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Title
        JLabel title = new JLabel("Chat App");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to continue");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(170, 170, 170));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        JTextField usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setBackground(new Color(55, 55, 60));
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        usernameField.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Button
        JButton connectBtn = new JButton("Continue");
        connectBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        connectBtn.setForeground(Color.WHITE);
        connectBtn.setBackground(new Color(0, 122, 204));
        connectBtn.setFocusPainted(false);
        connectBtn.setBorder(new EmptyBorder(10, 10, 10, 10));
        connectBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        connectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hover effect
        connectBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                connectBtn.setBackground(new Color(30, 144, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                connectBtn.setBackground(new Color(0, 122, 204));
            }
        });

        // Layout spacing
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(25));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(20));
        card.add(connectBtn);

        root.add(card);

        // Action
        connectBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Username cannot be empty",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            ChatClient client = new ChatClient("127.0.0.1", 5000);
            new ChatWindow(client, username);
            dispose();
        });

        setVisible(true);
    }

    // Custom rounded panel
    static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginWindow::new);
    }
}
