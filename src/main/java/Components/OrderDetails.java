package Components;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.DottedLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import com.formdev.flatlaf.FlatClientProperties;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import Class.Connexion;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OrderDetails extends JPanel {

    public OrderDetails(String ORDERID, List<Integer> ITEMIDList, List<Integer> quantityList, int USERID, Date dt, float orderprice, String orderstatus) {
        setLayout(new MigLayout("fill","[fill,grow][fill,grow][]","[]5[]5[]30[]push[]"));
        setBorder(new RoundBorder(15));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = sdf.format(dt);

        JLabel titleLabel = new JLabel(ORDERID);
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +18");
        add(titleLabel, "wrap");

        JLabel statusLabel = new JLabel(orderstatus);
        statusLabel.putClientProperty(FlatClientProperties.STYLE, "font:italic +13");
        add(statusLabel, "wrap");

        JLabel dateLabel = new JLabel(dateString);
        dateLabel.putClientProperty(FlatClientProperties.STYLE, "font:plain +13");
        add(dateLabel, "wrap");

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
                    add(itemLabel, "wrap, gapbottom 5");
                }
                rs.close();
            }
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JLabel totalLabel = new JLabel("TOTAL");
        totalLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +13");
        add(totalLabel, "split,aligny bottom");

        JLabel totalPriceLabel = new JLabel(Float.toString(orderprice)+" DH");
        totalPriceLabel.putClientProperty(FlatClientProperties.STYLE, "font:plain +13");
        add(totalPriceLabel,"aligny bottom");

        JButton generatePdfButton = new JButton("Generate PDF");
        generatePdfButton.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        generatePdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF(ORDERID, orderstatus, dateString, ITEMIDList, quantityList, orderprice);
            }
        });
        add(generatePdfButton,"alignx right");
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

                Color headerColor = new DeviceRgb(63, 169, 219);
                Color titleColor = new DeviceRgb(44, 62, 80);
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

                Paragraph totalPriceParagraph = new Paragraph("Total Price: " + orderprice +" DH")
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.RIGHT);
                document.add(totalPriceParagraph);

                BarcodeQRCode qrCode = new BarcodeQRCode(filePath);
                Image qrCodeImage = new Image(qrCode.createFormXObject(null, pdf))
                        .setWidth(UnitValue.createPercentValue(15))
                        .setHorizontalAlignment(HorizontalAlignment.RIGHT);
                document.add(qrCodeImage);

                document.close();
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error generating PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
