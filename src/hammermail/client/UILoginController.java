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
import static hammermail.core.Utils.sendRequest;
import hammermail.net.requests.*;
import hammermail.net.responses.*;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
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
    private Text loginfailure;

    @FXML
    private void handleLogin(ActionEvent event) {        
        try {
            if (username.getText().isEmpty() || password.getText().isEmpty())
                spawnLogin();
            else {
                ResponseBase response = composeAndSendGetMail();
                if (response instanceof ResponseError){
                    rollback();
                    loginfailure.setFill(Color.rgb(255,0,0));
                    loginfailure.setText("Incorrect Authentication");

                } else if (response instanceof ResponseMails){
                    updateModelReqMail((ResponseMails)response);
                    spawnHome();
                } 
            }
        } catch (ClassNotFoundException | IOException ex){
            System.out.println(ex.getMessage());
            // set the response to error internal_error
        } 
    }  
    

    @FXML
    private void handleSignup(ActionEvent event){
        
        try {
            if (username.getText().isEmpty() || password.getText().isEmpty())
                spawnLogin();
        
            else {
                //Compose request and send
                RequestSignUp requestSignUp = new RequestSignUp();
                requestSignUp.SetAuthentication(username.getText(), password.getText());
                
                ResponseBase response = sendRequest(requestSignUp);

                //Username taken
                if (response instanceof ResponseError){
                    loginfailure.setFill(Color.rgb(255,0,0));
                    loginfailure.setText("Username taken");

                } else if (response instanceof ResponseSuccess){
                    response = composeAndSendGetMail();
                    if (response instanceof ResponseError){
                        rollback();
                        spawnLogin();
                    
                    }else if (response instanceof ResponseMails){                        
                        updateModelReqMail((ResponseMails)response);
                        spawnHome();
                    }
                }
            }
        } catch (ClassNotFoundException | IOException classEx){
            // set the response to error internal_error
        }       
 
    }
         

    
    
    private ResponseBase composeAndSendGetMail() throws ClassNotFoundException, IOException{
        //Timestamp lastUpdate = viewLog();
        Model.getModel().setCurrentUser(username.getText(), password.getText());
        Timestamp lastUpdate = Model.getModel().takeLastRequestTime();
        Model.getModel().setLastRequestTime(new Timestamp(System.currentTimeMillis()));
       
        System.out.println("Login: last update " + lastUpdate);
        RequestGetMails requestGetMail = new RequestGetMails(lastUpdate);
        
        requestGetMail.SetAuthentication(username.getText(), password.getText());
        //clientServerLog(new Timestamp(System.currentTimeMillis()));
        return sendRequest(requestGetMail);
    }
    
    private void updateModelReqMail(ResponseMails response){
        //Meglio confermare il login DOPO la response
        Model.getModel().setCurrentUser(username.getText(), password.getText());
        
        List<Mail> received = response.getReceivedMails();
        List<Mail> sent = response.getSentMails();

        Model.getModel().dispatchMail(received, sent);
    }
    
    public void init(Stage stage){ 
        this.s = stage;
        Model.getModel().setCurrentMail(new EmptyMail()); //This is the first Model call, it will exectute the Model constructor
    }
     
    private void rollback(){
        Model.getModel().deleteJson();
        Model.getModel().setCurrentUser(null, null);
    }
    
    private void spawnLogin(){
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
         } catch (IOException e){
             //TODO
         }
    }
    
    private void spawnHome(){
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

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }      
}
