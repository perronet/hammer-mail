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
import hammermail.net.requests.RequestBase;
import hammermail.net.requests.RequestDeleteMails;
import hammermail.net.responses.ResponseBase;
import hammermail.net.responses.ResponseError;
import hammermail.net.responses.ResponseSuccess;
import hammermail.server.Database;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

public class UIController implements Initializable {

    @FXML
    private Label user;

    @FXML
    private Label fromto, subject, tofrom, loggedas;
    
    @FXML
    private ListView<Mail> listinbox, listsent, listdraft; 
    //renamed, Gaetano
    
    @FXML
    private TextArea mailfromto, mailtitle, maildate, mailcontent, mailtofrom;
    
    @FXML
    private TabPane tabs;
    
    @FXML
    private Tab inboxtab, senttab, drafttab; //use those to handle delete...
    
    @FXML
    private HBox bottombox;
    
    private final String currentUser = Model.getModel().getCurrentUser().getUsername();
    
    @FXML
    private void handleCreate(ActionEvent event){
    
        openEditor("","","", false);
        
    }  

    private List<Mail> composeDeletingRequest(){
        List<Mail> mailsToDelete = new ArrayList<>();
        
        //Here we add to mailsToDelete a Multiple Selection of mails from sentTab
        mailsToDelete.add(currentMail());
        RequestDeleteMails request = new RequestDeleteMails(mailsToDelete);
        request.SetAuthentication(currentUser, Model.getModel().getCurrentUser().getPassword());
        ResponseBase response = null;
        try { response = sendRequest(request);} 
        catch (ClassNotFoundException | IOException ex) {
               //TODO
        } finally {
            if (response != null){
                System.out.println("response non null");
                if (response instanceof ResponseSuccess){
                    //TEMP
                    System.out.println("Temp: Mails removed");
                    Database db = new Database(false);
                    db.dbStatus();
                    return mailsToDelete;
                } else if (response instanceof ResponseError){                     
                    ResponseError.ErrorType err = ((ResponseError)response).getErrorType();
                    System.out.println("Tipo di errore: " + err);
                }
                    
            } 
        }
        return null;
    }
    
    
    
    
    @FXML //TODO: find a way to remove from different tabs
    private void handleDelete(ActionEvent event){
       if(senttab.isSelected()){
           List<Mail> mailsToDelete = composeDeletingRequest();
           Model.getModel().getListSent().removeAll(mailsToDelete);
       
       }else if(drafttab.isSelected()){
           Model.getModel().removeDraft();
       
       }else if(inboxtab.isSelected()){
           List<Mail> mailsToDelete = composeDeletingRequest();
           Model.getModel().removeReceivedMail();
       }
    }
    
    
    @FXML
    private void handleReceive(ActionEvent event){ //just for testing
        Model.getModel().addReceivedMail("marco", "titolo", "testo");
    } 
    
    @Override
    public void initialize(URL url, ResourceBundle rb) { //Executes after @FXML fields are initialized, use this instead of constructor
        
        //GENERIC SETUPS
        
        //Model.getModel().dispatchMail(this.currentUser);
        loggedas.setText("Logged as: " + currentUser);
        fromto.setAlignment(Pos.CENTER);
        subject.setAlignment(Pos.CENTER);
        
        //SETUP CURRENT MAIL LISTENER
        
        (Model.getModel()).currentMailProperty().addListener((obsValue, oldValue, newValue) -> {
            if(newValue.getReceiver().contains(currentUser)){ //This mail was received //Fix in case of multiple users
                mailfromto.setText(newValue.getSender());
                mailtofrom.setText(newValue.getReceiver());
                fromto.setText("From");
                tofrom.setText("To");
            }else{ //This mail was sent or is draft
                mailfromto.setText(newValue.getReceiver());
                mailtofrom.setText(newValue.getSender());
                fromto.setText("To");
                tofrom.setText("From");
            }
            
            if(newValue.isDraft()){ //This handles null dates from drafts
                maildate.clear(); //TODO hide maildate textarea, don't just clear it
            }else{
                maildate.setText(newValue.getDate().toString()); 
            }
            
            mailtitle.setText(newValue.getTitle());    
            mailcontent.setText(newValue.getText());    
        });
   
        //SENT LIST
        
        listsent.setItems(Model.getModel().getListSent()); //the ListView will automatically refresh the view to represent the items in the ObservableList

        listsent.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); //can only select one element at a time

