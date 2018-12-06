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
import hammermail.core.JsonPair;
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
    
    private final ObservableList<Mail> mailsToNotify = FXCollections.observableArrayList();
    
    private final SimpleObjectProperty<Mail> currentMail;
    
    private User currentUser;
    
    private Timestamp lastMailStored;

    private int draftCounter = 0; 
    
    private Model() { //TODO load mails and user from file
        currentMail = new SimpleObjectProperty<>(); 
        Timestamp ts = new Timestamp(System.currentTimeMillis()); 
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
        try {
            this.lastMailStored = lastMailStored;
            Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
            String filepath = this.currentUser.getUserFileFolder();
            JsonReader reader = new JsonReader(new FileReader(filepath));
            JsonPair toSet = gson.fromJson(reader, JsonPair.class);
            if(toSet != null){
                toSet.setLastReq(lastMailStored);
                writeJson(filepath, toSet);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    public ObservableList<Mail> getMailsToNofity(){ 
        return mailsToNotify;
    }
    
    //ADD MAIL
    
    public void addMultiple(List<Mail> mailsToAdd){
        /*
        TODO some errors are generated in these list.add when you receive mails, if you fix it you need to also copy the code into:
        addMail()
        addMultipleNoStore()
        */
        String user = currentUser.getUsername();
        for(Mail m : mailsToAdd){
            storeMail(m);
            if(m.getDate() == null){
                listDraft.add(0, m);
            }else if(m.getSender().equals(user) && containsUser(m.getReceiver(),user)){
                if (listInbox.contains(m) && listSent.contains(m))
                    return;
                listSent.add(0, m);
                listInbox.add(0, m);
            }else if(containsUser(m.getReceiver(),user)){
                System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
                listInbox.add(0, m);
            }else{
                listSent.add(0, m);
            }    
        }
    }
    
    public void addNotify(List<Mail> mails){
        mailsToNotify.addAll(mails);
    }
//    public void addMail(Mail m){ //This method is here for compatibility
//        String user = currentUser.getUsername();
//        
//        storeMail(m);
//        if(m.getDate() == null){
//            listDraft.add(0, m);
//        }else if(m.getSender().equals(user) && containsUser(m.getReceiver(),user)){
//            listSent.add(0, m);
//            listInbox.add(0, m);
//        }else if(containsUser(m.getReceiver(),user)){
//            listInbox.add(0, m);
//        }else{
//            listSent.add(0, m);
//        }    
//    }
       
    public void saveDraft(String receiver, String title, String text){
        Mail m = new Mail(draftCounter, currentUser.getUsername(), receiver, title, text, null); 
        storeMail(m);
        listDraft.add(0, m);
        draftCounter++;
    }    
    
    //REMOVE MAIL
    
    public void removeMultiple(List<Mail> mailsToDelete, int listId){ //Tab IDs and list IDs are the same
        for(Mail m : mailsToDelete){
            removeFromStorage(m);
        }
        switch(listId){
            case 0:
                Model.getModel().getListInbox().removeAll(mailsToDelete);
            case 1:
                Model.getModel().getListSent().removeAll(mailsToDelete);
            case 2:
                Model.getModel().getListDraft().removeAll(mailsToDelete);    
        } 
        setCurrentMail(new EmptyMail());
    }

    public void removeDraft(){
        removeFromStorage(getCurrentMail());
        listDraft.remove(getCurrentMail());
        setCurrentMail(new EmptyMail());
    }
    
    public void removeNotify(List<? extends Mail> m){
        mailsToNotify.removeAll(m);
    }

    //JSON MANIPULATION
    
    //JSON PRIVATE METHODS
    
    //Store mails into .json
    //Store mails into .json (each time read the file, append the new mail and write the file)
    private void storeMail(Mail mailToStore) {
        try {
            Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
            String filepath = this.currentUser.getUserFileFolder();
            boolean isPresent = false;
            List<Mail> toStore = new ArrayList<>();
            JsonPair pairTest = new JsonPair(null, null);
            JsonReader reader = new JsonReader(new FileReader(filepath));
            pairTest = gson.fromJson(reader, JsonPair.class);
            if(!(pairTest == null)){
                toStore = pairTest.getMails();
                if (!(toStore == null)) {
                    for (Mail m : toStore) {
                        if (m.hashCode() == mailToStore.hashCode()) {
                            isPresent = true;
                        }
                    }
                    if (!isPresent)
                        toStore.add(mailToStore);
                    pairTest.setMails(toStore);
                }
            }else{
                List<Mail> put = new ArrayList<>();
                put.add(mailToStore);
                if(mailToStore.getDate() != null)
                    pairTest = new JsonPair(mailToStore.getDate(), put);
                else
                    pairTest = new JsonPair(new Timestamp(0), put);
            }
            writeJson(filepath, pairTest);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void removeFromStorage(Mail mailToDelete) {
        try {
            Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
            String filepath = this.currentUser.getUserFileFolder();
            List<Mail> test = null;
            JsonReader reader = new JsonReader(new FileReader(filepath));
            JsonPair  pairTest = gson.fromJson(reader, JsonPair.class);
            if(!(pairTest == null)){
                test = pairTest.getMails();
                if (!(test == null)) {
                    test.remove(mailToDelete);
                }
                pairTest.setMails(test);
            }
            writeJson(filepath, pairTest);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void writeJson(String filename, JsonPair pairToWrite){
	Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
        try {
            FileWriter writer = new FileWriter(filename);
            String toWrite = gson.toJson(pairToWrite);
            writer.write(toWrite);
            writer.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addMultipleNoStore(List<Mail> mailsToAdd){ //This is only used in the model, using it outside would be very dangerous
        String user = currentUser.getUsername();
        
        for(Mail m : mailsToAdd){
            if(m.getDate() == null){
                listDraft.add(0, m);
            }else if(m.getSender().equals(user) && containsUser(m.getReceiver(),user)){
                listSent.add(0, m);
                listInbox.add(0, m);
            }else if(containsUser(m.getReceiver(),user)){
                listInbox.add(0, m);
            }else{
                listSent.add(0, m);
            }    
        }
    }
    
    //JSON PUBLIC METHODS

    //Forse si potrebbero mettere le funzioni di scrittura in un'altra classe
    //TO BE CHECKED!!
        //TODO: clean this dirty code, add check if mail is already in the list
    public void dispatchMail(List<Mail> newReceived, List<Mail> newSent){

        
        //STEP 2: Load already stored mails into the model
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
        JsonReader reader;
        String filepath = this.currentUser.getUserFileFolder();
        List<Mail> mails;
        try {
            reader = new JsonReader(new FileReader(filepath));
            JsonPair pairToRead = gson.fromJson(reader, JsonPair.class);
            if(!(pairToRead == null)){
                 mails = pairToRead.getMails();
                if(!(mails == null)){ //Loads mails from json
                    addMultipleNoStore(mails);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        //STEP 1: Store and load into model new mails received from the server
        if(!(newReceived == null))
            addMultiple(newReceived);
        if(!(newSent == null))
            addMultiple(newSent);
    }
    
    public void createJson(String username){
        File userFile = new File(this.currentUser.getUserFileFolder());
        if(!(userFile.exists())){
            Gson file = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
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
        File userFile = new File(this.currentUser.getUserFileFolder());
        if(!userFile.exists()){
            createJson(this.currentUser.getUsername());
        }
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
	JsonPair pairTest = null;
        List<Mail> test = null;
        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(this.currentUser.getUserFileFolder()));
            pairTest = gson.fromJson(reader, JsonPair.class);
            if(!(pairTest == null)){
                lastMailStored = pairTest.getLastReq();
            }else{
                lastMailStored = new Timestamp(0);
            } 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(lastMailStored == null)
            lastMailStored = new Timestamp(0);
	return lastMailStored;
	
    }
    
    public void deleteJson(){
        File dir = new File(this.currentUser.getUserFileFolder());
	File[] listJson = dir.listFiles();
        if(listJson != null){
            for(File file : listJson){
                file.delete();
            }
        }
        dir.delete();
	System.out.println("Deleted");
    }

}
