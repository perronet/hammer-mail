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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
    
    private static final Model model = new Model(); //this will execute the constructor the first time you call a method
    
    private final ObservableList<Mail> listInbox = FXCollections.observableArrayList();
    
    private final ObservableList<Mail> listSent = FXCollections.observableArrayList(); 
    
    private final ObservableList<Mail> listDraft = FXCollections.observableArrayList();
    
    private final SimpleObjectProperty<Mail> currentMail;
    
    private User currentUser;
    
    private List<Mail> toStore;
    
    private Timestamp lastMailStored;

    private int idCounter = 0; 
    
    private Model() { //TODO load mails and user from file
        currentMail = new SimpleObjectProperty<>(); 
        Timestamp ts = new Timestamp(System.currentTimeMillis()); 
        toStore = new ArrayList<>();
    }

    public static Model getModel() {
        return model;
    }
    
    //CURRENT MAIL
    
    public final Mail getCurrentMail(){ return currentMail.get(); }
    
    public final void setCurrentMail(Mail m){ currentMail.set(m); }
    
    public SimpleObjectProperty<Mail> currentMailProperty() { return currentMail; }

    //CURRENT USER
    
    public User getCurrentUser() { return currentUser; }     
     
    public void setCurrentUser(String username, String password){ this.currentUser = new User(username, password); }
    
    //MAIL GETTERS
    
    public Mail getDraftByIndex(int i){
        return listDraft.get(i);
    }
    
    public Mail getMailByIndex(int i){
        return listSent.get(i);
    }

    public Mail getReceivedMailByIndex(int i){
        return listInbox.get(i);
    }
    
    //LIST GETTERS
    
    public ObservableList<Mail> getListInbox(){ 
        return listInbox;
    }
    
    public ObservableList<Mail> getListSent(){ 
        return listSent;
    }
    
    public ObservableList<Mail> getListDraft(){ 
        return listDraft;
    }
    
    //ADD MAIL

    public void addSent(String receiver, String title, String text){
        Mail mail = new Mail(idCounter, currentUser.getUsername(), receiver, title, text, new Timestamp(System.currentTimeMillis()));
        listSent.add(mail);
        storeMail(mail);
        idCounter++;
    }

    //Questo lavoro lo farà il demone, scaricherà le mail nuove e in seguito le aggiunge alla lista inbox
    //nel login controller (che fa una get mail request) avviene la scrittura su json
    //è già tutto predisposto e commentato nel Login Controller
    //la request get mail viene fatta solo nel login controller e dal demone
    public void addInbox(String sender, String title, String text){
        Mail mail = new Mail(idCounter, sender, currentUser.getUsername(), title, text, new Timestamp(System.currentTimeMillis()));
        listInbox.add(mail); 
        storeMail(mail);
        idCounter++;
    }

    public void saveDraft(String receiver, String title, String text){
        Mail mail = new Mail(idCounter, currentUser.getUsername(), receiver, title, text, null); /*new Timestamp(System.currentTimeMillis())*/
        listDraft.add(mail);
        storeMail(mail);
        idCounter++;
    }    
    
    //REMOVE MAIL
    
    public void removeMultiple(List<Mail> mailsToDelete, int listId){ //Tab IDs and list IDs are the same
        switch(listId){
            case 0:
                Model.getModel().getListInbox().removeAll(mailsToDelete);
            case 1:
                Model.getModel().getListSent().removeAll(mailsToDelete);
            case 2:
                Model.getModel().getListDraft().removeAll(mailsToDelete);    
        }
    }
    
    public void removeSent(){ 
        listSent.remove(getCurrentMail());
    }
    
    public void removeInbox(){
        listInbox.remove(getCurrentMail());
    }

    public void removeDraft(){
        listDraft.remove(getCurrentMail());
    }

    //JSON MANIPULATION
    
    //Forse si potrebbero mettere le funzioni di scrittura in un'altra classe
    //TO BE CHECKED!!
        //TODO: clean this dirty code, add check if mail is already in the list
    public void dispatchMail(String filename){
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
        String user = this.currentUser.getUsername();
        JsonReader reader;
        String filepath = user + "mails" + "//" + user + ".json";
        //check if file exist - if not create it with create Json (maybe else-if??) 
        try {
            reader = new JsonReader(new FileReader(filepath));
            Type mailList = new TypeToken<List<Mail>>(){}.getType();
            List<Mail> mails = gson.fromJson(reader, mailList);
            if(!(mails == null)){
                for(Mail m : mails){
                    if(m.getDate() == null){
                        listDraft.add(0, m);
                    }else if(m.getSender().equals(user) && m.getReceiver().contains(user)){
                        listSent.add(0, m);
                        listInbox.add(0, m);
                    }else if(m.getReceiver().contains(user)){
                        listInbox.add(0, m);
                    }else{
                        listSent.add(0, m);
                    }
                    lastMailStored = m.getDate();
                }
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Store mails into .json
    //Store mails into .json (each time read the file, append the new mail and write the file)
    public void storeMail(Mail mailToStore){
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
	String user = this.currentUser.getUsername();
	String filepath = user + "mails" + "\\" + user  + ".json";
        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(filepath));
            Type mailList = new TypeToken<ArrayList<Mail>>(){}.getType();
            ArrayList<Mail> test = gson.fromJson(reader, mailList);
            if(!(test == null)){
                toStore = test;
            } 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        toStore.add(mailToStore);
        
        try {
            FileWriter writer = new FileWriter(filepath);
            String toWrite = gson.toJson(toStore);
            writer.write(toWrite);
            writer.close();
            lastMailStored = mailToStore.getDate();
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
