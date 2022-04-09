package org.mockenhaupt.fortgnox.swing;

import org.mockenhaupt.fortgnox.FgPreferences;

import javax.swing.JCheckBox;

public class JCheckBoxPersistent extends JCheckBox
{
    public JCheckBoxPersistent (String preference, boolean defaultValue)
    {
        this(preference, null, null, defaultValue);
    }

    public JCheckBoxPersistent (String preference)
    {
        this(preference, null, null, false);
    }
    public JCheckBoxPersistent (String preference, String text)
    {
        this(preference, text, null, false);
    }

    public JCheckBoxPersistent (String preference, String text, Runnable onAction)
    {
        this(preference, text, onAction, false);
    }
    public JCheckBoxPersistent (String preference, String text, Runnable onAction, boolean defaultValue)
    {
        super(text);
        this.setSelected(FgPreferences.get().get(preference, (Boolean)defaultValue));
        addActionListener(actionEvent -> {
            FgPreferences.get().putPreference(preference, JCheckBoxPersistent.this.isSelected());
            if (onAction != null)
            {
                onAction.run();
            }
        });
    }
}
