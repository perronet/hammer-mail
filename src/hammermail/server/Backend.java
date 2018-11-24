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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hammermail.server;

import hammermail.core.Globals;
import hammermail.net.responses.ResponseBase;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the backend of the Hammermail server.
 *
 * @author 00mar
 */
public class Backend {

    ExecutorService exec;
    ServerSocket serverSocket;

    @SuppressWarnings("empty-statement")
    public void startServer() {
        try {
            System.out.println("Creating sockets...");
            serverSocket = new ServerSocket(Globals.HAMMERMAIL_SERVER_PORT_NUMBER);
            System.out.println("Creating Thread pool...");
            exec = Executors.newFixedThreadPool(Globals.HAMMERMAIL_SERVER_NUM_THREAD);
            System.out.println("Sockets and Threads created. Server starting...");
            
            while (serverLoop());//#TODO fill this while with proper logging
        } catch (IOException ex) {
            Logger.getLogger(Backend.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            stopServer();
        }
    }

    /**
     * This method will loop until the server is working
     */
    boolean serverLoop() {
        try (Socket incoming = serverSocket.accept()) {
            handleNewRequest(incoming);
        } catch (IOException ex) {
            Logger.getLogger(Backend.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }
    
    void handleNewRequest(Socket clientSocket) {
        Task task = new Task(clientSocket);
        exec.execute(task);
    }

    /**
     * Stops threads and socket. Use it to stop the server
     */
    void stopServer() {
        //Stopping threads
        exec.shutdown();
        try {
            exec.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        //Closing socket
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(Backend.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

/**
 * Represents a single task of the HammerMail server
 * @author 00mar
 */
class Task implements Runnable {

    Socket clientSocket;

    public Task(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        if (clientSocket != null) {
            handleClient(clientSocket);
        }
    }

    void handleClient(Socket clientSocket) {
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            Object obj = in.readObject();
            
            //#TODO MANAGE RESPONSES
            System.out.println("Server received request");
            ResponseBase response = new ResponseBase();
            response.response = "Received object of type: " + obj.getClass(); 
        } catch (IOException ex) {
            Logger.getLogger(Backend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Backend.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
