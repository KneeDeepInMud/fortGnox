package org.mockenhaupt.fortgnox.swing;

import org.mockenhaupt.fortgnox.FgPreferences;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class JTextFieldPersistent extends JFormattedTextField
{
    private Runnable action;
    String preference;
    public JTextFieldPersistent (String preference)
    {
        this(preference, null, null);
    }

    public JTextFieldPersistent (String preference, String text)
    {
        this(preference, text, null);
    }

    public JTextFieldPersistent (String preference, String text, Runnable action)
    {
        super(text);
        this.action = action;
        this.preference = preference;

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
        FgPreferences.get().putPreference(preference, getText());
        System.out.println("true = " + getText());
        if (action != null)
        {
            action.run();
        }
    }

    public static void main (String[] a)
    {
        JFrame f = new JFrame();
        f.add(new JTextFieldPersistent("XXX", "XXXXXXXXXXXXXXXXXXXXXXX"));
        f.pack();
        f.setVisible(true);
    }
}
