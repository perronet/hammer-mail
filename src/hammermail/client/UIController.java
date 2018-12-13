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
import hammermail.core.Utils;
import static hammermail.core.Utils.containsUser;
import static hammermail.core.Utils.isNullOrWhiteSpace;
import static hammermail.core.Utils.sendRequest;
import static hammermail.core.Utils.spawnError;
import hammermail.net.requests.*;
import hammermail.net.responses.*;
import hammermail.net.responses.ResponseError.ErrorType;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import static hammermail.core.Utils.spawnErrorIfWrongReceivers;

public class UIController implements Initializable {

    private Stage stage;

    private final String currentUser = Model.getModel().getCurrentUser().getUsername();

    @FXML
    private Label fromLabel, toLabel, subjectLabel, usernameLabel;

    @FXML
    private Button newMailButton, sendButton, modifyButton, forwardButton, replyButton, replyAllButton, deleteButton;

    @FXML
    private ListView<Mail> listinbox, listsent, listdraft;

    @FXML
    private TextArea maildate, mailcontent;

    @FXML
    private TabPane tabPane;

    @FXML
    private VBox noMailBox, mailBox;

    Thread daemon;

    @Override
    public void initialize(URL url, ResourceBundle rb) { //Executes after @FXML fields are initialized, use this instead of constructor
        usernameLabel.setText(currentUser);

        //Setting up current mail listener
        Model.getModel().currentMailProperty().addListener((obsValue, oldValue, newValue) -> {
            boolean isEmptyMail = getCurrentMail() instanceof EmptyMail;
            if (!isEmptyMail) {
                fromLabel.setText(newValue.getSender());
                toLabel.setText(newValue.getReceiver());

                if (newValue.isDraft()) { //This handles null dates from drafts
                    maildate.clear();
                } else {
                    SimpleDateFormat sdfr = new SimpleDateFormat("dd MMM yyyy - HH:mm");
                    maildate.setText(sdfr.format(newValue.getDate()));
                }

                subjectLabel.setText(newValue.getTitle().trim());
                mailcontent.setText(newValue.getText());
            }else{
                hideAllButtons();
            }
            //Hide main pane if the mail is empty
            Utils.toggleCollapse(mailBox, !isEmptyMail);  
            
            //Show "empty mail" pane if the mail is empty
            Utils.toggleCollapse(noMailBox, isEmptyMail);
        });

        clearAllSelections();
        initializeButtons();
        initializeLists();

        selectFirstMail(listinbox);

        //Setting up notifications
        Model.getModel().getMailsToNotify().addListener((ListChangeListener.Change<? extends Mail> change) -> {
            change.next();
            if (change.wasAdded()) {
                inboxNotify(change.getAddedSubList()); //Notifies then removes notification from list
            }
        });

        //Setting up tab change
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //if tab changes clear all selections and text
            clearAllSelections();
            System.out.println("Tab number " + newValue + " selected, list selections cleared");

            switch ((int) newValue) {
                case 0:
                    selectFirstMail(listinbox);
                    break;
                case 1:
                    selectFirstMail(listsent);
                    break;
                case 2:
                    selectFirstMail(listdraft);
                    break;
            }
        });

        //Starting daemon
        daemon = new Thread(new DaemonTask());
        daemon.setDaemon(true);
        daemon.start();
    }

    private void initializeLists() {
        //Sent list
        listsent.setItems(Model.getModel().getListSent()); //the ListView will automatically refresh the view to represent the items in the ObservableList
        listsent.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); //can only select one element at a time
        listsent.setCellFactory(param -> new MailCell()); //the argument "param" is completely useless but you have to use it because of the Callback functional interface
        listsent.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //implementation of ChangeListener
            int newindex = (int) newValue;

            if (!listsent.getSelectionModel().isEmpty() && newindex >= 0) {
                System.out.println("New mail selected from sent list");
                sentTabShow();
                Model.getModel().setCurrentMail(Model.getModel().getSentMailByIndex(newindex));
            }
        });

        //Draft list
        listdraft.setItems(Model.getModel().getListDraft());
        listdraft.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listdraft.setCellFactory(param -> new MailCell());
        listdraft.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> {
            int newindex = (int) newValue;

            if (!listdraft.getSelectionModel().isEmpty() && newindex >= 0) {
                System.out.println("New draft selected");
                draftTabShow();
                Model.getModel().setCurrentMail(Model.getModel().getDraftByIndex(newindex));
            }
        });

        //Inbox list
        listinbox.setItems(Model.getModel().getListInbox());
        listinbox.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listinbox.setCellFactory(param -> new MailCell());
        listinbox.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> {
            int newindex = (int) newValue;

            if (!listinbox.getSelectionModel().isEmpty() && newindex >= 0) {
                System.out.println("New mail selected from inbox");
                inboxTabShow();
                Model.getModel().setCurrentMail(Model.getModel().getReceivedMailByIndex(newindex));
            }
        });
    }

    private void initializeButtons() {
        newMailButton.setOnAction((ActionEvent e) -> openEditor("", "", "", false));

        deleteButton.setOnAction((ActionEvent e) -> {
            if (!(getCurrentMail() instanceof EmptyMail)) {
                int tabId = tabPane.getSelectionModel().getSelectedIndex();
                List<Mail> mailsToDelete = composeDeletingRequest();

                if (mailsToDelete == null) {
                    spawnError("Unable to contact server");
                } else {
                    Model.getModel().removeMultiple(mailsToDelete, tabId); //Tab ID and List ID are the same

                    clearAllSelections();
                    Model.getModel().setCurrentMail(new EmptyMail());
                }
            }
        });

        sendButton.setOnAction((ActionEvent e) -> {
            if (getCurrentMail().getReceiver().isEmpty()) {
                spawnError("Invalid receiver");
            } else {
                if (sendDraft()) {
                    Model.getModel().removeDraft();
                }
            }
        });

        modifyButton.setOnAction((ActionEvent e) -> {
            openEditor(getCurrentMail().getReceiver(), getCurrentMail().getTitle(), getCurrentMail().getText(), true);
            Model.getModel().removeDraft();
        });

        forwardButton.setOnAction((ActionEvent e) -> {
            openEditor("", getCurrentMail().getTitle(), "Forwarded from: " + getCurrentMail().getSender() + " -- " + getCurrentMail().getText(), false);
        });

        replyButton.setOnAction((ActionEvent e) -> {
            openEditor(getCurrentMail().getSender(), "", "", false);
        });

        replyAllButton.setOnAction((ActionEvent e) -> {
            String receiver = getCurrentMail().getReceiver();
            StringTokenizer st = new StringTokenizer(receiver, ";");
            String newReceiver = new String();
            String sender = getCurrentMail().getSender();
            while (st.hasMoreTokens()) {
                String test = st.nextToken();
                if (!(test.equals(currentUser))) {
                    newReceiver = newReceiver + test + ";";
                }
            }
            newReceiver = newReceiver + sender;
            openEditor(newReceiver, "", "", false);
        });
    }

    public void setStage(Stage s) {
        this.stage = s;
    }

    private Mail getCurrentMail() {
        return Model.getModel().getCurrentMail();
    }

    private void selectFirstMail(ListView<Mail> list) {
        if (!list.getItems().isEmpty()) {
            list.getSelectionModel().select(0);
        } else {
            Model.getModel().setCurrentMail(new EmptyMail());
        }
    }

    private void clearAllSelections() {
        listinbox.getSelectionModel().clearSelection();
        listsent.getSelectionModel().clearSelection();
        listdraft.getSelectionModel().clearSelection();
        hideAllButtons();
    }

    //SHOW-HIDE BUTTONS
    private void inboxTabShow() {
        Utils.toggleCollapse(deleteButton, true);
        Utils.toggleCollapse(forwardButton, true);
        Utils.toggleCollapse(replyButton, true);
        Utils.toggleCollapse(replyAllButton, true);
    }

    private void sentTabShow() {
        Utils.toggleCollapse(deleteButton, true);
        Utils.toggleCollapse(forwardButton, true);
    }

    private void draftTabShow() {
        Utils.toggleCollapse(deleteButton, true);
        Utils.toggleCollapse(sendButton, true);
        Utils.toggleCollapse(modifyButton, true);
    }

    private void hideAllButtons(){
        Utils.toggleCollapse(deleteButton, false);
        Utils.toggleCollapse(forwardButton, false);
        Utils.toggleCollapse(replyButton, false);
        Utils.toggleCollapse(replyAllButton, false);
        Utils.toggleCollapse(sendButton, false);
        Utils.toggleCollapse(modifyButton, false);
    }
    
    //EDIT-SEND MAIL METHODS
    private void openEditor(String sndrcv, String title, String body, boolean modifiable) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("UIeditor.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 350, 450);
            Stage stage = new Stage();
            UIEditorController editorController = fxmlLoader.getController();
            editorController.setStage(stage);
            editorController.setTextAreas(sndrcv, title, body, modifiable);
            stage.setTitle("Write a mail...");
            stage.setScene(scene);
            stage.show();

            // Handler to save drafts when closing the window
            stage.setOnCloseRequest(e -> {
                editorController.handleSave(e);
                System.out.println("Stage is closing");
            });

        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private Mail composeMail(String receiver) {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        return new Mail(-1, currentUser, receiver, getCurrentMail().getTitle(), getCurrentMail().getText(), ts);
    }

    private boolean sendDraft() {
        String receiver = getCurrentMail().getReceiver();
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
                            spawnError("Unexistent receiver\nError code: " + err.toString()); break;
                        
                        case SENDING_INVALID_MAIL:
                            spawnError("Invalid mail\nError code: " + err.toString()); break;
                        
                        case INTERNAL_ERROR:
                            spawnError("Server: Internal error\nError code: " + err.toString()); break;
                    }
                    return false;
                    
                }else if (response instanceof ResponseMailSent) {
                    spawnErrorIfWrongReceivers((ResponseMailSent)response);
                    return true;
                    
                }else if (response instanceof ResponseRetrieve) {
                    response = sendRequest(request);
                    int count = 0;
                    while (response instanceof ResponseRetrieve && count < 5){
                        response = sendRequest(request);
                        count++;
                    }

                    if (response instanceof ResponseError || response instanceof ResponseRetrieve) {
                        spawnError("Unable to contact server, retry to send");
                        return false;
                    }else{
                        spawnErrorIfWrongReceivers((ResponseMailSent)response);
                        return true;
                    }
                }

            } catch (ClassNotFoundException | IOException classEx) {
                spawnError("Internal error");
                return false;
            }
        }
        
        return false;
    }

    //POPUP NOTIFICATION 
    String path = "src/hammermail/resources/andre_hammer.mp3";
    Media sound = new Media(new File(path).toURI().toString());

    public void inboxNotify(List<? extends Mail> newMails) {
        PopupControl popup = new PopupControl();
        MediaPlayer hammer = new MediaPlayer(sound);
        CornerRadii corner = new CornerRadii(8, 0, 0, 0, false);
        Insets insets = new Insets(15);
        Integer adjustment = 7;
        AnchorPane pane = new AnchorPane();

        Label label = new Label();
        label.setTextFill(Paint.valueOf("#e2e2e2"));
        label.setPadding(insets);
        label.setTextAlignment(TextAlignment.CENTER);

        if (newMails.size() == 1) {
            label.setText("New mail from " + newMails.get(0).getSender() + "!\nSubject: " + newMails.get(0).getTitle());
        } else {
            String text = "You got " + newMails.size() + " new mails!";
            for (Mail m : newMails) {
                text = text.concat("\n" + m.getSender() + " - Subject: " + m.getTitle());
            }
            label.setText(text);
        }

        //I should've used CSS, i know...
        pane.getChildren().add(label);
        pane.setBackground(
            new Background(new BackgroundFill(Paint.valueOf("#3A506B"), corner, Insets.EMPTY))
        );
        pane.setBorder(
            new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, corner, new BorderWidths(1.5)))
        );

        pane.setOnMouseReleased((e) -> { //MouseEvent implementation
            popup.hide();
        });
        popup.getScene().setRoot(pane);
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        popup.setOnHidden((e) -> {
            Model.getModel().removeNotify(newMails);
        });

        //HAMMER TIME
        hammer.setVolume(.5);
        popup.setOnShown((e) -> {
            popup.setX(stage.getX() + stage.getWidth() - popup.getWidth() - adjustment);
            popup.setY(stage.getY() + stage.getHeight() - popup.getHeight() - adjustment);
            hammer.play();
        });
        popup.show(stage);
    }

    //Currently deletes only one mail, but it's ready to delete multiple
    private List<Mail> composeDeletingRequest() {
        List<Mail> mailsToDelete = new ArrayList<>();
        mailsToDelete.add(getCurrentMail());

        if (!getCurrentMail().isDraft()) { //We don't need to send drafts to the database
            RequestDeleteMails request = new RequestDeleteMails(mailsToDelete);
            request.SetAuthentication(currentUser, Model.getModel().getCurrentUser().getPassword());
            try {
                ResponseBase response = sendRequest(request);

                if (response instanceof ResponseError) {
                    return null;
                } else if (response instanceof ResponseRetrieve) {
                    response = sendRequest(request);
                    int count = 0;
                    while (response instanceof ResponseRetrieve && count < 5){
                        response = sendRequest(request);
                        count++;
                    }

                    if (response instanceof ResponseError || response instanceof ResponseRetrieve) {
                        return null;
                    } 
                }
            } catch (ClassNotFoundException | IOException ex) {
                //TODO
            }
        }  
        return mailsToDelete;
    }
}

class MailCell extends ListCell<Mail> { //Custom cells for the list, we can show a Mail object in different ways

    @Override
    protected void updateItem(Mail item, boolean isEmpty) {
        super.updateItem(item, isEmpty);

        if (isEmpty || item == null || item.getId() == null) {
            setText(null);
        } else {
            if (containsUser(item.getReceiver(), Model.getModel().getCurrentUser().getUsername())) { //Mail was received
                setText(item.getSender() + " - " + item.getTitle());
            } else {
                if (item.getTitle().isEmpty() || item.getReceiver().isEmpty()) { //Handle drafts with empty fields (can't use isDraft() here)
                    setText("(No subject)");
                } else {
                    setText(item.getReceiver() + " - " + item.getTitle());
                }
            }
        }
    }

}
