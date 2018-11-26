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
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import hammermail.core.Mail;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.stage.WindowEvent;

public class UIController implements Initializable {

    private Model m;
    
    //TODO maybe add a Stage attribute
    
    @FXML
    private Label user;
    
    @FXML
    private ListView<Mail> listmail;
    
    @FXML
    private ListView<Mail> listdraft; //To set and to put in a tabpane's tab
    
    @FXML
    private TextArea mailcontent;
    
    @FXML
    private Tab tab1, tab2; //use those to handle delete...
    
    private String nametab;
    
    @FXML
    private void handleCreate(ActionEvent event){
    
        try{    
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("UIeditor.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 350, 450);
            Stage stage = new Stage();
            UIEditorController editorController = fxmlLoader.getController();
            editorController.init(m, stage);
            stage.setTitle("Write a mail...");
            stage.setScene(scene);
            stage.show();
            
            // Handler to save drafts when closing the window
            stage.setOnCloseRequest(e -> {
                editorController.handleSave(e);
                System.out.println("Stage is closing");
            });
            
        }catch(IOException e){
            System.out.println (e.toString());
        }
        
    }  

    @FXML //TODO: find a way to remove from different tabs
    private void handleDelete(ActionEvent event){
       // nametab = tab1.selected();
        m.removeMail();
    }  
       
    @Override
    public void initialize(URL url, ResourceBundle rb) { //Executes after @FXML fields are initialized, use this instead of constructor
        m = new Model();
        
        //BINDINGS
//        mailcontent.textProperty().bind(m.currentMailProperty()); //changed this bind to a listener for convenience
        
        //SETUP LISTENERS AND OTHER PARAMETERS (listmail)
        listmail.setItems(m.getListMail()); //the ListView will automatically refresh the view to represent the items in the ObservableList

        listmail.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); //can only select one element at a time

        listmail.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //implementation of ChangeListener
            System.out.println("New mail selected from list");
            int newindex = (int)newValue;
            m.setCurrentMail(m.getMailByIndex(newindex));
        });     
        
        m.currentMailProperty().addListener((obsValue, oldValue, newValue) -> {
            mailcontent.setText(newValue.toString());
        });
        
        //SETUP LISTENERS AND OTHER PARAMETERS (listdrafts)
        listdraft.setItems(m.getListDraft()); 

        listdraft.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); 

        listdraft.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> {
            System.out.println("New draft selected from list");
            int newindex = (int)newValue;
            m.setCurrentDraft(m.getDraftByIndex(newindex));
        });     
        
        m.currentDraftProperty().addListener((obsValue, oldValue, newValue) -> {
            mailcontent.setText(newValue.toString());
        });
        
    }    
    
}
