/*
 * Copyright (C) 2018 00mar
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
 * Stallman si mangia i piedi
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hammermail.server;

import hammermail.core.Mail;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author 00mar
 */
public class UI extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("UI.fxml"))));

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() - 400);
        primaryStage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() - 300);
        primaryStage.setWidth(300);
        primaryStage.setHeight(200);

        primaryStage.show();

        startServer();
        
        for(int i = 0; i < 1; i++)//Up to 200 working on my machine
            testServer();
    }

    void startServer() {
        Thread t = new Thread(() -> {
            Backend backend = new Backend();
            backend.startServer();
        });
        t.start();
    }

    void testServer() {
        System.out.println("Testing Server!");
        Thread t = new Thread(() -> {
            try {
                System.out.println("Testing in 3 seconds!");
                Thread.sleep(1000);
                System.out.println("Testing in 2 seconds!");
                Thread.sleep(1000);
                System.out.println("Testing in 1 second!");
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                DummyClient c = new DummyClient();
            } catch (InterruptedException ex) {
                Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        t.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
