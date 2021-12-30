package org.mockenhaupt.fortgnox.misc;

import org.mockenhaupt.fortgnox.FgPreferences;

import javax.swing.JComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.mockenhaupt.fortgnox.FgPreferences.PREF_HISTORY_SIZE;

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
        deduplicate(backward);
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

    private <E> void deduplicate (Collection<E> aCollection)
    {
        reverse(aCollection);
        LinkedHashSet<E> dupSet = new LinkedHashSet<>(aCollection);
        aCollection.clear();
        aCollection.addAll(dupSet);
        reverse(aCollection);
    }

    private <E> void reverse (Collection<E> aCollection)
    {
        List<E> copyList = aCollection.stream().collect(Collectors.toList());
        Collections.reverse(copyList);
        aCollection.clear();
        aCollection.addAll(copyList);
    }

    private void handleSensitivity ()
    {
        backwardComp.setEnabled(false);
        backwardComp.setEnabled(!backward.empty());
        forwardComp.setEnabled(!forward.empty());
        forwardComp.setToolTipText(getToolTip(forward));
        backwardComp.setToolTipText(getToolTip(backward));
    }

    private String getToolTip (Stack<String> collection)
    {
        if (collection == null || collection.isEmpty())
        {
            return "no entries";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        AtomicReference<Integer> i = new AtomicReference<>(1);
        List<String > copyList = collection.stream().collect(Collectors.toList());
        Collections.reverse(copyList);
        copyList.stream().forEach(s -> {
            File f = new File(s);
            sb.append(String.format("%3d. ", i.getAndSet(i.get() + 1)));
            sb.append(f.getName() + "<br>");
        });
        sb.append("</html>");
        return sb.toString();
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
                    deduplicate(forward);
                }
            }
            else
            {
                retVal = forward.pop();
                if (current instanceof String)
                {
                    backward.push((String)current);
                    deduplicate(backward);
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
