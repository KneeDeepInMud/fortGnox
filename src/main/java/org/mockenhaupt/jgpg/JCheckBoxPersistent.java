package org.mockenhaupt.jgpg;

import javax.swing.JCheckBox;

public class JCheckBoxPersistent extends JCheckBox
{
    public JCheckBoxPersistent (String preference, String text)
    {
        super(text);
        this.setSelected(JgpgPreferences.get().getBoolean(preference));
        addActionListener(actionEvent -> {
            JgpgPreferences.get().putPreference(preference, JCheckBoxPersistent.this.isSelected());
        });
    }
}
