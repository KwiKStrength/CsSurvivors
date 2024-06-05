module CsSurvivors {
    requires com.formdev.flatlaf;
    requires com.formdev.flatlaf.fonts.inter;
    requires java.datatransfer;
    requires java.desktop;
    requires uk.co.caprica.vlcj;
    requires java.management;
    requires com.miglayout.swing;
    requires java.sql;
    requires javafx.graphics;
    requires java.logging;
    requires com.formdev.flatlaf.extras;
    requires java.mail;
    requires MultiOS.HWID;
    requires io;
    requires kernel;
    requires layout;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.dlsc.formsfx;
    requires MaterialFX;
    requires java.sql.rowset;
    requires java.prefs;
//    requires barcodes;
    requires itextpdf;
    requires barcodes;

    exports Interface.InterfaceAdmin.interfaces;
    opens Interface.InterfaceAdmin.interfaces to javafx.fxml;

    opens Interface.InterfaceAdmin.controllers to javafx.fxml;
    exports Interface.InterfaceAdmin.controllers;


    exports Interface.InterfaceAdmin.interfaces.panes;

    opens Interface.InterfaceAdmin.controllers.panes to javafx.fxml, javafx.base;
    exports Interface.InterfaceAdmin.controllers.panes;
    opens Interface.InterfaceAdmin.interfaces.panes to javafx.base, javafx.fxml;
    exports Class;
    opens Class to javafx.fxml;
}