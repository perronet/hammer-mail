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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hammermail.core.User;
import hammermail.core.Mail;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
    
    private static final Model model = new Model(); //this will execute the constructor the first time you call a method
    
    private final ObservableList<Mail> listInbox = FXCollections.observableArrayList();
    
    private final ObservableList<Mail> listSent = FXCollections.observableArrayList(); //TODO rename it "listSent"
    
    private final ObservableList<Mail> listDraft = FXCollections.observableArrayList();
    
    private final SimpleObjectProperty<Mail> currentMail;
    
    private User currentUser;

    private int idCounter = 3; 
    
    private Model() { //TODO load mails and user from file
        currentMail = new SimpleObjectProperty<>(); 
        Timestamp ts = new Timestamp(System.currentTimeMillis());

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
        Mail mail = new Mail(idCounter, sender, currentUser.getUsername(), title, text, new Timestamp(System.currentTimeMillis()));
        listInbox.add(mail); //Receiver needs to be null
        storeMail(mail);
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
        Mail mail = new Mail(idCounter, currentUser.getUsername(), receiver, title, text, new Timestamp(System.currentTimeMillis()));
        listSent.add(mail);
        storeMail(mail);
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
        Mail mail = new Mail(idCounter, currentUser.getUsername(), receiver, title, text, null /*new Timestamp(System.currentTimeMillis())*/);
        listDraft.add(mail);
        storeMail(mail);
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
    
    //TO BE CHECKED!!
    public void dispatchMail(String filename){
        Gson gson = new Gson();
        String user = this.currentUser.getUsername();
        JsonReader reader;
        //check if file exist - if not create it with create Json (maybe else-if??) 
        try {
            reader = new JsonReader(new FileReader(filename));
            Type mailList = new TypeToken<List<Mail>>(){}.getType();
            List<Mail> mails = gson.fromJson(reader, Mail.class); 
            for(Mail m : mails){
                if(m.getDate() == null){
                    listDraft.add(m);
                }else if(m.getSender().equals(user) && m.getReceiver().contains(user)){
                    listSent.add(m);
                    listInbox.add(m);
                }else if(m.getReceiver().contains(user)){
                    listInbox.add(m);
                }else{
                    listSent.add(m);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Store mails into .json
    public void storeMail(Mail mailToStore){
        //String json = "{\"Mail\":[{\"Sender\":\"value\",\"Receiver\":\"value\" }, { \"lat\":\"value\", \"lon\":\"value\"}]}";
        Gson gson = new GsonBuilder().serializeNulls().create();
	String user = this.currentUser.getUsername();
	String filepath = user + "mails" + "\\" + user  + ".json";

        try {
            FileWriter writer = new FileWriter(filepath, true);
            writer.append(gson.toJson(mailToStore));
            writer.append("\n");
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        
	
    }
    
    public void createJson(String username){
        Gson file = new Gson();
        String namedir = username + "mails";
        String filepath = namedir + "\\" + username + ".json";
        File dir = new File(namedir);
        dir.mkdir();
        try{
            JsonWriter writer = new JsonWriter(new FileWriter(filepath));
            System.out.println(filepath);

        }catch (IOException ex) {
            Logger.getLogger(UILoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
