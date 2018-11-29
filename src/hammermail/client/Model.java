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

import hammermail.core.User;
import hammermail.core.Mail;
import java.sql.Timestamp;
import java.util.ArrayList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
    
    private static Model model = new Model();
    
    private final ObservableList<Mail> listInbox = FXCollections.observableArrayList();
    
    private final ObservableList<Mail> listSent = FXCollections.observableArrayList(); //TODO rename it "listSent"
    
    private final ObservableList<Mail> listDraft = FXCollections.observableArrayList();
    
    private final SimpleObjectProperty<Mail> currentMail;
    
    private User currentUser;

//    private ArrayList<User> listUser;

    private int idCounter = 3;
    
    //Use this         
    //listUser.add(new User("marco", "qwerty", "marco.maida@hammermail.com")); 
    //listUser.add(new User("andrea", "12345", "andrea.rondinelli@hammermail.com"));
    //listUser.add(new User("gaetano", "12345", "gaetano.97@hammermail.com"));    
    
    public Model() { //TODO load mails and user from file 
        currentMail = new SimpleObjectProperty<>(); 
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        
        //Gaetano: Why in the costructor? 
        setCurrentMail(new Mail(0, "Test", "andrea", "Select a mail from the list", "ciao", ts));

//        listSent.setAll(
//            new Mail(0, "marco", "andrea", "Mailzero", "ciao", ts),
//            new Mail(1, "andrea", "gaetano", "Mailuno", "ue gaetà", ts),
//            new Mail(2, "marco", "gaetano", "Maildue", "hey come stai", ts)
//        );
        
    }

    public static Model getModel() {
        return model;
    }
   
    
    
    public final Mail getCurrentMail(){ return currentMail.get(); }
    
    public final void setCurrentMail(Mail m){ currentMail.set(m); }
    
    public SimpleObjectProperty<Mail> currentMailProperty() { return currentMail; }
    
    //Inbox
    public ObservableList<Mail> getListInbox(){ 
        return listInbox;
    }
    
    public void addReceivedMail(String sender, String title, String text){
        listInbox.add(new Mail(idCounter, sender, null, title, text, new Timestamp(System.currentTimeMillis()))); //Receiver needs to be null
        idCounter++;
    }

    public void removeReceivedMail(){ //TODO remove specified element 
        listInbox.remove(listInbox.size()-1);
    }
    
    public Mail getReceivedMailByIndex(int i){
        return listInbox.get(i);
    }
    
    //Sent
    public ObservableList<Mail> getListSent(){ 
        return listSent;
    }
    
    //we can bypass this, look UIEditorController, row 89, handleSend 
    public void addMail(String receiver, String title, String text){
        listSent.add(new Mail(idCounter, "marco", receiver, title, text, new Timestamp(System.currentTimeMillis())));
        idCounter++;
    }

    public void removeMail(){ //TODO remove specified element 
        listSent.remove(listSent.size()-1);
    }
    
    public Mail getMailByIndex(int i){
        return listSent.get(i);
    }
    
    //Drafts  

    public ObservableList<Mail> getListDraft(){ 
        return listDraft;
    }
    
    public void saveDraft(String receiver, String title, String text){
        listDraft.add(new Mail(idCounter, "marco", receiver, title, text, new Timestamp(System.currentTimeMillis())));
        idCounter++;
    }
    
    public void removeDraft(){//OutOfBound exception when list is empty
        listDraft.remove(listDraft.size()-1);
    }
    
     public Mail getDraftByIndex(int i){
        return listDraft.get(i);
    }
    
    public User getCurrentUser() {
        return currentUser;
    }     
     
    public void setCurrentUser(String username, String password){
         this.currentUser = new User(username, password);
    }
}
