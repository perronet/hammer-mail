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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HammerMail extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
//        Uncomment to start without login
//        Parent root = FXMLLoader.load(getClass().getResource("UI.fxml"));
	Parent root = FXMLLoader.load(getClass().getResource("UIlogin.fxml"));
       
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("HammerMail");
        stage.show();
    }

    
    public static void main(String[] args) {
        launch(args);
    }
    
}
