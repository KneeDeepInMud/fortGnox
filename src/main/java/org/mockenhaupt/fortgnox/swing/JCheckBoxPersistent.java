package org.mockenhaupt.fortgnox.swing;

import org.mockenhaupt.fortgnox.FgPreferences;

import javax.swing.JCheckBox;

public class JCheckBoxPersistent extends JCheckBox
{
    public JCheckBoxPersistent (String preference)
    {
        this(preference, null, null);
    }
    public JCheckBoxPersistent (String preference, String text)
    {
        this(preference, text, null);
    }

    public JCheckBoxPersistent (String preference, String text, Runnable onAction)
    {
        super(text);
        this.setSelected(FgPreferences.get().getBoolean(preference));
        addActionListener(actionEvent -> {
            FgPreferences.get().putPreference(preference, JCheckBoxPersistent.this.isSelected());
            if (onAction != null)
            {
                onAction.run();
            }
        });
    }
}
