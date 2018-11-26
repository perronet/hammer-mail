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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
    
    private final ObservableList<Mail> listMail = FXCollections.observableArrayList();
    
    private final ObservableList<Mail> listDraft = FXCollections.observableArrayList();//invisible
    
    private final SimpleObjectProperty<Mail> currentMail;
    
    private final SimpleObjectProperty<Mail> currentDraft;
    
    private ArrayList<User> listUser;

    private int idCounter = 3;
    
    //Use this         
    //listUser.add(new User("marco", "qwerty", "marco.maida@hammermail.com")); 
    //listUser.add(new User("andrea", "12345", "andrea.rondinelli@hammermail.com"));
    //listUser.add(new User("gaetano", "12345", "gaetano.97@hammermail.com"));    
    
    public Model() { //TODO load mails and user from file 
        currentMail = new SimpleObjectProperty<>(); 
        currentDraft = new SimpleObjectProperty<>();
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        setCurrentMail(new Mail(0, "Test", "andrea", "Select a mail from the list", "ciao", ts));

        listMail.setAll(
            new Mail(0, "marco", "andrea", "Mailzero", "ciao", ts),
            new Mail(1, "andrea", "gaetano", "Mailuno", "ue gaetà", ts),
            new Mail(2, "marco", "gaetano", "Maildue", "hey come stai", ts)
        );
    }
   
    public final Mail getCurrentMail(){ return currentMail.get(); }
    
    public final void setCurrentMail(Mail m){ currentMail.set(m); }
    
    public SimpleObjectProperty<Mail> currentMailProperty() { return currentMail; }
    
    public ObservableList<Mail> getListMail(){ 
        return listMail;
    }
    
    //draft methods (to be put in the second tab
    
    public final Mail getCurrentDraft(){ return currentDraft.get();}
    
    public final void setCurrentDraft(Mail m) {currentDraft.set(m);}
    
    public SimpleObjectProperty<Mail> currentDraftProperty() { return currentDraft; }
    
    public ObservableList<Mail> getListDraft(){ 
        return listDraft;
    }
    
    public void addMail(String receiver, String title, String text){
        listMail.add(new Mail(idCounter, "marco", receiver, title, text, new Timestamp(System.currentTimeMillis())));
        idCounter++;
    }
    
    //invisible saves
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
    
    public void removeMail(){ //TODO remove specified element 
        listMail.remove(listMail.size()-1);
    }
    
    public Mail getMailByIndex(int i){
        return listMail.get(i);
    }
    
}
