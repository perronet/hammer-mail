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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hammermail.core.User;
import hammermail.core.Mail;
import hammermail.core.EmptyMail;
import static hammermail.core.Utils.containsUser;
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
    
    private ArrayList<Mail> toStore;
    
    private Timestamp lastMailStored = new Timestamp(0);

    private int draftCounter = 0; 
    
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
    
    //LAST MAIL
    
    public Timestamp getLastMailStored() {
        return lastMailStored;
    }    

    public void setLastMailStored(Timestamp lastMailStored) {
        this.lastMailStored = lastMailStored;
    }
    
    //MAIL GETTERS
    
    public Mail getDraftByIndex(int i){
        return listDraft.get(i);
    }
    
    public Mail getSentMailByIndex(int i){
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
    
    public void addMultiple(List<Mail> mailsToAdd){
        String user = currentUser.getUsername();
        for(Mail m : mailsToAdd){
            if(m.getSender().equals(user) && containsUser(m.getReceiver(),user)){
                listSent.add(0, m);
                listInbox.add(0, m);
            }else if(containsUser(m.getReceiver(),user)){
                listInbox.add(0, m);
            }else{
                listSent.add(0, m);
            }    
        }
    }
    
    public void addMail(Mail m){
        String user = currentUser.getUsername();
        if(m.getSender().equals(user) && containsUser(m.getReceiver(),user)){
            listSent.add(0, m);
            listInbox.add(0, m);
        }else if(containsUser(m.getReceiver(),user)){
            listInbox.add(0, m);
        }else{
            listSent.add(0, m);
        }
    }
       
    public void saveDraft(String receiver, String title, String text){
        Mail mail = new Mail(draftCounter, currentUser.getUsername(), receiver, title, text, null); 
        listDraft.add(0, mail);
        storeMail(mail);
        draftCounter++;
    }    
    
    //REMOVE MAIL
    
    public void removeMultiple(List<Mail> mailsToDelete, int listId){ //Tab IDs and list IDs are the same
        
        for(Mail m : mailsToDelete){
            removeFromStorage(m);
        }
        setCurrentMail(new EmptyMail());
        switch(listId){
            case 0:
                Model.getModel().getListInbox().removeAll(mailsToDelete);
            case 1:
                Model.getModel().getListSent().removeAll(mailsToDelete);
            case 2:
                Model.getModel().getListDraft().removeAll(mailsToDelete);    
        } 
    }

    public void removeDraft(){
        listDraft.remove(getCurrentMail());
        setCurrentMail(new EmptyMail());
    }

    //JSON MANIPULATION
    
    //Forse si potrebbero mettere le funzioni di scrittura in un'altra classe
    //TO BE CHECKED!!
        //TODO: clean this dirty code, add check if mail is already in the list
    public void dispatchMail(){
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
        String user = this.currentUser.getUsername();
        JsonReader reader;
        String filepath = user + "mails" + "\\" + user + ".json";
        try {
            reader = new JsonReader(new FileReader(filepath));
            Type mailList = new TypeToken<List<Mail>>(){}.getType();
            List<Mail> mails = gson.fromJson(reader, mailList);
            if(!(mails == null)){
                addMultiple(mails);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Store mails into .json
    //Store mails into .json (each time read the file, append the new mail and write the file)
    public void storeMail(Mail mailToStore){
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
	String filepath = this.currentUser.getUserFileFolder();
        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(filepath));
            Type mailList = new TypeToken<ArrayList<Mail>>(){}.getType();
            ArrayList<Mail> test = gson.fromJson(reader, mailList);
            if(!(test == null)){
                if(!(test.contains(mailToStore))){
                    toStore = test;
                }
            } 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        toStore.add(mailToStore);
        writeJson(filepath, toStore);
        if(!(mailToStore.getDate() == null)){
                lastMailStored = mailToStore.getDate();
        }
        
        
	
    }
    
    public void removeFromStorage(Mail mailToDelete){
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
	String filepath = this.currentUser.getUserFileFolder();
        ArrayList<Mail> test = new ArrayList<>();
        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(filepath));
            Type mailList = new TypeToken<ArrayList<Mail>>(){}.getType();
            test = gson.fromJson(reader, mailList);
            if(!(test == null)){
                test.remove(mailToDelete);
            } 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        writeJson(filepath, test);      
    }
    
    private void writeJson(String filename, ArrayList<Mail> mails){
	Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
        try {
            FileWriter writer = new FileWriter(filename);
            String toWrite = gson.toJson(mails);
            writer.write(toWrite);
            writer.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
}
    
    public void createJson(String username){
        File userFile = new File(this.currentUser.getUserFileFolder());
        if(!(userFile.exists())){
            Gson file = new Gson();
            String filepath = this.currentUser.getUserFileFolder();
            File dir = new File(this.currentUser.getUsername() + "mails");
            dir.mkdir();
            try{
                JsonWriter writer = new JsonWriter(new FileWriter(filepath));
                System.out.println(filepath);

            }catch (IOException ex) {
                Logger.getLogger(UILoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public Timestamp calculateLastMailStored(){
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
	List<Mail> test = new ArrayList<>();
        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(this.currentUser.getUserFileFolder()));
            Type mailList = new TypeToken<ArrayList<Mail>>(){}.getType();
            test = gson.fromJson(reader, mailList);
            if(!(test == null)){
                for(Mail m : test){
                    if(!(m.getDate() == null)){
			lastMailStored = m.getDate();
                    }
                }
            } 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
	return lastMailStored;
	
    }

    
    

}
