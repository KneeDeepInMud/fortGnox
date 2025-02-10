package org.mockenhaupt.fortgnox.swing;

import javax.swing.JToggleButton;
import java.awt.Insets;

public class FgToggleButton extends JToggleButton
{
    public FgToggleButton(String text)
    {
        super(text);
        init();
    }

    public FgToggleButton()
    {
        init();
    }


    private void init()
    {
        Insets m = getMargin();
        setMargin(new Insets(m.top, 4, m.bottom, 4));
    }
}
