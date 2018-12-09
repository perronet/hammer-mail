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
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
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
import javafx.scene.text.TextAlignment;

public class UIController implements Initializable {

    private Stage s;

    private final String currentUser = Model.getModel().getCurrentUser().getUsername();

    @FXML
    private Label fromto, tofrom, loggedas, mailfromto, mailtofrom, mailtitle;

    @FXML
    private Button sendButton, modifyButton, forwardButton, replyButton, replyAllButton, deleteButton;

    @FXML
    private ListView<Mail> listinbox, listsent, listdraft;

    @FXML
    private TextArea maildate, mailcontent;

    @FXML
    private TabPane tabs;

    @FXML
    private VBox noMailBox, mailBox;

    //BUTTON HANDLES
    @FXML
    private void handleCreate(ActionEvent event) {
        openEditor("", "", "", false);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (!(currentMail() instanceof EmptyMail)) {
            int tabId = tabs.getSelectionModel().getSelectedIndex();
            List<Mail> mailsToDelete = composeDeletingRequest();

            if (mailsToDelete == null){
                spawnError("Unable to contact server");
                System.out.println("Unable to contact server");
            }
            if (!mailsToDelete.isEmpty()) { //just to be sure
                Model.getModel().removeMultiple(mailsToDelete, tabId); //Tab ID and List ID are the same
            }
        }
    }

    private List<Mail> composeDeletingRequest() {
        List<Mail> mailsToDelete = new ArrayList<>();
        mailsToDelete.add(currentMail());
        if (!(currentMail().getDate() == null)) { //We don't need to send drafts to the database
            RequestDeleteMails request = new RequestDeleteMails(mailsToDelete);
            request.SetAuthentication(currentUser, Model.getModel().getCurrentUser().getPassword());
            ResponseBase response = null;
            try {
                response = sendRequest(request);

                if (response instanceof ResponseError) {
                    return null;
                } else if (response instanceof ResponseRetrieve) {
                    //TODO
                }

            } catch (ClassNotFoundException | IOException ex) {
                //TODO
            } finally {
//                if (response != null){
//                    System.out.println("response non null");
//                    Database db = new Database(false);
//                    db.dbStatus();
//                }
            }
        }
        return mailsToDelete;
    }

    //INITIALIZATION
    public void init(Stage s) {
        this.s = s;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) { //Executes after @FXML fields are initialized, use this instead of constructor
        clearAllSelections();
        //GENERIC SETUPS
//        Model.getModel().dispatchMail(this.currentUser);
        loggedas.setText(currentUser);
        fromto.setAlignment(Pos.CENTER);
        tabInitialize(); //Never execute this again

        //SETUP CURRENT MAIL LISTENER
        (Model.getModel()).currentMailProperty().addListener((obsValue, oldValue, newValue) -> {
            if (!(currentMail() instanceof EmptyMail)) {
                if (containsUser(newValue.getReceiver(), currentUser)) { //This mail was received
                    mailfromto.setText(newValue.getSender());
                    mailtofrom.setText(newValue.getReceiver());
                    fromto.setText("From");
                    tofrom.setText("To");
                } else { //This mail was sent or is draft
                    mailfromto.setText(newValue.getReceiver());
                    mailtofrom.setText(newValue.getSender());
                    fromto.setText("To");
                    tofrom.setText("From");
                }

                if (newValue.isDraft()) { //This handles null dates from drafts
                    maildate.clear();
                } else {
                    maildate.setText(newValue.getDate().toString());
                }

                mailtitle.setText(newValue.getTitle().trim());
                mailcontent.setText(newValue.getText());
            } else {
                clearAllSelections();
                clearAllText();
            }
        });

        //SETUP NOTIFICATION LISTENER
        Model.getModel().getMailsToNofity().addListener((ListChangeListener.Change<? extends Mail> change) -> {
            change.next();
            if (change.wasAdded()) {
                inboxNotify(change.getAddedSubList()); //Notifies then removes notification from list
            }
        });

        //SENT LIST
        listsent.setItems(Model.getModel().getListSent()); //the ListView will automatically refresh the view to represent the items in the ObservableList

        listsent.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); //can only select one element at a time

