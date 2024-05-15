package Interface.InterfaceUser;
import Class.Connexion;
import Components.MenuButton;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MenuForm extends JPanel {
    private Connexion connexion = new Connexion();
    private List<MenuButton> menuButtonsList;
    private int currentPage = 0;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel titleLabel;

    public MenuForm(int userID) {
        FlatInterFont.install();
        setLayout(new MigLayout("alignx center, wrap 1, aligny top", "", "20[]10[]"));
        titleLabel = new JLabel("Choose the Category");
        titleLabel.putClientProperty(FlatClientProperties.STYLE,""+"font:bold +30");
        add(titleLabel);

        menuButtonsList = new ArrayList<>();
        initCategory(userID);

        prevButton = new JButton("Previous");
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previousPage();
            }
        });

        nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextPage();
            }
        });


        refreshMenuButtons();


        setVisible(true);
    }

    private void initCategory(int userID) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            conn = connexion.etablirConnexion();
            String selectQuery = "SELECT CATEGORYID , categoryname, image FROM CATEGORY";
            preparedStatement = conn.prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("CATEGORYID");
                String categoryName = resultSet.getString("categoryname");
                byte[] imageBytes = resultSet.getBytes("image");
                MenuButton button = new MenuButton(id,imageBytes, categoryName,userID);
                menuButtonsList.add(button);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        refreshMenuButtons();
    }

    private void refreshMenuButtons() {
        removeAll();

        add(titleLabel, "align center, wrap");

        int startIndex = currentPage * 3;
        int endIndex = Math.min(startIndex + 3, menuButtonsList.size());

        for (int i = startIndex; i < endIndex; i++) {
            add(menuButtonsList.get(i), "wrap");
        }

        prevButton = new JButton("Previous");
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previousPage();
            }
        });

        nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextPage();
            }
        });

        if (currentPage == 0) {
            add(nextButton, "align center, skip 1, split 2");
        } else {
            add(prevButton, "align center, split 2");
            add(nextButton, "align center");
        }

        if (currentPage == (menuButtonsList.size() - 1) / 3) {
            remove(nextButton);
        }



        revalidate();
        repaint();
    }


    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            refreshMenuButtons();
        }
        if (currentPage == 0){
            remove(prevButton);
        }
    }

    private void nextPage() {
        int maxPage = (menuButtonsList.size() - 1) / 3;
        if (currentPage < maxPage) {
            currentPage++;
            refreshMenuButtons();
        }
        if (currentPage == maxPage){
            remove(nextButton);
        }
    }
}
