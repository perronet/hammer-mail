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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;

public class UIController implements Initializable {

    @FXML
    private Label user;
    
    @FXML
    private ListView<String> listmail;
    
    @FXML
    private TextArea mailcontent;
    
    @FXML
    private void handleCreate(ActionEvent event){
        m.addMail();
    }  

    @FXML
    private void handleDelete(ActionEvent event){
        m.removeMail();
    }  
    
    private Model m;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) { //Executes after @FXML fields are initialized, use this instead of constructor
        m = new Model();
        
        //BINDINGS
        mailcontent.textProperty().bind(m.currentMailProperty());
        
        //SETUP LISTENERS AND OTHER PARAMETERS
        listmail.setItems(m.getListMail()); //the ListView will automatically refresh the view to represent the items in the ObservableList

        listmail.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); //can only select one element at a time

        listmail.getSelectionModel().selectedIndexProperty().addListener((obsValue, oldValue, newValue) -> { //implementation of ChangeListener
            System.out.println("New mail selected from list");
            int newindex = (int)newValue;
            m.setCurrentMail(m.getMailByIndex(newindex) + "     Testo mail qui");
        });
        
//        listmail.getSelectionModel().getSelectedIndex();        
    }    
    
}
