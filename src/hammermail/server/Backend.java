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
import hammermail.net.requests.*;
import hammermail.net.responses.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hammermail.net.responses.ResponseError.ErrorType.*;
//import static hammermail.net.responses.ResponseError.ErrorType.INCORRECT_AUTHENTICATION;
//import static hammermail.net.responses.ResponseError.ErrorType.SENDING_INVALID_MAIL;
//import static hammermail.net.responses.ResponseError.ErrorType.SENDING_TO_UNEXISTING_USER;
//import static hammermail.net.responses.ResponseError.ErrorType.SIGNUP_USERNAME_TAKEN;

/**
 * This class implements the backend of the HammerMail server.
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
        logAction("Waiting for request...");

        try {
            Socket incoming = serverSocket.accept();
            logAction("Received request! Starting new task...");
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
        logAction("Stopping server...");

        //Stopping threads
        exec.shutdown();
        try {
            exec.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logAction(e.getMessage());
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

    public void logAction(String log) {
        System.out.println("°°°° BACKEND °°°° " + log);
    }
}

/**
 * Represents a single task of the HammerMail server
 *
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
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try {
            logAction("Task initialized! Receiving data...");

            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            Object obj = in.readObject();

            //#TODO MANAGE RESPONSES
            logAction("Server task received request");

            if (!(obj instanceof RequestBase)) {
                logAction("Error: received object of type: " + obj.getClass());
            } else {
                ResponseBase response = handleRequest((RequestBase) obj);
                out.writeObject(response);
            }
        } catch (IOException ex) {
            Logger.getLogger(Backend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Backend.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Given a valid request, generates a proper response
     *
     * @param request
     * @return
     */
    ResponseBase handleRequest(RequestBase request) {
        if (!request.IsAuthenticationWellFormed()) {
            return new ResponseError(ResponseError.ErrorType.INCORRECT_AUTHENTICATION);
        }

        if (request instanceof RequestSignUp) {
            return handleSignUp((RequestSignUp) request);
        } else if (request instanceof RequestSendMail) {
            return handleSendMail((RequestSendMail) request);
        } else if (request instanceof RequestGetMails) {
            return handleGetMails((RequestGetMails) request);
        } else if (request instanceof RequestDeleteMails) {
            return handleDeleteMails((RequestDeleteMails) request);
        } else {//should never get here, #TODO do something if it happens
            return null;
        }
    }

    ResponseBase handleSignUp(RequestSignUp request) {
        Database db = new Database(false);
        if (db.isUser(request.getUsername())) {
            return new ResponseError(SIGNUP_USERNAME_TAKEN);
        } else {
            db.addUser(request.getUsername(), request.getPassword());
        }

        return new ResponseSuccess();
    }

    ResponseBase handleSendMail(RequestSendMail request) {
        Database db = new Database(false);
        //note: checkPassword return false on not-existing user
        if (db.checkPassword(request.getUsername(), request.getPassword())) {
                System.out.println("mail adding: ");

            if (request.IsMailWellFormed()) {
                String rec = (request.getMail().getReceiver()).replaceAll("\\s+","");
                String [] receivers = rec.split(";");  
                
                if (receivers.length == 1){
                    if (!db.isUser(receivers[0]))
                        return new ResponseError(SENDING_TO_UNEXISTING_USER);
                    else {
                            int mailID =  db.addMail(request.getMail());              
                            return new ResponseMailSent(mailID);
                    }
                }
                
                rec = "";
                String refused = "";

                for(int i = 0; i < receivers.length; i++){
                    if (db.isUser(receivers[i]))
                        rec = rec + ";" + receivers[i];
                    else 
                        refused = refused + ";" + receivers[i];
                }
                request.getMail().setReceiver(rec.substring(1));
                int mailID =  db.addMail(request.getMail());              
                //TODO servers things?
                return new ResponseMailSent(mailID);
            } else 
                return new ResponseError(SENDING_INVALID_MAIL);
            
        } else {
            return new ResponseError(INCORRECT_AUTHENTICATION);
        }
    }

    ResponseBase handleGetMails(RequestGetMails request) {
        Database db = new Database(false);
        
        if (db.checkPassword(request.getUsername(), request.getPassword())) {
            return new ResponseMails(db.getReceivedMails(request.getUsername()),
                                                    db.getSentMails(request.getUsername()));
        } else {
            return new ResponseError(INCORRECT_AUTHENTICATION);
        }
    }

    private ResponseBase handleDeleteMails(RequestDeleteMails requestDeleteMails) {
        Database db = new Database(false);
        for(Integer mailID : requestDeleteMails.getMailsIDsToDelete()){
            db.removeMail(mailID, requestDeleteMails.getUsername());
        }
        return new ResponseSuccess();
    }

    public void logAction(String log) {
        System.out.println("**** SERVER **** " + log);
    }
}
