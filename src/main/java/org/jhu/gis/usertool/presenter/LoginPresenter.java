package org.jhu.gis.usertool.presenter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.jhu.gis.usertool.Controller;
import org.jhu.gis.usertool.model.LoginCredential;


import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class LoginPresenter {

    public TextField usernameField;
    public PasswordField passwordField;
    public Label errorMessage;

    private Node loginView = null;
    private Controller controller;

    public LoginPresenter() {
    }

    public Node display(Controller controller) throws IOException {

        this.controller = controller;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login_view.fxml"), ResourceBundle.getBundle("bundles/labels", Locale.getDefault()));
        loader.setController(this);

        if (loginView == null) {
            loginView = loader.load();
        }

        return loginView;
    }

    public void loginAction(ActionEvent actionEvent) throws IOException {
        errorMessage.setVisible(false);
        LoginCredential credential = new LoginCredential(usernameField.getText(), passwordField.getText());
        controller.handleLogin(credential);
    }

    public void handleError(String message) {
        errorMessage.setVisible(true);
        errorMessage.setText(message);
    }
}
