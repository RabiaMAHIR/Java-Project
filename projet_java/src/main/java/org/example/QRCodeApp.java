package org.example;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeApp {
    public static Connection connect() {
        String url = "jdbc:sqlite:qr_codes.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    // Créer la base de données
    public static void CréerBasedonee() {
        String sql = "CREATE TABLE IF NOT EXISTS qr_codes ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "data TEXT NOT NULL,"
                + "qr_image BLOB NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Enregistre le code QR dans la base de données
    public static void EnregistreCodeQR(String data, byte[] qrImage) {
        String sql = "INSERT INTO qr_codes(data, qr_image) VALUES(?,?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data);
            pstmt.setBytes(2, qrImage);
            pstmt.executeUpdate();
            System.out.println("[1] Code QR enregistré avec succès dans la base de données!");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement du code QR dans la base de données: " + e.getMessage());
        }
    }

    // Générer un code QR
    public static byte[] generateQRCode(String text, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hintMap);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < byteMatrix.getWidth(); i++) {
            for (int j = 0; j < byteMatrix.getHeight(); j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
    // Mise à jour du QR code dans la base de données
    public static void ModifieQrCode(String data, byte[] qrImage) {
        String sql = "UPDATE qr_codes SET qr_image = ?, created_at = CURRENT_TIMESTAMP WHERE data = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBytes(1, qrImage);
            pstmt.setString(2, data);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[1] Le code QR dans la base de données a été mis à jour avec succès !");
            } else {
                System.out.println("[1] Aucun code QR de mise à jour trouvé.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du code QR dans la base de données: " + e.getMessage());
        }
    }


    // Supprime le code QR de la base de données en utilisant du texte
    // Supprime le code QR de la base de données et du fichier
    public static void SupprimeCodeQR(String data) {
        String sql = "DELETE FROM qr_codes WHERE data = ?";
        String directoryPath = "projet_java\\qr_codes";
        File directory = new File(directoryPath);

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[1] Le code QR a été supprimé avec succès de la base de données !");

                // Suppression de l'image du fichier
                deleteQRCodeImageFile(data);
            } else {
                System.out.println("[1] Le code QR n'a pas été trouvé dans la base de données.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du code QR: " + e.getMessage());
        }
    }

    // Fonction pour supprimer l'image du code QR dans le répertoire

    public static void deleteQRCodeImageFile(String text) {
        // Définir le chemin du fichier en fonction du texte du code QR
        String fileName = "QRCode_" + text.replace(" ", "_") + ".png";
        File directory = new File(System.getProperty("user.dir") + File.separator + "qr_codes");
        File file = new File(directory + File.separator + fileName);

        if (file.exists()) {
            if (file.delete()) {
                System.out.println("[2] L'image du code QR a été supprimée du fichier avec succès !");
            } else {
                System.err.println("Erreur lors de la suppression de l'image du fichier.");
            }
         }
    }

    // Enregistre l'image du QR Code dans un fichier
    public static void saveQRCodeImageToFile(String text) {
        try {
            byte[] qrImage = generateQRCode(text, 200, 200);

            // Spécifie où enregistrer l'image
            String fileName = "QRCode_" + text.replace(" ", "_") + ".png";
            File directory = new File(System.getProperty("user.dir") + File.separator + "qr_codes");

            if (!directory.exists()) {
                directory.mkdir();
            }

            File file = new File(directory + File.separator + fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(qrImage);
            }

            System.out.println("[1] Le code QR a été enregistré dans: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}