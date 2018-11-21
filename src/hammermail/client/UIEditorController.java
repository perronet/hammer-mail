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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author marco
 */
public class UIEditorController implements Initializable {

    private Model m;
   
    @FXML
    private TextArea receiversmail;
    @FXML
    private TextArea mailsubject;
    @FXML
    private TextArea bodyfield;
    
    private String receiver, subject, text;
    
    
    @FXML 
    private void handleSend(ActionEvent event){
        receiver = receiversmail.getText();
        subject = mailsubject.getText();
        text = bodyfield.getText();
        m.addMail(receiver, subject, text);  
    }
    
    @FXML 
    private void handleSave(ActionEvent event){
        receiver = receiversmail.getText();
        subject = mailsubject.getText();
        text = bodyfield.getText();
        m.saveDraft(receiver, subject, text);
    }
    /**
     * Initializes the controller class.
     */
    
    public void init(Model model){ //to add parameter "current user" to set sender
        if(this.m != null){
                throw new IllegalStateException("Only one initialization per model.");
            }
        this.m = model; //Binding the model
    }
       
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    } 
    
}
