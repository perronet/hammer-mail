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
package hammermail.core;

import java.io.Serializable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import java.sql.Timestamp;

public class Mail implements Serializable {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleStringProperty sender = new SimpleStringProperty();
    private SimpleStringProperty receiver = new SimpleStringProperty();
    private SimpleStringProperty title = new SimpleStringProperty();
    private SimpleStringProperty text = new SimpleStringProperty();
    private SimpleObjectProperty <Timestamp> date = new SimpleObjectProperty<>();

    public Mail(Integer id, String sender, String receiver, String title, String text, Timestamp date){
        setId(id);
        setSender(sender);
        setReceiver(receiver);
        setTitle(title);
        setText(text);
        setDate(date);
    }
    
    @Override
    public String toString(){
        return getId() + " " + getSender() + " " + getTitle() + " " + getDate();
    }
    
    //Every getter, setter and property getter has the following format
    //getX();
    //setX();
    //xProperty();
    public final Integer getId(){return id.get();}
 
    public final void setId(Integer value){id.set(value);}
 
    public SimpleIntegerProperty idProperty() {return id;}
    
    
    public final String getSender(){return sender.get();}
 
    public final void setSender(String value){sender.set(value);}
 
    public SimpleStringProperty senderProperty() {return sender;}
    
    
    public final String getReceiver(){return receiver.get();}
 
    public final void setReceiver(String value){receiver.set(value);}
 
    public SimpleStringProperty receiverProperty() {return receiver;}  
    
    
    public final String getTitle(){return title.get();}
 
    public final void setTitle(String value){title.set(value);}
 
    public SimpleStringProperty titleProperty() {return title;}


    public final String getText(){return text.get();}
 
    public final void setText(String value){text.set(value);}
 
    public SimpleStringProperty textProperty() {return text;}


    public final Timestamp getDate(){return date.get();}
 
    public final void setDate(Timestamp value){date.set(value);}
 
    public SimpleObjectProperty<Timestamp> dateProperty() {return date;}    

}
