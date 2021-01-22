package org.mockenhaupt.fortgnox.swing;

import org.mockenhaupt.fortgnox.FgPreferences;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class JTextFieldChangeAction extends JFormattedTextField
{
    private Runnable changeAction;


    public JTextFieldChangeAction (Runnable changeAction)
    {
        this(null, changeAction);
    }
    public JTextFieldChangeAction (String text, Runnable changeAction)
    {
        super(text);
        this.changeAction = changeAction;


        getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate (DocumentEvent e)
            {
            }

            @Override
            public void removeUpdate (DocumentEvent e)
            {
            }

            @Override
            public void changedUpdate (DocumentEvent e)
            {
                handleContentChanged();
            }
        });

        addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost (FocusEvent e)
            {
                handleContentChanged();
            }
        });
        addActionListener(actionEvent ->
        {
            handleContentChanged();

        });
    }


    private void handleContentChanged ()
    {
        if (changeAction != null)
        {
            changeAction.run();
        }
    }


}
