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
package hammermail.server;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 */
public class UIController {
    
    @FXML
    private TextArea logText;
    
    private UIModel model;
    
    public void initModel(UIModel model) {
        // ensure model is only set once:
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        
        this.model = model;
        model.logProperty().addListener((val, oldVal, newVal) -> {
            logText.textProperty().set(newVal);
            logText.positionCaret(logText.getLength());
        });
    }
}
