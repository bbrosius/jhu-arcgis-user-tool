package org.jhu.gis.usertool;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
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
        content.setBackground(new Background(new BackgroundFill(Color.BEIGE, CornerRadii.EMPTY, Insets.EMPTY)));

        HBox banner = new HBox();
        banner.setAlignment(Pos.CENTER);
        banner.getChildren().add(new ImageView("banner.png"));

        content.setTop(banner);
        userPresenter = new UserPresenter();
        loginPresenter = new LoginPresenter();
    }

    public void startApp() throws IOException {
        content.setCenter(loginPresenter.display(this));
    }

    public void handleLogin(LoginCredential credential) throws IOException {
        try {
            ArcGISOnlineService service = new ArcGISOnlineService(credential);
            content.setCenter(userPresenter.display(service));
        } catch (Exception e) {
            loginPresenter.handleError(e.getMessage());
        }
    }

    public Parent asParent() {
        return content;
    }

}
