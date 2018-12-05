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
import hammermail.core.User;
import static hammermail.core.Utils.containsUser;
import static hammermail.core.Utils.isNullOrWhiteSpace;
import static hammermail.core.Utils.sendRequest;
import hammermail.net.requests.RequestDeleteMails;
import hammermail.net.requests.RequestSendMail;
import hammermail.net.responses.ResponseBase;
import hammermail.net.responses.ResponseError;
import hammermail.net.responses.ResponseMailSent;
import hammermail.server.Database;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

public class UIController implements Initializable {

    private Stage s;
    
    private final String currentUser = Model.getModel().getCurrentUser().getUsername();
    
    @FXML
    private Label user;

    @FXML
    private Label fromto, subject, tofrom, loggedas;
    
    @FXML
    private Button sendButton, modifyButton, forwardButton, replyButton, replyAllButton;
    
    @FXML
    private ListView<Mail> listinbox, listsent, listdraft; 
    
    @FXML
    private TextArea mailfromto, mailtitle, maildate, mailcontent, mailtofrom;
    
    @FXML
    private TabPane tabs;
    
    @FXML
    private Tab inboxtab, senttab, drafttab; 
    
    @FXML
    private HBox bottombox;
    
    //BUTTON HANDLES
    
    @FXML
    private void handleCreate(ActionEvent event){
        openEditor("","","", false);
    }  

    @FXML 
    private void handleDelete(ActionEvent event){ 
        if(!(currentMail() instanceof EmptyMail)){
            int tabId = tabs.getSelectionModel().getSelectedIndex();
            List<Mail> mailsToDelete = composeDeletingRequest();

            if(!mailsToDelete.isEmpty()){
                Model.getModel().removeMultiple(mailsToDelete, tabId); //Tab ID and List ID are the same
            }        
        }
    }
    
    private List<Mail> composeDeletingRequest(){
        List<Mail> mailsToDelete = new ArrayList<>();
        //TODO Here add to mailsToDelete a Multiple Selection of mails from sentTab (not urgent)
        mailsToDelete.add(currentMail());
        if(!(currentMail().getDate() == null)){ //We don't need to send drafts to the database
            RequestDeleteMails request = new RequestDeleteMails(mailsToDelete);
            request.SetAuthentication(currentUser, Model.getModel().getCurrentUser().getPassword());
            ResponseBase response = null;
            try { response = sendRequest(request);} 
            catch (ClassNotFoundException | IOException ex) {
                   //TODO
            } finally {
                if (response != null){
                    System.out.println("response non null");
                    //TEMP
                    Database db = new Database(false);
                    db.dbStatus();
                }
            }
        }
        return mailsToDelete;
    }

    //INITIALIZATION
    
    public void init(Stage s){
        this.s = s;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) { //Executes after @FXML fields are initialized, use this instead of constructor
        
        //GENERIC SETUPS
        
//        Model.getModel().dispatchMail(this.currentUser);
        loggedas.setText("Logged as: " + currentUser);
        fromto.setAlignment(Pos.CENTER);
        subject.setAlignment(Pos.CENTER);
        tabInitialize(); //Never execute this again
        
        //SETUP CURRENT MAIL LISTENER
        
        (Model.getModel()).currentMailProperty().addListener((obsValue, oldValue, newValue) -> {
            if(!(currentMail() instanceof EmptyMail)){
                if(containsUser(newValue.getReceiver(), currentUser)){ //This mail was received //Fix in case of multiple users
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
                    maildate.clear(); 
                }else{
                    maildate.setText(newValue.getDate().toString()); 
                }

                mailtitle.setText(newValue.getTitle());    
                mailcontent.setText(newValue.getText());
            }else{
                clearAllSelections();
                clearAllText();
                bottombox.getChildren().clear(); //just to be safe
            }
        });
   
        //SENT LIST
        
        listsent.setItems(Model.getModel().getListSent()); //the ListView will automatically refresh the view to represent the items in the ObservableList

        listsent.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); //can only select one element at a time

