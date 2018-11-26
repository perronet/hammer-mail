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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class UILoginController implements Initializable {

    private Stage s;
        
    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML 
    private Text loginfailure;

    @FXML
    private void handleLogin(ActionEvent event){
        //username, password to the server
        boolean logged = true;
        //use of bool: waiting for the DB check credential implementations
        try {
            if (logged){
                s.close(); //close myself

                //spawn a new stage
                Parent root = FXMLLoader.load(getClass().getResource("UI.fxml"));
                Stage newstage = new Stage();
                newstage.setTitle("HammerMail - Home");
                newstage.setScene(new Scene(root));
                newstage.show();
            }
            else {
                loginfailure.setFill(Color.rgb(255,0,0));
                loginfailure.setText("Wrong credentials");
            }
        } catch (Exception e){
            //TODO
        }
    }  

    @FXML
    private void handleSignup(ActionEvent event){
        //contact server, that will call Database.dbAddUser(username, psw)
    }

    //"Constructor"
    public void init(Stage stage){ 
        this.s = stage;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

}
