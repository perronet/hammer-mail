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

public class User implements Serializable { //Users will be static, no need to use properties (may change this later on)
    
    private String username;
    private String password; 

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
 
    @Override
    public String toString(){
        return username;
    }
    
    public String getUserFileFolder(){
        return this.username + "mails" + "/" + this.username + ".json";
    }
    
    public String getUsername() {
        return username;
    }

    public void setUserame(String name) {
        this.username = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}