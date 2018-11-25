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
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            logAction("Task initialized! Receiving data...");

            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
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
        } else if (request instanceof RequestNewMail) {
            return handleNewMail((RequestNewMail) request);
        } else if (request instanceof RequestGetMails) {
            return handleGetMails((RequestGetMails) request);
        } else {//should never get here, #TODO do something if it happens
            return null;
        }
    }

    ResponseBase handleSignUp(RequestSignUp request) {
        //#TODO DB CALLS
        return new ResponseSuccess();
    }

    ResponseBase handleNewMail(RequestNewMail request) {
        //#TODO DB CALLS
        return new ResponseSuccess();
    }

    ResponseBase handleGetMails(RequestGetMails request) {
        //#TODO DB CALLS
        return new ResponseMails(null);
    }
    
    public void logAction(String log) {
        System.out.println("**** SERVER **** " + log);
    }
}
