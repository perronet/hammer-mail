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
package hammermail.net.responses;

/**
 * Sent by the server when an error occurs
 * @author 00mar
 */
public class ResponseError extends ResponseBase {

    public enum ErrorType {
        //Sign up errors:
        SIGNUP_USERNAME_TAKEN, //Username already taken
        SIGNUP_AUTHENTICATION_NOT_WELL_FORMED, //We don't like authentication (empty user or pass, non accepted characters)
        
        //Sending mails errors:
        SENDING_INVALID_MAIL, //Missing from, to
        SENDING_TO_UNEXISTING_USER, //to is not valid
        
        //General errors:
        INCORRECT_AUTHENTICATION, //Wrong username or password
        INTERNAL_ERROR, //Errors the client is not responsible for
    }
    
    ErrorType errorType;

    public ResponseError(ErrorType errorType) {
        this.errorType = errorType;
    }

//    we will need this fro specify what type of error occurs
//    public ErrorType getErrorType(){
//        return this.errorType;
//    }
}
