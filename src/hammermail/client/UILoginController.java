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

import hammermail.core.Mail;
import hammermail.core.Utils;
import static hammermail.core.Utils.isNullOrWhiteSpace;
import static hammermail.core.Utils.sendRequest;
import hammermail.net.requests.*;
import hammermail.net.responses.*;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class UILoginController implements Initializable {

    private Stage stage;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label loginfailure;

    private void retrieveGet(){
        System.out.println("In normal usage condition this doesn't print!!!");
        
        int count = 0;
        ResponseBase response;
        try {
            rollback();
            response = composeAndSendGetMail();

            while (response instanceof ResponseRetrieve && count < 5){
                rollback();
                response = composeAndSendGetMail();
                count++;
            }

            if (response instanceof ResponseError || response instanceof ResponseRetrieve) {
                rollback();
                showError("Unable to contact server, retry");
            } else if (response instanceof ResponseMails) {
                updateModelReqMail((ResponseMails) response);
                spawnHome();
            }
        
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UILoginController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UILoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            if (Utils.isAuthenticationWellFormed(username.getText(), password.getText())) {
                ResponseBase response = composeAndSendGetMail();
                showError("Incorrect authentication");
                if (response instanceof ResponseError) {
                    rollback();
                    showError("Incorrect Authentication");
                } else if (response instanceof ResponseMails) {
                    updateModelReqMail((ResponseMails) response);
                    spawnHome();
                } else if (response instanceof ResponseRetrieve)
                    retrieveGet();
                
            } else {
                showError("Insert a valid username and password!");
            }
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
                RequestSignUp requestSignUp = new RequestSignUp();
                requestSignUp.SetAuthentication(username.getText(), password.getText());

                ResponseBase response = sendRequest(requestSignUp);

                //Username taken
                if (response instanceof ResponseError) {
                    showError("Username taken!");

                } else if (response instanceof ResponseSuccess) {
                    response = composeAndSendGetMail();
                    if (response instanceof ResponseError) {
                        rollback();
                        showError("Unable to connect to server!");
                    } else if (response instanceof ResponseMails) {
                        updateModelReqMail((ResponseMails) response);
                        spawnHome();
                    } else if (response instanceof ResponseRetrieve) {
                        retrieveGet();
                    }
                } else if (response instanceof ResponseRetrieve) {
                    showError("Unable to contact server, retry");
                }
            } else {
                showError("Insert a valid username and password!");
            }
        } catch (ClassNotFoundException | IOException classEx) {
            showError("Unable to connect to server.");
            // set the response to error internal_error
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Platform.exit();
    }

    private ResponseBase composeAndSendGetMail() throws ClassNotFoundException, IOException {
        Model.getModel().setCurrentUser(username.getText(), password.getText());
        Timestamp lastUpdate = Model.getModel().takeLastRequestTime();        
        Model.getModel().setLastRequestTime(new Timestamp(System.currentTimeMillis()));
        
//        this.rollbackTime = new Timestamp(lastUpdate.getTime());

        System.out.println("Login: last update " + lastUpdate);
        RequestGetMails requestGetMail = new RequestGetMails(lastUpdate);

        requestGetMail.SetAuthentication(username.getText(), password.getText());
        return sendRequest(requestGetMail);
    }

    private void updateModelReqMail(ResponseMails response) {
        //Meglio confermare il login DOPO la response
        Model.getModel().setCurrentUser(username.getText(), password.getText());

        List<Mail> received = response.getReceivedMails();
        List<Mail> sent = response.getSentMails();

        Model.getModel().dispatchMail(received, sent);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
       // Model.getModel().setCurrentMail(new EmptyMail()); //This is the first Model call, it will exectute the Model constructor
    }
    
    private void rollback() {
        Model.getModel().deleteJson();
        Model.getModel().setCurrentUser(null, null);
    }

    private void spawnLogin() {
        //spawn a new login view
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("UIlogin.fxml"));
            Parent root = fxmlLoader.load();
            UILoginController loginController = fxmlLoader.getController();
            loginController.setStage(stage);
            stage.close();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("HammerMail - Login");
            stage.getIcons().add(new Image("hammermail/resources/hammermail.png"));
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(UILoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void spawnHome() {
        try {
            FXMLLoader uiLoader = new FXMLLoader();
            uiLoader.setLocation(getClass().getResource("UI.fxml"));
            Parent root;
            root = uiLoader.load();
            UIController uiController = uiLoader.getController();
            stage.close(); //close login view
            Stage newstage = new Stage();
            newstage.setTitle("HammerMail - Home");
            newstage.getIcons().add(new Image("hammermail/resources/hammermail.png"));
            newstage.setScene(new Scene(root));
            uiController.setStage(newstage);
            newstage.show();
        } catch (IOException ex) {
            Logger.getLogger(UILoginController.class.getName()).log(Level.SEVERE, null, ex);
            Platform.exit();
        }
    }

    private void showError(String message) {
        loginfailure.setText(message);
        loginfailure.setVisible(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        loginfailure.setVisible(false);
        username.textProperty().addListener(e -> loginfailure.setVisible(false));
        password.textProperty().addListener(e -> loginfailure.setVisible(false));
    }
    
//    public void startTestServer() {
//        //Autostart server and login
//        username.setText("a");
//        password.setText("a");
//        Thread t = new Thread(() -> {
//            Backend b;
//            b = new Backend();
//            b.startServer();
//        });
//        t.start();
//    }
}
