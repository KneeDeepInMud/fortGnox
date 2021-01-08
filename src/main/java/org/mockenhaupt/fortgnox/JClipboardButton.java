package org.mockenhaupt.fortgnox;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JClipboardButton extends JButton
{
    private final String clipboardText;

    public JClipboardButton (int label, String clipboardText)
    {
        super(String.format("%02d", label));
        this.clipboardText = clipboardText;

        setToolTipText("Copy password with number " + label + " to clipboard");

        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent actionEvent)
            {
                MainFrame.toClipboard(clipboardText, "passwurd number " + JClipboardButton.this.getText(), true);
            }
        });
    }


}