        listsent.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //implementation of ChangeListener
            System.out.println("New mail selected from sent list");
            int newindex = (int) newValue;
            if (!listsent.getSelectionModel().isEmpty()) {
                sentTabShow();
                Model.getModel().setCurrentMail(Model.getModel().getSentMailByIndex(newindex));
                updateSelectedMailView();
            }
        });

        listsent.setCellFactory(param -> new MailCell()); //the argument "param" is completely useless but you have to use it because of the Callback functional interface

        //DRAFT LIST
        listdraft.setItems(Model.getModel().getListDraft());

        listdraft.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        listdraft.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> {
            System.out.println("New draft selected");
            int newindex = (int) newValue;
            if (!listdraft.getSelectionModel().isEmpty()) {
                draftTabShow();
                Model.getModel().setCurrentMail(Model.getModel().getDraftByIndex(newindex));
            }
        });

        listdraft.setCellFactory(param -> new MailCell());

        //INBOX LIST
        listinbox.setItems(Model.getModel().getListInbox());

        listinbox.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        listinbox.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> {
            System.out.println("New mail selected from inbox");
            int newindex = (int) newValue;
            if (!listinbox.getSelectionModel().isEmpty()) {
                inboxTabShow();
                Model.getModel().setCurrentMail(Model.getModel().getReceivedMailByIndex(newindex));
                updateSelectedMailView();
            }
        });

        listinbox.setCellFactory(param -> new MailCell());

        //TABS 
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); //can't close tabs

        tabs.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //if tab changes clear all selections and text
            clearAllSelections();
            System.out.println("Tab number " + newValue + " selected, list selections cleared");
        });

        updateSelectedMailView();
        
        //START DAEMON PROCESS
        Thread daemon = new Thread(new DaemonTask());
        daemon.setDaemon(true);
        daemon.start();

    }

    //UTILS
    private Mail currentMail() { //Use this to make the code cleaner
        return Model.getModel().getCurrentMail();
    }

    private void clearAllSelections() {
        listinbox.getSelectionModel().clearSelection();
        listsent.getSelectionModel().clearSelection();
        listdraft.getSelectionModel().clearSelection();

        Utils.toggleCollapse(deleteButton, false);
        Utils.toggleCollapse(forwardButton, false);
        Utils.toggleCollapse(replyButton, false);
        Utils.toggleCollapse(replyAllButton, false);
        Utils.toggleCollapse(sendButton, false);
        Utils.toggleCollapse(modifyButton, false);
        updateSelectedMailView();
    }

    private void updateSelectedMailView() {
        boolean isMailSelected = Model.getModel().getCurrentMail() != null && !(Model.getModel().getCurrentMail() instanceof EmptyMail);
        Utils.toggleCollapse(mailBox, isMailSelected);
        Utils.toggleCollapse(noMailBox, !isMailSelected);
    }   

    private void clearAllText() {
        mailfromto.setText("");
        mailtitle.setText("");
        maildate.clear();
        mailcontent.clear();
        mailtofrom.setText("");
    }

    //BOTTOM BUTTONS
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

    private void tabInitialize() {

        sendButton.setOnAction((ActionEvent e) -> {
            if (currentMail().getReceiver().isEmpty()) {
                spawnError("Invalid receiver");
            } else {
                sendDraft();
                Model.getModel().removeDraft();
            }
        });

        modifyButton.setOnAction((ActionEvent e) -> {
            openEditor(currentMail().getReceiver(), currentMail().getTitle(), currentMail().getText(), true);
            Model.getModel().removeDraft();
        });

        forwardButton.setOnAction((ActionEvent e) -> {
            openEditor("", currentMail().getTitle(), "Forwarded from: " + currentMail().getSender() + " -- " + currentMail().getText(), false);
        });

        replyButton.setOnAction((ActionEvent e) -> {
            openEditor(currentMail().getSender(), "", "", false);
        });

        replyAllButton.setOnAction((ActionEvent e) -> {
            String receiver = currentMail().getReceiver();
            StringTokenizer st = new StringTokenizer(receiver, ";");
            String newReceiver = new String();
            String test = new String();
            String sender = currentMail().getSender();
            while (st.hasMoreTokens()) {
                test = st.nextToken();
                if (!(test.equals(currentUser))) {
                    newReceiver = newReceiver + test + ";";
                }
            }
            newReceiver = newReceiver + sender;
            openEditor(newReceiver, "", "", false);
        });

    }

    //EDIT-SEND MAIL METHODS
    private void openEditor(String sndrcv, String title, String body, boolean modifiable) {
        try {
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

        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private Mail composeMail(String receiver) {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        return new Mail(-1, currentUser, receiver, currentMail().getTitle(), currentMail().getText(), ts);
    }

    private void sendDraft() {
        //TODO read receiver to each comma and verify it is an existent person
        //ENSURE that the current mail is the draft
        String receiver = currentMail().getReceiver();
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
//                  TODO inspect the type of error
                    ResponseError.ErrorType err = ((ResponseError) response).getErrorType();
                    System.out.println(err);
                    spawnError("Error response received: " + err.toString());
                } else if (response instanceof ResponseMailSent) {
                    Model.getModel().removeDraft();
                } else if (response instanceof ResponseRetrieve) {
                    //TODO
                }

            } catch (ClassNotFoundException | IOException classEx) {
                System.out.println("catch2");
                spawnError("Internal error");
                // set the response to error internal_error
            } finally {
                //Only for testing
//                Database d = new Database(false);
//                d.dbStatus();
            }
        }
    }

    //POPUP NOTIFICATION 
    String path = "sound/andre_hammer.mp3";
    Media sound = new Media(new File(path).toURI().toString());

    public void inboxNotify(List<? extends Mail> newMails) {
        PopupControl popup = new PopupControl();
        MediaPlayer hammer = new MediaPlayer(sound);
        CornerRadii corner = new CornerRadii(8, 0, 0, 0, false);
        Insets insets = new Insets(15);
        Integer adjustment = 7;
        AnchorPane pane = new AnchorPane();

        Label label = new Label();
        label.getStylesheets().add("../dark.css"); //FIXME wrong css path
        label.setStyle("label-enveloped");
        label.setTextFill(Color.WHITESMOKE);
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
                new Background(new BackgroundFill(Color.DARKSLATEGREY, corner, Insets.EMPTY))
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
            popup.setX(s.getX() + s.getWidth() - popup.getWidth() - adjustment);
            popup.setY(s.getY() + s.getHeight() - popup.getHeight() - adjustment);
            hammer.play();
        });
        popup.show(s);
    }

}

class MailCell extends ListCell<Mail> { //Custom cells for the list, we can show a Mail object in different ways

    @Override
    protected void updateItem(Mail item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null || item.getId() == null) {
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
