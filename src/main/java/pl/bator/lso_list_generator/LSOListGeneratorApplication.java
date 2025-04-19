package pl.bator.lso_list_generator;

import pl.bator.lso_list_generator.controller.ApplicationController;

import javax.swing.*;

public class LSOListGeneratorApplication {

    private static ApplicationController applicationController;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applicationController = new ApplicationController();
            applicationController.setVisible(true);
        });
    }
}