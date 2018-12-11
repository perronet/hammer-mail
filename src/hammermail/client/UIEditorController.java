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

import static hammermail.core.Utils.*;
import hammermail.core.Mail;
import hammermail.core.User;
import static hammermail.core.Utils.isNullOrWhiteSpace;
import hammermail.net.requests.RequestSendMail;
import hammermail.net.responses.ResponseBase;
import hammermail.net.responses.ResponseError;
import hammermail.net.responses.ResponseError.ErrorType;
import hammermail.net.responses.ResponseMailSent;
import hammermail.net.responses.ResponseRetrieve;
import static hammermail.core.Utils.spawnError;
import static hammermail.net.responses.ResponseError.ErrorType.SENDING_INVALID_MAIL;
import static hammermail.net.responses.ResponseError.ErrorType.SENDING_TO_UNEXISTING_USER;
import java.sql.Timestamp;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author marco
 */
public class UIEditorController {

    private Stage stage;

    @FXML
    private TextField receiversmail, mailsubject;
    
    @FXML
    private TextArea bodyfield;

    @FXML
    private void handleSend(ActionEvent event) {
        //TODO read receiver to each comma and verify it is an existent person
        String receiver = receiversmail.getText();
        if (isNullOrWhiteSpace(receiver)) {
            spawnError("Invalid receiver");
        } else {
            Mail mail = composeMail(receiver);
            RequestSendMail request = new RequestSendMail(mail);
            User current = Model.getModel().getCurrentUser();
            request.SetAuthentication(current.getUsername(), current.getPassword());

            try {
                ResponseBase response = sendRequest(request);
                if (response instanceof ResponseError) {
                    ErrorType err = ((ResponseError) response).getErrorType();
                    switch(err){
                        case SENDING_TO_UNEXISTING_USER:
                            //Popup or visualize this message
                        
                        case SENDING_INVALID_MAIL:
                            //as before
                        
                        case INTERNAL_ERROR:
                            //as before

                    }
                    spawnError("Error response received: " + err.toString());
                } 
                else if (response instanceof ResponseMailSent) {
//                    mail.setId(((ResponseMailSent) response).getMailID());
//                    Model.getModel().addMail(mail);
                    if ( ((ResponseMailSent)response).getRefusedName().length() > 0){
                        //SHOW POPUP, mail was sent to: response.getSentTo()
                        // response.getRefusedNames doesn't exists
                    }

                } 
                else if (response instanceof ResponseRetrieve) {
                    response = sendRequest(request);
                    int count = 0;
                    while (response instanceof ResponseRetrieve && count < 5){
                        response = sendRequest(request);
                        count++;
                    }

                    if (response instanceof ResponseError || response instanceof ResponseRetrieve) {
                        spawnError("Unable to contact server, retry to send");
                    } 
                }

            } catch (ClassNotFoundException | IOException classEx) {
                spawnError("Internal error");
                // set the response to error internal_error
            } finally {
                stage.close();
                //Only for testing
//                Database d = new Database(false);
//                d.dbStatus();
            }
        }
    }

    @FXML
    public void handleSave(Event event) {
        String receiver = receiversmail.getText();
        String mailsub = mailsubject.getText();
        String body = bodyfield.getText();
        if (isNullOrWhiteSpace(receiver) && isNullOrWhiteSpace(mailsub)
                && isNullOrWhiteSpace(body) && (event instanceof WindowEvent)) {
            stage.close();
        } else {
            Model.getModel().saveDraft(receiver, mailsub, body);
            System.out.println("Draft saved");
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    //If the field is not empty, it cannot be modified
    public void setTextAreas(String sndrcv, String tit, String text, boolean modifiable) {
        receiversmail.setText(sndrcv);
        mailsubject.setText(tit);
        bodyfield.setText(text);
        if (!(modifiable)) {
            if (!(sndrcv.equals(""))) {
                receiversmail.setEditable(false);
            }
            if (!(tit.equals(""))) {
                mailsubject.setEditable(false);
            }
            if (!(text.equals(""))) {
                bodyfield.setEditable(false);
            }
        }
    }

    private Mail composeMail(String receiver) {
        String sender = Model.getModel().getCurrentUser().getUsername();
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        return new Mail(-1, sender, receiver, mailsubject.getText(), bodyfield.getText(), ts);
    }

}