        listsent.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //implementation of ChangeListener
            int newindex = (int)newValue;
            if(!listsent.getSelectionModel().isEmpty()){
                System.out.println("New mail selected from list");
                bottombox.getChildren().clear();
                sentTabInitialize(); //TODO fix this! you should only hide and show buttons, not create new buttons every time you switch tab
                Model.getModel().setCurrentMail(Model.getModel().getMailByIndex(newindex));

            }
        });     
           
        listsent.setCellFactory(param -> new MailCell()); //the argument "param" is completely useless but you have to use it because of the Callback functional interface
        
        //DRAFT LIST
        
        listdraft.setItems(Model.getModel().getListDraft()); 

        listdraft.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); 

        listdraft.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> {
            System.out.println("New draft selected from list");
            int newindex = (int)newValue;
            if(!listdraft.getSelectionModel().isEmpty()){
                bottombox.getChildren().clear();
                draftTabInitialize(); //TODO fix this! you should only hide and show buttons, not create new buttons every time you switch tab
                Model.getModel().setCurrentMail(Model.getModel().getDraftByIndex(newindex));
            }
        });     
            
        listdraft.setCellFactory(param -> new MailCell());
        
        //INBOX LIST
        
        listinbox.setItems(Model.getModel().getListInbox());

        listinbox.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); 

        listinbox.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { 
            System.out.println("New received mail selected from list");
            int newindex = (int)newValue;
            if(!listinbox.getSelectionModel().isEmpty()){
                bottombox.getChildren().clear();
                inboxTabInitialize(); //TODO fix this! you should only hide and show buttons, not create new buttons every time you switch tab
                Model.getModel().setCurrentMail(Model.getModel().getReceivedMailByIndex(newindex));
            }
        });     
  
        listinbox.setCellFactory(param -> new MailCell());
    
        //TABS 
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); //can't close tabs
        
        tabs.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //if tab changes clear all selections and text
            bottombox.getChildren().clear();
            clearAllSelections();
            
            if((int) newValue == 1){ 
//                sentTabInitialize(); //TODO fix this! you should only hide and show buttons, not create new buttons every time you switch tab
            }else if((int) newValue == 2){
//                draftTabInitialize(); //TODO fix this! you should only hide and show buttons, not create new buttons every time you switch tab
            }else if((int) newValue == 0){
//                inboxTabInitialize(); //TODO fix this! you should only hide and show buttons, not create new buttons every time you switch tab
            }
        });
        
        //Listener that shows the right buttons in the view for each tab
        //TODOs: forward the selected mail; Remove the selected mail; Send the selected draft and notify errors
        //TODO: If you are replying or forwarding a mail, you shouldn't be able to save as draft e then modify it.
//        tabs.getSelectionModel().selectedItemProperty().addListener((ob, oldtab, newtab) -> {
//            
//        });
        
    }
    
    private Mail currentMail(){ //Use this to make the code cleaner
        return Model.getModel().getCurrentMail();
    }
    
    private void clearAllSelections(){
        listinbox.getSelectionModel().clearSelection();
        listsent.getSelectionModel().clearSelection();
        listdraft.getSelectionModel().clearSelection();
    }
    
    private void clearAllText(){
        mailfromto.clear();
        mailtitle.clear(); 
        maildate.clear(); 
        mailcontent.clear(); 
        mailtofrom.clear();
    }
    
    private void openEditor(String sndrcv, String title, String body, boolean modifiable){
        try{    
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("UIeditor.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 350, 450);
            Stage stage = new Stage();
            UIEditorController editorController = fxmlLoader.getController();
            editorController.init(stage);
            editorController.setTextAreas(sndrcv, title, body, modifiable);
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
    
    //Duplicate. To move.
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
    
    private void sentTabInitialize(){
        Button forwardButton = new Button("Forward");
        forwardButton.setOnAction((ActionEvent e) -> {
            openEditor("", currentMail().getTitle(), currentMail().getText(), false);
        });
        bottombox.getChildren().add(forwardButton);
    }
    
    private void draftTabInitialize(){
        //Qui c'è un errore, le mail vengono spostate all'interno della view ma non avviene nulla
        //a livello di DB. nel ramo else ci andrà lo stesso codice (o quasi) dell'handle send dell'editor controller
        
        Button sendButton = new Button("Send");
        sendButton.setOnAction((ActionEvent e) -> {
            if(currentMail().getReceiver().isEmpty()){
                handleError();
            }else{
                Model.getModel().addMail(currentMail().getReceiver(), currentMail().getTitle(), currentMail().getText());
                Model.getModel().removeDraft();
            }
        });
        bottombox.getChildren().add(sendButton);
                
        Button modifyButton = new Button("Modify");
        modifyButton.setOnAction((ActionEvent e) -> {
            openEditor(currentMail().getReceiver(), currentMail().getTitle(), currentMail().getText(), true);
            Model.getModel().removeDraft();
            //remove current draft and replace if saved, delete if sent
        });
        bottombox.getChildren().add(modifyButton);
    }
    
    private void inboxTabInitialize(){
        Button forwardButton = new Button("Forward");
        forwardButton.setOnAction((ActionEvent e) -> { 
            openEditor("", currentMail().getTitle(), "Forwarded from: " + currentMail().getSender() + " -- " + currentMail().getText(), false);
        });
        bottombox.getChildren().add(forwardButton);
        
        Button replyButton = new Button("Reply");
        replyButton.setOnAction((ActionEvent e) -> {
            openEditor(currentMail().getSender() , "", "", false);
        });
        bottombox.getChildren().add(replyButton);
        
        //TODO: add unmodifiable field "your mail" in the editor and set it with "currentuser"
        Button replyAllButton = new Button("Reply All");
        replyAllButton.setOnAction((ActionEvent e) -> { //TODO use currentMail()
            String receiver = currentMail().getReceiver(); 
            StringTokenizer st = new StringTokenizer(receiver, ";");
            String newReceiver = new String();
            String test = new String();
            String sender = currentMail().getSender();
            while(st.hasMoreTokens()){
                test = st.nextToken();
                if(!(test.equals(currentUser))){
                    newReceiver = newReceiver + test + ";";
                }
            }
            newReceiver = newReceiver + sender;
            openEditor(newReceiver, "", "", false);
        });
        bottombox.getChildren().add(replyAllButton);
        
    }
    
    //we need to find a common location for this method!!!
    private ResponseBase sendRequest(RequestBase request) throws ClassNotFoundException, UnknownHostException,  IOException{
        Socket socket = new Socket(Inet4Address.getLocalHost().getHostAddress(), Globals.HAMMERMAIL_SERVER_PORT_NUMBER);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(request);
        return (ResponseBase)in.readObject();
    }

    
}

class MailCell extends ListCell<Mail>{ //Custom cells for the list, we can show a Mail object in different ways

    @Override
    protected void updateItem(Mail item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null || item.getId() == null) {
            setText(null);
        } else {
            if(item.getReceiver().contains(Model.getModel().getCurrentUser().getUsername())){ //Mail was received
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