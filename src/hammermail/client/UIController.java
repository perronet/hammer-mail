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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.WindowEvent;

public class UIController implements Initializable {

    private Model m;
    
    //TODO maybe add a Stage attribute
    
    @FXML
    private Label user;

    @FXML
    private Label fromto, subject;
    
    @FXML
    private ListView<Mail> listinbox, listmail, listdraft; //TODO rename listmail as "listsent"
    
    @FXML
    private TextArea mailfromto, mailtitle, maildate, mailcontent;
    
    @FXML
    private TabPane tabs;
    
    @FXML
    private Tab inboxtab, senttab, drafttab; //use those to handle delete...
    
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

    @FXML
    private void handleReceive(ActionEvent event){ //just for testing
        m.addReceivedMail("marco", "titolo", "testo");
    }  
    
    @Override
    public void initialize(URL url, ResourceBundle rb) { //Executes after @FXML fields are initialized, use this instead of constructor
        
        m = new Model();
            
        //Current mail listener
        m.currentMailProperty().addListener((obsValue, oldValue, newValue) -> {
            if(newValue.isReceived()){ //This mail was received
                mailfromto.setText(newValue.getSender());
            }else{ //This mail was sent or is draft
                mailfromto.setText(newValue.getReceiver()); 
            }
            maildate.setText(newValue.getDate().toString()); //TODO this should handle null values like in drafts, needs testing
            mailtitle.setText(newValue.getTitle());    
            mailcontent.setText(newValue.getText());    
        });
   
        //SETUP SENT LIST
        
        listmail.setItems(m.getListMail()); //the ListView will automatically refresh the view to represent the items in the ObservableList

        listmail.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); //can only select one element at a time

        listmail.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //implementation of ChangeListener
            System.out.println("New mail selected from list");
            int newindex = (int)newValue;
            if(!listmail.getSelectionModel().isEmpty()){ 
                m.setCurrentMail(m.getMailByIndex(newindex));
            }
        });     
           
        listmail.setCellFactory(param -> new MailCell()); //the argument "param" is completely useless but you have to use it because of the Callback functional interface
        
        //SETUP DRAFT LIST
        
        listdraft.setItems(m.getListDraft()); 

        listdraft.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); 

        listdraft.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> {
            System.out.println("New draft selected from list");
            int newindex = (int)newValue;
            if(!listdraft.getSelectionModel().isEmpty()){
                m.setCurrentMail(m.getDraftByIndex(newindex));
            }
        });     
            
        listdraft.setCellFactory(param -> new MailCell());
        
        //SETUP INBOX LIST
        
        listinbox.setItems(m.getListInbox());

        listinbox.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); 

        listinbox.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { 
            System.out.println("New received mail selected from list");
            int newindex = (int)newValue;
            if(!listinbox.getSelectionModel().isEmpty()){
                m.setCurrentMail(m.getReceivedMailByIndex(newindex));
            }
        });     
  
        listinbox.setCellFactory(param -> new MailCell());
        
        //OTHER SETUPS
        
        fromto.setAlignment(Pos.CENTER);
        subject.setAlignment(Pos.CENTER);
        
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); //can't close tabs
        tabs.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //if tab changes clear all selections
            clearAllSelections();
            if((int) newValue == 0){ //Inbox tab selected
                fromto.setText("From");
            }else{
                fromto.setText("To");
            }
        });
        
    }    

    private void clearAllSelections(){
        listinbox.getSelectionModel().clearSelection();
        listmail.getSelectionModel().clearSelection();
        listdraft.getSelectionModel().clearSelection();
    }
    
}

class MailCell extends ListCell<Mail>{ //Custom cells for the list, we can show a Mail object in different ways

    @Override
    protected void updateItem(Mail item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null || item.getId() == null) {
            setText(null);
        } else {
            if(item.isReceived()){ //Mail was received
                setText(item.getSender() + " - " + item.getTitle());
            }else{
                if(item.getTitle().isEmpty() || item.getReceiver().isEmpty()){ //Handle drafts with empty fields (can't use isDraft() here)
                    setText("(No subject)");
                }else{
                    setText(item.getReceiver() + " - " + item.getTitle());
                }
            }
        }            
    }
    
}