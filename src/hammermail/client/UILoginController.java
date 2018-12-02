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

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import hammermail.core.Globals;
import hammermail.core.Mail;
import hammermail.net.requests.*;
import hammermail.net.responses.*;
import static hammermail.net.responses.ResponseError.ErrorType.INTERNAL_ERROR;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class UILoginController implements Initializable {

    private Stage s;
    private Socket clientSocket;
        
    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML 
    private Text loginfailure;

    @FXML
    private void handleLogin(ActionEvent event) {
        //TODO: LOAD FILE IF EXISTS, CREATE IT AND FILL IT IF IT DOESN'T EXISTS
        try {
            if (username.getText().isEmpty() || password.getText().isEmpty())
                spawnLogin();
            else {
                ResponseBase response = composeAndSendGetMail();
                if (response instanceof ResponseError){
                    loginfailure.setFill(Color.rgb(255,0,0));
                    loginfailure.setText("Incorrect Authentication");

                } else if (response instanceof ResponseMails){
                    updateModelReqMail(response);
                    spawnHome();
                    Thread daemon = new Thread(new Task());
                    daemon.setDaemon(true);
//                    daemon.start();
                    
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
                String user = username.getText();
                RequestSignUp requestSignUp = new RequestSignUp();
                requestSignUp.SetAuthentication(user, password.getText());
                ResponseBase response = sendRequest(requestSignUp);

                
                //Username taken
                if (response instanceof ResponseError){
                    loginfailure.setFill(Color.rgb(255,0,0));
                    loginfailure.setText("Username taken");

                } else if (response instanceof ResponseSuccess){
                    response = composeAndSendGetMail();
                    Model.getModel().createJson(user);
                    if (response instanceof ResponseError)
                        spawnLogin();

                    else if (response instanceof ResponseMails){
                        updateModelReqMail(response);
                        spawnHome();
                    }
                    
                }
            }
        } catch (ClassNotFoundException classEx){
            // set the response to error internal_error
        } catch (UnknownHostException ex){
            // set the response to error internal_error
        } catch (IOException ioEx){
            // set the response to error internal_error
        } 
    }
     
    
    private void updateModelReqMail(ResponseBase response){
        Model.getModel().setCurrentUser(username.getText(), password.getText());
        List<Mail> received = ((ResponseMails) response).getReceivedMails();
        List<Mail> sent = ((ResponseMails) response).getSentMails();

        //insert the mails list to client local JSON file
        //received = received + JSON received
        //sent = sent + JSON sent 

        //Update Model
        //TEMPORARY ONLY FROM SERVE, we will add also the JSON mail
        Model.getModel().getListInbox().setAll(received);
        Model.getModel().getListSent().setAll(sent);
    }
    
    //maybe event argument will be eliminated..
    /**
    * Method to spawn a new Login view.
    * @author Gaetano
    * @param event:
    */
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
            newstage.show();
        } catch (IOException ex) {
            spawnLogin();
        }
    }
    
    private ResponseBase composeAndSendGetMail() throws ClassNotFoundException, IOException{
        //the RequestGetMail will be called with the Date argument from JSON    
        RequestGetMails requestGetMail = new RequestGetMails();
        requestGetMail.SetAuthentication(username.getText(), password.getText());
        return sendRequest(requestGetMail);
    }
    
    /**
    * Method to send request to the server
    * @author
    */
    private ResponseBase sendRequest(RequestBase request) throws ClassNotFoundException,  IOException{
            Socket socket = new Socket(Inet4Address.getLocalHost().getHostAddress(), Globals.HAMMERMAIL_SERVER_PORT_NUMBER);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(request);
            return (ResponseBase)in.readObject();
    }
    
    
    public void init(Stage stage){ 
        this.s = stage;
    }
        
    
    class Task implements Runnable{

        public Task(){
            if (clientSocket == null)
                clientSocket = new Socket();
        }

        @Override
        public void run() {
            try {
                RequestGetMails requestGetMail = new RequestGetMails();
                requestGetMail.SetAuthentication(username.getText(), password.getText());

                    while (true){
                        Thread.sleep(5000);
                        System.out.println("Daemon polling");
                        ResponseBase response = sendRequest(requestGetMail);
                        
                        List<Mail> received = ((ResponseMails) response).getReceivedMails();
                        List<Mail> sent = ((ResponseMails) response).getSentMails();
                        if (received.size() > 0)
                            Model.getModel().getListInbox().addAll(0, received);
                        if (sent.size() > 0)
                            Model.getModel().getListSent().addAll(0, sent);
                        
                        //Write on JSON
                            
                    }
            } catch (InterruptedException ex) {
                    //TODO
            } catch (ClassNotFoundException ex) {
                 Logger.getLogger(UILoginController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                 Logger.getLogger(UILoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    
}
