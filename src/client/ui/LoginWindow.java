package client.ui;

import client.ChatClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginWindow extends JFrame {

    public LoginWindow() {

        setTitle("Chat Login");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center on screen
        setResizable(false);

        // Main background
        JPanel bg = new JPanel();
        bg.setBackground(new Color(40, 40, 40));
        bg.setLayout(new GridBagLayout());
        add(bg);

        // Card panel (centered box)
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(340, 200));
        card.setBackground(new Color(55, 55, 55));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                new EmptyBorder(20, 30, 20, 30)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Title label
        JLabel title = new JLabel("Welcome");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your username");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(180, 180, 180));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setBackground(new Color(70, 70, 70));
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        usernameField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Connect button (modern style)
        JButton connectBtn = new JButton("Continue");
        connectBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        connectBtn.setForeground(Color.WHITE);
        connectBtn.setBackground(new Color(10, 132, 255));
        connectBtn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        connectBtn.setFocusPainted(false);
        connectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        connectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add spacing & components to card
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(18));
        card.add(connectBtn);

        // Add card to center
        bg.add(card);

        // Click action
        connectBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a username.",
                        "Error",
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

    public static void main(String[] args) {
        new LoginWindow();
    }
}
