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

    private Model m;
   
    private Stage s;
    
    @FXML
    private TextArea receiversmail, mailsubject, bodyfield;
    
    @FXML 
    private void handleSend(ActionEvent event){
        //TODO read receiver to each comma and verify it is an existent person
        String receiver = receiversmail.getText();
        if(receiver.equals("")){
            handleError();
        }else{
            m.addMail(receiver, mailsubject.getText(), bodyfield.getText());
            s.close();
        }
    }
    
    @FXML 
    public void handleSave(Event event){
        String receiver = receiversmail.getText();
        if(receiver.equals("")){
            if(!(event instanceof WindowEvent)){ //"If the window didn't get closed"
                handleError();                   //TODO just save the draft always, even if the receiver is empty
            }
        }else{
            m.saveDraft(receiver, mailsubject.getText(), bodyfield.getText());
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
    public void init(Model model, Stage stage){ //to add parameter "current user" to set sender
        if(this.m != null){
            throw new IllegalStateException("Only one initialization per model.");
        }
        this.m = model; 
        this.s = stage;
    }
       
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    } 
    
}
