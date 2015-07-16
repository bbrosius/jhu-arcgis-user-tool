package org.jhu.gis.usertool;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.jhu.gis.usertool.model.LoginCredential;
import org.jhu.gis.usertool.presenter.LoginPresenter;
import org.jhu.gis.usertool.presenter.UserPresenter;

import java.io.IOException;

public class Controller {

    private BorderPane content;
    private UserPresenter userPresenter;
    private LoginPresenter loginPresenter;

    public Controller() throws IOException {
        content = new BorderPane();
        userPresenter = new UserPresenter();
        loginPresenter = new LoginPresenter();
    }

    public void startApp() throws IOException {
        content.setCenter(loginPresenter.display(this));
    }

    public void handleLogin(LoginCredential credential) throws IOException {
        content.setCenter(userPresenter.display(credential));
    }

    public Parent asParent() {
        return content;
    }

}
