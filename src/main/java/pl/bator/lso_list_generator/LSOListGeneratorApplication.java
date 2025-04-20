package pl.bator.lso_list_generator;

import pl.bator.lso_list_generator.view.ApplicationView;

import javax.swing.*;

public class LSOListGeneratorApplication {

    private static ApplicationView applicationView;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applicationView = new ApplicationView();
            applicationView.setVisible(true);
        });
    }
}