package org.jhu.gis.usertool.presenter;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import org.jhu.gis.usertool.ArcGISOnlineService;
import org.jhu.gis.usertool.model.LoginCredential;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

public class UserPresenter {

    public ProgressIndicator expiringUserIndicator;
    public Button expiringUserButton;
    public Button newUserButton;
    public ProgressIndicator newUserIndicator;
    public TextArea newUsersResult;
    public TextArea expiringUsersResult;

    private ArcGISOnlineService service;
    private Node userView = null;

    public UserPresenter() {
    }

    public Node display(ArcGISOnlineService service) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_view.fxml"), ResourceBundle.getBundle("bundles/labels", Locale.getDefault()));
        loader.setController(this);
        this.service = service;
        if (userView == null) {
            userView = loader.load();
        }

        return userView;
    }

    public void getNewUsers(ActionEvent actionEvent) {
        newUsersResult.setText("");

        newUserIndicator.setVisible(true);
        expiringUserButton.setDisable(true);
        newUserButton.setDisable(true);

        Task userLoader = new Task<ArcGISOnlineService.Users>() {
            {
                setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        ArcGISOnlineService.Users newUsers = getValue();

                        String newUserText = "";
                        if (newUsers.getUsers().isEmpty()) {
                            newUserText = "No new users in the last week.";
                        }
                        else {
                            for (ArcGISOnlineService.User user : newUsers.getUsers()) {
                                GregorianCalendar createdDate = new GregorianCalendar();
                                createdDate.setTimeInMillis(user.createdDate);

                                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                                newUserText += "Fullname: " + user.fullName + " Username: " + user.userName + " Created: " +
                                    df.format(createdDate.getTime()) + "\n";
                            }
                        }
                        newUserIndicator.setVisible(false); // stop displaying the loading indicator
                        expiringUserButton.setDisable(false);
                        newUserButton.setDisable(false);
                        newUsersResult.setText(newUserText);
                    }
                });

                setOnFailed(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        newUsersResult.setText("Error retrieving users from ArcGIS Online: " + getException().getMessage());
                    }
                });
            }

            @Override
            protected ArcGISOnlineService.Users call() throws Exception {
                return service.getNewUsersFromPastWeek();
            }
        };

        Thread loadingThread = new Thread(userLoader, "user-loader");
        loadingThread.setDaemon(true);
        loadingThread.start();
    }

    public void getExpiringUsers(ActionEvent actionEvent) {
        expiringUsersResult.setText("");

        expiringUserIndicator.setVisible(true);
        expiringUserButton.setDisable(true);
        newUserButton.setDisable(true);

        Task userLoader = new Task<ArcGISOnlineService.Users>() {
            {
                setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        ArcGISOnlineService.Users expiringUsers = getValue();

                        String expiringUserText = "";

                        if (expiringUsers.getUsers().isEmpty()) {
                            expiringUserText = "No inactive users";
                        } else {
                            for (ArcGISOnlineService.User user : expiringUsers.getUsers()) {
                                String loginString = "Last Login: ";
                                if (user.lastLogin < 43200) {
                                    loginString += "Never";
                                } else {
                                    GregorianCalendar lastLogin = new GregorianCalendar();
                                    lastLogin.setTimeInMillis(user.lastLogin);

                                    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                                    loginString += df.format(lastLogin.getTime());
                                }

                                expiringUserText += "Fullname: " + user.fullName + " Username: "  + user.userName + " " + loginString + "\n";
                            }
                        }
                        expiringUserIndicator.setVisible(false); // stop displaying the loading indicator
                        expiringUserButton.setDisable(false);
                        newUserButton.setDisable(false);
                        expiringUsersResult.setText(expiringUserText);

                    }
                });

                setOnFailed(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        expiringUsersResult.setText(
                                                    "Error retrieving users from ArcGIS Online: " + getException().getMessage());
                    }
                });

            }

            @Override
            protected ArcGISOnlineService.Users call() throws Exception {
                return service.getUsersInactiveForOneYear();
            }
        };

        Thread loadingThread = new Thread(userLoader, "user-loader");
        loadingThread.setDaemon(true);
        loadingThread.start();
    }
}
