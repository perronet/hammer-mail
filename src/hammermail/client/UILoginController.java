/*
 * Copyright (C) 2018 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hammermail.client;

import hammermail.core.EmptyMail;
import hammermail.core.Mail;
import hammermail.core.Utils;
import static hammermail.core.Utils.clientServerLog;
import static hammermail.core.Utils.sendRequest;
import static hammermail.core.Utils.viewLog;
import hammermail.net.requests.*;
import hammermail.net.responses.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class UILoginController implements Initializable {

    private Stage s;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label loginfailure;

    @FXML
    private void handleLogin(ActionEvent event) {
        //TODO: LOAD FILE IF EXISTS, CREATE IT AND FILL IT IF IT DOESN'T EXISTS
        try {
            if (Utils.isAuthenticationWellFormed(username.getText(), password.getText())) {
                ResponseBase response = composeAndSendGetMail();
                if (response instanceof ResponseError) {
                    showError("Incorrect authentication");

                } else if (response instanceof ResponseMails) {
                    updateModelReqMail((ResponseMails) response);
                    spawnHome();
                }
            }
            else
                showError("Insert a valid username and password!");
        } catch (ClassNotFoundException | IOException ex) {
            System.out.println(ex.getMessage());
            showError("Unable to connect to server.");
            // set the response to error internal_error
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            if (Utils.isAuthenticationWellFormed(username.getText(), password.getText())) {
                //Compose request and send
                String user = username.getText();
                RequestSignUp requestSignUp = new RequestSignUp();
                requestSignUp.SetAuthentication(user, password.getText());
                ResponseBase response = sendRequest(requestSignUp);

                //Username taken
                if (response instanceof ResponseError) {
                    showError("Username taken!");

                } else if (response instanceof ResponseSuccess) {
                    response = composeAndSendGetMail();
                    if (response instanceof ResponseError) {
                        spawnLogin();
                    } else if (response instanceof ResponseMails) {
                        updateModelReqMail((ResponseMails) response);
                        spawnHome();
                    }
                }
            }
            else
                showError("Insert a valid username and password!");
        } catch (ClassNotFoundException | IOException classEx) {
            showError("Unable to connect to server.");
            // set the response to error internal_error
        }

    }

    @FXML
    private void handleClose(ActionEvent event) {
        Platform.exit();
    }

    //CONVERT INTO DIFF
    private void updateModelReqMail(ResponseMails response) {
        Model.getModel().setCurrentUser(username.getText(), password.getText());
        Model.getModel().createJson(username.getText());

//      We will check if it is possible to substitute the log file with this function
//      Timestamp ts = Model.getModel().calculateLastMailStored();
//      System.out.println(ts);
        List<Mail> received = response.getReceivedMails();
        List<Mail> sent = response.getSentMails();

        Model.getModel().dispatchMail(received, sent);
    }

    /**
     * Method to spawn a new Login view.
     *
     * @author Gaetano
     * @param event:
     */
    private void spawnLogin() {
        //spawn a new login view
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("UIlogin.fxml"));
            Parent root = fxmlLoader.load();
            UILoginController loginController = fxmlLoader.getController();
            loginController.init(s);
            s.close();

            Scene scene = new Scene(root);
            s.setScene(scene);
            s.setTitle("HammerMail - Login");
            s.show();
        } catch (IOException e) {
            //TODO
        }
    }

    private void spawnHome() {
        try {
            FXMLLoader uiLoader = new FXMLLoader();
            uiLoader.setLocation(getClass().getResource("UI.fxml"));
            Parent root;
            root = uiLoader.load();
            UIController uiController = uiLoader.getController();
            s.close(); //close login view
            Stage newstage = new Stage();
            newstage.setTitle("HammerMail - Home");
            newstage.setScene(new Scene(root));
            uiController.init(newstage);
            newstage.show();
        } catch (IOException ex) {
            spawnLogin();
        }
    }

    private void showError(String message) {
        loginfailure.setVisible(true);
        loginfailure.setText(message);
    }

    private ResponseBase composeAndSendGetMail() throws ClassNotFoundException, IOException {
        Timestamp lastUpdate = viewLog();
        RequestGetMails requestGetMail = new RequestGetMails(lastUpdate);
        requestGetMail.SetAuthentication(username.getText(), password.getText());
        clientServerLog(new Timestamp(System.currentTimeMillis()));
        return sendRequest(requestGetMail);
    }

    public void init(Stage stage) {
        this.s = stage;
        Model.getModel().setCurrentMail(new EmptyMail()); //This is the first Model call, it will exectute the Model constructor
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        File userFile = new File("log.txt");
        if (!(userFile.exists())) {
            try {
                clientServerLog(new Timestamp(0));
                System.out.println("°°°°°°°°°INIT-LOGIN: the current log is " + viewLog());

            } catch (IOException ex) {
                //Logger.getLogger(UILoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("°°°°°°°°°INIT-LOGIN: the current log is " + viewLog());
        }

        loginfailure.setVisible(false);

        username.textProperty().addListener(e -> loginfailure.setVisible(false));
        password.textProperty().addListener(e -> loginfailure.setVisible(false));
    }
}
