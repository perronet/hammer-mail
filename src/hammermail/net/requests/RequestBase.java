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
package hammermail.net.requests;

import hammermail.core.Utils;
import static hammermail.core.Utils.isNullOrWhiteSpace;
import java.io.Serializable;

/**
 * A base class for requests made from client to server.
 * @author 00mar
 */
public abstract class RequestBase implements Serializable {
    protected String username;
    protected String password;

    /**
    * Set username and password for this request. 
    * @param username the username
    * @param password the password
    * @return true if the authentication is well-formed
    */
    public boolean SetAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
        return isAuthenticationWellFormed();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAuthenticationWellFormed(){
        return Utils.isAuthenticationWellFormed(username,password);
    }
    
}
