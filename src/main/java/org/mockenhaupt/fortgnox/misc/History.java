package org.mockenhaupt.fortgnox.misc;

import org.mockenhaupt.fortgnox.FgPreferences;

import javax.swing.JComponent;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EmptyStackException;
import java.util.Stack;

import static org.mockenhaupt.fortgnox.FgPreferences.PREF_HISTORY_SIZE;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_TEXTAREA_FONT_SIZE;

public class History implements PropertyChangeListener
{
    final private Stack<String> forward;
    final private Stack<String> backward;

    private static History instance;

    private int MAX_SIZE = 30;
    private JComponent backwardComp;
    private JComponent forwardComp;

    public History (JComponent backwardComp, JComponent forwardComp)
    {
        this.forward = new Stack<>();
        this.backward = new Stack<>();

        MAX_SIZE = FgPreferences.get().get(PREF_HISTORY_SIZE, 20);

        this.backwardComp = backwardComp;
        this.forwardComp = forwardComp;
        FgPreferences.get().addPropertyChangeListener(this);

        handleSensitivity();
    }


    public boolean isHistoryEnabled ()
    {
        return MAX_SIZE > 0;
    }

    public void clear ()
    {
        this.backward.clear();
        this.forward.clear();
        handleSensitivity();
    }

    public void add (String fileName)
    {
        if (!isHistoryEnabled())
        {
            return;
        }
        if (!backward.isEmpty() && fileName != null)
        {
            String top = backward.peek();
            if (fileName.equals(top))
            {
                return;
            }
        }
        this.backward.push(fileName);
        handleHistoryChange();
    }

    private void handleHistoryChange ()
    {
        limitHistorySize();
        handleSensitivity();
    }

    private void limitHistorySize ()
    {
        while (backward.size() > MAX_SIZE)
        {
            backward.removeElementAt(0);
        }
    }

    private void handleSensitivity ()
    {
        backwardComp.setEnabled(false);
        backwardComp.setEnabled(!backward.empty());
        forwardComp.setEnabled(!forward.empty());
        if (!forward.isEmpty())
        {
            forwardComp.setToolTipText(forward.peek());
        }
        if (!backward.isEmpty())
        {
            backwardComp.setToolTipText(backward.peek());
        }
    }


    public String popBack (Object current)
    {
        return getBackForward(true, current);
    }
    public String popForward (Object current)
    {
        return getBackForward(false, current);
    }
    private String getBackForward (boolean isBack, Object current)
    {
        String retVal = null;

        try
        {
            if (isBack)
            {
                retVal = backward.pop();
                if (current instanceof String)
                {
                    forward.push((String)current);
                }
            }
            else
            {
                retVal = forward.pop();
                if (current instanceof String)
                {
                    backward.push((String)current);
                }
            }
            handleSensitivity();
        }
        catch (EmptyStackException ex)
        {
        }
        return retVal;
    }

    @Override
    public void propertyChange (PropertyChangeEvent propertyChangeEvent)
    {
        switch (propertyChangeEvent.getPropertyName())
        {
            case PREF_HISTORY_SIZE:
                this.MAX_SIZE = (Integer)propertyChangeEvent.getNewValue();
                handleHistoryChange();
                break;
        }
    }

}
