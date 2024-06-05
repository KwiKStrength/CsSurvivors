package Components;

import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.DottedLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

import com.itextpdf.layout.element.LineSeparator;

import Class.Connexion;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.formdev.flatlaf.FlatClientProperties;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import net.miginfocom.swing.MigLayout;

public class OrderDetails extends JPanel {

    public OrderDetails(String ORDERID, List<Integer> ITEMIDList, List<Integer> quantityList, int USERID, Date dt, float orderprice, String orderstatus) {
        setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 15", "[left]push[right]", "[]10[]10[]20[][]20[]"));

        setBorder(new RoundBorder(15));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = sdf.format(dt);

        JLabel titleLabel = new JLabel(ORDERID);
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +18");
        contentPanel.add(titleLabel, "span, wrap");

        JLabel statusLabel = new JLabel(orderstatus);
        statusLabel.putClientProperty(FlatClientProperties.STYLE, "font:italic +13");
        contentPanel.add(statusLabel, "span, wrap");

        JLabel dateLabel = new JLabel(dateString);
        dateLabel.putClientProperty(FlatClientProperties.STYLE, "font:plain +13");
        contentPanel.add(dateLabel, "span, wrap");

        try {
            Connection conn = Connexion.etablirConnexion();
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM ITEM WHERE ITEMID = ?");
            for (int i = 0; i < ITEMIDList.size(); i++) {
                int ITEMID = ITEMIDList.get(i);
                int quantity = quantityList.get(i);
                stmt.setInt(1, ITEMID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String itemName = rs.getString("name");
                    String itemDescription = quantity + "x " + itemName;
                    JLabel itemLabel = new JLabel(itemDescription);
                    itemLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +12");
                    contentPanel.add(itemLabel, "span, wrap");
                }
                rs.close();
            }
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setPreferredSize(new Dimension(800, 30));
        progressBar.setFont(new Font("Arial", Font.BOLD, 14));
        updateProgressBar(progressBar, orderstatus);
        contentPanel.add(progressBar, "align, wrap, gapy 80");

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JLabel totalLabel = new JLabel("TOTAL PRICE : ");
        totalLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +13");
        bottomPanel.add(totalLabel, BorderLayout.WEST);

        JLabel totalPriceLabel = new JLabel(Float.toString(orderprice) + " DH");
        totalPriceLabel.putClientProperty(FlatClientProperties.STYLE, "font:plain +13");
        bottomPanel.add(totalPriceLabel, BorderLayout.CENTER);

        JButton generatePdfButton = new JButton("Generate PDF");
        generatePdfButton.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        generatePdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF(ORDERID, orderstatus, dateString, ITEMIDList, quantityList, orderprice);
            }
        });
        bottomPanel.add(generatePdfButton, BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void generatePDF(String ORDERID, String orderstatus, String dateString, List<Integer> ITEMIDList, List<Integer> quantityList, float orderprice) {
        JFileChooser fileChooser = new JFileChooser() {
            @Override
            public void approveSelection() {
                File selectedFile = getSelectedFile();
                if (!selectedFile.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + ".pdf");
                    setSelectedFile(selectedFile);
                }
                super.approveSelection();
            }
        };
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            try {
                PdfWriter writer = new PdfWriter(filePath);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf, PageSize.A4);
                pdf.getCatalog().setLang(new PdfString("en-US"));
                pdf.getDocumentInfo().setTitle(ORDERID +" Details");
                pdf.getDocumentInfo().setAuthor("CAMPUS GRILL");

                com.itextpdf.kernel.colors.Color headerColor = new DeviceRgb(63, 169, 219);
                com.itextpdf.kernel.colors.Color titleColor = new DeviceRgb(44, 62, 80);
                Color separatorColor = new DeviceRgb(189, 195, 199);

                document.setMargins(36, 36, 36, 36);

                Paragraph header = new Paragraph("Order Details")
                        .setFontSize(20)
                        .setFontColor(headerColor)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20);
                document.add(header);

                Paragraph orderIdParagraph = new Paragraph("ORDERID: " + ORDERID)
                        .setFontSize(18)
                        .setFontColor(titleColor)
                        .setBold();
                document.add(orderIdParagraph);

                Paragraph statusParagraph = new Paragraph("Order Status: " + orderstatus)
                        .setFontSize(14)
                        .setFontColor(titleColor);
                document.add(statusParagraph);

                Paragraph dateParagraph = new Paragraph("Date: " + dateString)
                        .setFontSize(14)
                        .setItalic();
                document.add(dateParagraph);

                document.add(new LineSeparator(new DottedLine()).setStrokeColor(separatorColor).setMarginBottom(10));

                Connection conn = Connexion.etablirConnexion();
                PreparedStatement stmt = conn.prepareStatement("SELECT name FROM ITEM WHERE ITEMID = ?");
                for (int i = 0; i < ITEMIDList.size(); i++) {
                    int ITEMID = ITEMIDList.get(i);
                    int quantity = quantityList.get(i);
                    stmt.setInt(1, ITEMID);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String itemName = rs.getString("name");
                        String itemDescription = quantity + "x " + itemName;
                        Paragraph itemParagraph = new Paragraph("Item: " + itemDescription)
                                .setFontSize(12)
                                .setMarginBottom(5);
                        document.add(itemParagraph);
                    }
                    rs.close();
                }
                stmt.close();
                conn.close();

                document.add(new LineSeparator(new DottedLine()).setStrokeColor(separatorColor).setMarginTop(10).setMarginBottom(10));

                BarcodeQRCode qrCode = new BarcodeQRCode(filePath);
                com.itextpdf.layout.element.Image qrCodeImage = new Image(qrCode.createFormXObject(null, pdf))
                        .setWidth(UnitValue.createPercentValue(30))
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(qrCodeImage);

                document.add(new LineSeparator(new DottedLine()).setStrokeColor(separatorColor).setMarginTop(10).setMarginBottom(10));

                Paragraph totalPriceParagraph = new Paragraph("Total Price: " + orderprice + " DH")
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.LEFT);
                document.add(totalPriceParagraph);

                document.close();
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error generating PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateProgressBar(JProgressBar progressBar, String orderstatus) {
        int progress = 0;
        switch (orderstatus) {
            case "Pending":
                progress = 25;
                break;
            case "Being Prepared":
                progress = 50;
                break;
            case "Ready":
                progress = 75;
                break;
            case "Delivered":
                progress = 100;
                break;
        }
        progressBar.setValue(progress);
        progressBar.setString(orderstatus);
    }

    public class RoundBorder extends AbstractBorder {
        private int radius;

        public RoundBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius, this.radius, this.radius, this.radius);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = this.radius;
            return insets;
        }
    }
}
