package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.example.QRCodeApp.*;

public class Main {
    public static void main(String[] args) {
        CréerBasedonee();

        //  l'interface utilisateur
        JFrame frame = new JFrame("Générateur de codes QR");
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        //
        UIManager.put("Button.background", new Color(72, 133, 255));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 16));
        UIManager.put("Panel.background", new Color(240, 240, 240));
        UIManager.put("TextField.font", new Font("Arial", Font.PLAIN, 14));

        // les champs de saisie
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JLabel textLabel = new JLabel("Saisissez du texte pour générer le code QR :");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(textLabel);
        panel.add(textField);

        // les boutons
        JButton generateButton = new JButton("Générer un code QR");
        JButton deleteButton = new JButton("Supprimer QR code");
        JButton archiveButton = new JButton("Voir les archives des codes QR");
        // le bouton d'édition du QR Code
        JButton editButton = new JButton("Modifier le code QR");
        editButton.setPreferredSize(new Dimension(150, 30));
        editButton.setFocusPainted(false);
        editButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        generateButton.setPreferredSize(new Dimension(15, 30));
        deleteButton.setPreferredSize(new Dimension(150, 30));
        archiveButton.setPreferredSize(new Dimension(150, 30));
        generateButton.setFocusPainted(false);
        deleteButton.setFocusPainted(false);
        archiveButton.setFocusPainted(false);
        generateButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        deleteButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        archiveButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        // Ajout de boutons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(generateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(archiveButton);
        buttonPanel.add(editButton);

        // Label pour afficher l'image du QR Code
        JLabel qrImageLabel = new JLabel();
        qrImageLabel.setPreferredSize(new Dimension(200, 200));
        qrImageLabel.setHorizontalAlignment(JLabel.CENTER);

        // Label pour afficher le texte
        JLabel qrTextLabel = new JLabel();
        qrTextLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        qrTextLabel.setHorizontalAlignment(JLabel.CENTER);

        // Générer un QR Code en appuyant sur le bouton "Générer un QR Code"
        generateButton.addActionListener(e -> {
            String text = textField.getText();
            if (!text.isEmpty()) {
                try {

                    byte[] qrImage = generateQRCode(text, 200, 200);
                    EnregistreCodeQR(text, qrImage);
                    saveQRCodeImageToFile(text);
                    ImageIcon icon = new ImageIcon(qrImage);
                    qrImageLabel.setIcon(icon);
                    qrTextLabel.setText("QR Text: " + text);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Veuillez saisir du texte !");
            }
        });

        // Modifier le code QR en appuyant sur le bouton "Modifier le code QR"
        editButton.addActionListener(e -> {
            String text = textField.getText();
            if (!text.isEmpty()) {
                try {
                    byte[] qrImage = generateQRCode(text, 200, 200);
                    ModifieQrCode(text, qrImage);
                    saveQRCodeImageToFile(text);
                    ImageIcon icon = new ImageIcon(qrImage);
                    qrImageLabel.setIcon(icon);
                    qrTextLabel.setText("QR Text: " + text);
                    JOptionPane.showMessageDialog(frame, "Code QR modifié avec succès!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Veuillez saisir le texte à modifier!");
            }
        });

        // Supprimez le code QR en appuyant sur le bouton "Supprimer le code QR"
        deleteButton.addActionListener(e -> {
            String text = textField.getText();
            if (!text.isEmpty()) {
                try {
                    // 1. Supprimer le code QR de la base de données
                    SupprimeCodeQR(text);

                    // 2. Supprimer l'image du QR code du fichier
                    deleteQRCodeImageFile(text);

                    // Effacer l'image et le texte dans l'interface utilisateur
                    qrImageLabel.setIcon(null);
                    qrTextLabel.setText("");

                    JOptionPane.showMessageDialog(frame, "Code QR supprimé avec succès !");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Erreur lors de la suppression du code QR !");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Veuillez saisir le texte à supprimer !");
            }
        });

        // Afficher tous les codes QR stockés
        archiveButton.addActionListener(e -> {
            String sql = "SELECT * FROM qr_codes";
            try (Connection conn = connect(); Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                StringBuilder archive = new StringBuilder("Archives des codes QR:\n");
                while (rs.next()) {
                    archive.append("ID: ").append(rs.getInt("id")).append(" Data: ").append(rs.getString("data")).append("\n");
                }
                JOptionPane.showMessageDialog(frame, archive.toString());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Ajout de composants à la fenêtre
        frame.add(panel);
        frame.add(buttonPanel);
        frame.add(qrImageLabel);
        frame.add(qrTextLabel);
        frame.setVisible(true);
    }
}


