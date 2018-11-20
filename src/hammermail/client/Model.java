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
import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
    
    //private ObservableList<Mail> listMail = FXCollections.observableArrayList();
    private final ObservableList<String> listMail;
    
    private final SimpleStringProperty currentMail = new SimpleStringProperty(); //TODO change everything to mail objects (use SimpleObjectProperty) 
                                                                           //then find a way to print them properly
    private ArrayList<User> listUser;

    private int idCounter = 0;
    
    public Model() { //TODO load mails and user from file 
        setCurrentMail("Select a mail from the list");
        listMail = FXCollections.observableArrayList("mail a", "mail b", "mail c");
        
        //Uncomment to test mail objects instead of strings  
        //listMail.setAll(
        //    new Mail(0, "Mailzero", "marco", "andrea", "ciao", ""),
        //    new Mail(1, "Mailuno", "andrea", "gaetano", "ue gaetà", ""),
        //    new Mail(2, "Maildue", "marco", "gaetano", "hey come stai", "")
        //);
        
        //Not working        
        //listUser.add(new User("marco", "qwerty", "marco.maida@hammermail.com")); 
        //listUser.add(new User("andrea", "12345", "andrea.rondinelli@hammermail.com"));
        //listUser.add(new User("gaetano", "12345", "gaetano.97@hammermail.com"));
    }
   
    public final String getCurrentMail(){ return currentMail.get(); }
    
    public final void setCurrentMail(String s){ currentMail.set(s); }
    
    public SimpleStringProperty currentMailProperty() { return currentMail; }
    
    public ObservableList<String> getListMail(){ 
        return listMail;
    }
    
    public void addMail(){
        listMail.add("newmail " + idCounter);
        idCounter++;
    }
    
    //TODO Tostring?
    
    public void removeMail(){ //TODO remove specified element 
        listMail.remove(listMail.size()-1);
    }
    
    public String getMailByIndex(int i){
        return listMail.get(i);
    }
    
}
