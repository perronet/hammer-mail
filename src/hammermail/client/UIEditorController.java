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

import hammermail.core.Globals;
import hammermail.core.Mail;
import hammermail.core.User;
import static hammermail.core.Utils.isNullOrWhiteSpace;
import hammermail.net.requests.RequestBase;
import hammermail.net.requests.RequestSendMail;
import hammermail.net.responses.ResponseBase;
import hammermail.net.responses.ResponseError;
import hammermail.net.responses.ResponseError.ErrorType;
import hammermail.net.responses.ResponseMailSent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author marco
 */
public class UIEditorController implements Initializable {

//    private Model m;
   
    private Stage s;
    
    @FXML
    private TextArea receiversmail, mailsubject, bodyfield;
    
   @FXML 
    private void handleSend(ActionEvent event){
        //TODO read receiver to each comma and verify it is an existent person
        String receiver = receiversmail.getText();
        if(isNullOrWhiteSpace(receiver)){
            handleError();
        }else{
            String sender = Model.getModel().getCurrentUser().getUsername();
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            Mail mail = new Mail(-1, sender, receiver, mailsubject.getText(), bodyfield.getText(), ts);
            RequestSendMail request = new RequestSendMail(mail);
            User current = Model.getModel().getCurrentUser();
            request.SetAuthentication(current.getUsername(), current.getPassword());
            
            try {
                ResponseBase response = sendRequest(request);
                if (response instanceof ResponseError){
//                  TODO inspect the type of error
//                  ErrorType err = ((ResponseError)response).getErrorType();
//                  System.out.println(err);
                    handleError();
                } else if (response instanceof ResponseMailSent){
                    int mailID = ((ResponseMailSent) response).getMailID();
                    mail.setId(mailID);
                    Model.getModel().getListSent().add(mail);

                    //WRITE ID-MAIL ON JSON
                }
                

            } catch (UnknownHostException ex){
                System.out.println("catch1");
                handleError();
            // set the response to error internal_error
            } catch (ClassNotFoundException | IOException classEx){
                System.out.println("catch2");
                handleError();
            // set the response to error internal_error
            } finally {
                s.close();
            }
        }
    }
    
    @FXML 
    public void handleSave(Event event){
        String receiver = receiversmail.getText();
        String mailsub = mailsubject.getText();
        String body = bodyfield.getText();
        if(receiver.equals("") && mailsub.equals("") && body.equals("") && (event instanceof WindowEvent)){
            s.close();
        }else{            
            Model.getModel().saveDraft(receiver, mailsub, body);
            System.out.println("Draft saved");
            s.close();
        }
    }
    
    private void handleError(){
        try{
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("UIEditorError.fxml"));
                Parent root = fxmlLoader.load();
                Scene scene = new Scene(root, 200, 200);
                Stage stage = new Stage();
                stage.setTitle("Error!");
                stage.setScene(scene);
                stage.show();
            }catch(IOException e){
                System.out.println (e.toString());
            }
    }
    
    /**
     * Initializes the controller class.
     */
    
    //"Constructor"
    public void init(/*Model model, */Stage stage){ //to add parameter "current user" to set sender
//        if(this.m != null){
//            throw new IllegalStateException("Only one initialization per model.");
//        }
//        this.m = model; 
        this.s = stage;
    }
    
    //If the field is not empty, it cannot be modified
    public void setTextAreas(String sndrcv, String tit, String text, boolean modifiable){
        receiversmail.setText(sndrcv);
        mailsubject.setText(tit);
        bodyfield.setText(text);
        if(!(modifiable)){
            if(!(sndrcv.equals(""))){ receiversmail.setEditable(false);}
            if(!(tit.equals(""))){ mailsubject.setEditable(false);}
            if(!(text.equals(""))){ bodyfield.setEditable(false);}
        }
        
        
    }
       
    
    //this method should be place in a separate file
    private ResponseBase sendRequest(RequestBase request) throws ClassNotFoundException, UnknownHostException,  IOException{
            Socket socket = new Socket(Inet4Address.getLocalHost().getHostAddress(), Globals.HAMMERMAIL_SERVER_PORT_NUMBER);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(request);
            return (ResponseBase)in.readObject();
    }
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    } 
    
}