        listsent.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //implementation of ChangeListener
            int newindex = (int)newValue;
            if(!listsent.getSelectionModel().isEmpty()){
                System.out.println("New mail selected from list");
                    sentTabShow(); 
                Model.getModel().setCurrentMail(Model.getModel().getSentMailByIndex(newindex));
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
                //bottombox.getChildren().clear();
                draftTabShow(); 
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
                //bottombox.getChildren().clear();
                inboxTabShow(); 
                Model.getModel().setCurrentMail(Model.getModel().getReceivedMailByIndex(newindex));
            }
        });     
  
        listinbox.setCellFactory(param -> new MailCell());
    
        //TABS 
        
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); //can't close tabs
        
        tabs.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //if tab changes clear all selections and text
            
            bottombox.getChildren().clear();
            clearAllSelections();
            System.out.println("Tab number " + newValue + " selected, list selections cleared");
                      
        });
        
        //START DAEMON PROCESS
        
        Thread daemon = new Thread(new DaemonTask());
        daemon.setDaemon(true);
//        Thread daemon = new Thread(new DaemonTask(s));
        daemon.start();
  
    }
    
    //UTILS
    
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
        
    //BOTTOM BUTTONS
    
    private void inboxTabShow(){
        if(bottombox.getChildren().isEmpty()){
            bottombox.getChildren().add(forwardButton);
            bottombox.getChildren().add(replyButton);
            bottombox.getChildren().add(replyAllButton);
        }
    }
    
    private void sentTabShow(){
        if(bottombox.getChildren().isEmpty()){
            bottombox.getChildren().add(forwardButton);
        }
    }
    
    private void draftTabShow(){
        if(bottombox.getChildren().isEmpty()){
            bottombox.getChildren().add(sendButton);
            bottombox.getChildren().add(modifyButton);
        }
    }
    
    private void tabInitialize(){
       
        sendButton = new Button("Send");
        sendButton.setOnAction((ActionEvent e) -> {
            if(currentMail().getReceiver().isEmpty()){
                handleError();
            }else{
                sendDraft();
                Model.getModel().removeDraft();
            }
        });       
        
        modifyButton = new Button("Modify");
        modifyButton.setOnAction((ActionEvent e) -> {
            openEditor(currentMail().getReceiver(), currentMail().getTitle(), currentMail().getText(), true);
            Model.getModel().removeDraft();
        });
        
        forwardButton = new Button("Forward");
        forwardButton.setOnAction((ActionEvent e) -> { 
            openEditor("", currentMail().getTitle(), "Forwarded from: " + currentMail().getSender() + " -- " + currentMail().getText(), false);
        });
        
        replyButton = new Button("Reply");
        replyButton.setOnAction((ActionEvent e) -> {
            openEditor(currentMail().getSender() , "", "", false);
        });
        
        replyAllButton = new Button("Reply All");
        replyAllButton.setOnAction((ActionEvent e) -> { 
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
        
    }
    
    //EDIT-SEND MAIL METHODS
    
    private void openEditor(String sndrcv, String title, String body, boolean modifiable){
            try{    
                clearAllSelections(); //So that when you close the editor you have nothing selected
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

    private Mail composeMail(String receiver){
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        return new Mail(-1, currentUser, receiver, currentMail().getTitle(), currentMail().getText(), ts);
    }
    
    private void sendDraft(){
        //TODO read receiver to each comma and verify it is an existent person
        //ENSURE that the current mail is the draft
        String receiver = currentMail().getReceiver();
        if(isNullOrWhiteSpace(receiver)){
            handleError(); 
        }else{
            Mail mail = composeMail(receiver);
            RequestSendMail request = new RequestSendMail(mail);
            User current = Model.getModel().getCurrentUser();
            request.SetAuthentication(current.getUsername(), current.getPassword());
            
            try {
                ResponseBase response = sendRequest(request);
                if (response instanceof ResponseError){
//                  TODO inspect the type of error
                    ResponseError.ErrorType err = ((ResponseError)response).getErrorType();
                    System.out.println(err);
                    handleError();
                } else if (response instanceof ResponseMailSent){
                    mail.setId(((ResponseMailSent) response).getMailID());
//                    Model.getModel().addMail(mail);
                    Model.getModel().removeDraft();
                }
                

            } catch (ClassNotFoundException | IOException classEx){
                System.out.println("catch2");
                handleError();
                // set the response to error internal_error
            } finally {
                //Only for testing
                Database d = new Database(false);
                d.dbStatus();
            }
        }
    }
 
}

class MailCell extends ListCell<Mail>{ //Custom cells for the list, we can show a Mail object in different ways

    @Override
    protected void updateItem(Mail item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null || item.getId() == null) {
            setText(null);
        } else {
            if(containsUser(item.getReceiver(),Model.getModel().getCurrentUser().getUsername())){ //Mail was received
                setText(item.getSender() + " - " + item.getTitle()); //URGENT FIX Daemon gives an error here
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