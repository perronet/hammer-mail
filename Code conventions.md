# Conventions

######Always use the first bracket inline

Like so:

```
method(params){
	
}
```

Don't use the "unix convention":
```
method(params)
{
	
}
```

######Variable names

You can use the usual java convention for variables (first word is lowercase and the next ones are uppercase)
```
private int idCounter;
```

The same is true for every method
```
public Boolean isFieldEmpty();
```

The only exception are the @FXML variables in the UiController binded to the FXML document, these are lowercase only 
```
private Label idcounter;
private TextField userinput;
```

######Let netbeans generate getter/setter for regular attributes

```
public void setParam(param){ 
	this.param = param; 
}
```

######Define Properties getter/setter methods in one line only

Netbeans can't generate Properties getter/setter, define them manually like this:

```
private final SimpleStringProperty test = new SimpleStringProperty();

public String getTest(){ return test.get(); }
public void setTest(String s){ test.set(s); }
```

Other than getter/setter methods always add the method that returns the property itself

```
public SimpleStringProperty testProperty(){ return test; }
```

######Always use properties, not regular attributes

Of course there are exceptions, things that will never change during runtime such as usernames can be regular attributes

######Use lambda expressions instead of anonymous classes

Use it when you implement interfaces that require a single method such as ChangeListener and InvalidationListener

Anonymous class version:
```
testProperty().addListener(new ChangeListener() {
    @Override
    public void changed(ObservableValue o, Object oldVal, Object newVal) { //pretty ugly
    	/* code */
    }
});			
```

Lambda version:
```
testProperty().addListener((ObservableValue o, Object oldVal, Object newVal) -> { 
	/* code */
});				
```

Not working? Use the proper cast.
```
testProperty().addListener((ChangeListener)(ObservableValue o, Object oldVal, Object newVal) -> { 
	/* code */
});				
```

######ChangeListener vs InvalidationListener interfaces

Use a ChangeListener if you need to know the new (or old) value in the listener, otherwise use an InvalidationListener

######Use Ctrl + / to comment/uncomment current line or selected lines

Prefer this over multiline comments so commenting/uncommenting is faster for everybody, this is especially useful for xml
If you don't like the shortcut just change it in Tools->Options->Keymaps

# Netbeans tips

######Use right click -> show Javadoc to see documentations of everything in the code

Not working?
Tools->Java platform->Javadoc and add the following urls:
```
https://docs.oracle.com/javase/8/javafx/api/
https://docs.oracle.com/javase/8/docs/api/
```

Still not working?
Tools->Analyze javadoc then select all and select fix selected

######Make sure code completion is working properly

If it doesn't work go to Tools->Options->Editor->Code completion and select Java as language

######Use alt+scrollwheel to zoom in/out, the text very tiny by default

I know, this is pretty ugly, you can configure your own keys in Tools->Options->Keymaps

######Want dark theme?
Tools->Plugins, search for "Dark look and feel themes" in available plugins and install it

You can change theme later in Tools->Options->Appearance->Look and feel
