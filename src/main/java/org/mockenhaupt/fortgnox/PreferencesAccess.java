package org.mockenhaupt.fortgnox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PreferencesAccess
{
    private final PropertyChangeSupport propertyChangeSupport;

    // this is a hack for backwards compability, package name could be used


    final private Preferences preferences;

    private PreferencesAccess (String node)
    {
        propertyChangeSupport = new PropertyChangeSupport(this);
        preferences = Preferences.userRoot().node(node);
    }

    private static PreferencesAccess INSTANCE;
    private static String NODE;

    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    private PropertyChangeEvent getEvent (String name, Object oldVal, Object newVal)
    {
        return new PropertyChangeEvent(INSTANCE, name, oldVal, newVal);
    }
    private void fireEvent (String name, Object oldVal, Object newVal)
    {
        propertyChangeSupport.firePropertyChange(getEvent(name, oldVal, newVal));
    }

    // for test purpose only
    static PreferencesAccess getInstance (String node)
    {
        if (INSTANCE == null)
        {
            NODE = node;
            INSTANCE = new PreferencesAccess(node);
        }
        if (!NODE.equals(node))
        {
            throw new IllegalArgumentException("Invalid node " + node + " expected " + NODE);
        }
        return INSTANCE;
    }

    public  String get (String name)
    {
        return getPreference(name, "");
    }
    public  Integer getInt (String name)
    {
        return getPreference(name, Integer.MIN_VALUE);
    }
    public Boolean getBoolean (String name)
    {
        return getPreference(name, Boolean.FALSE);
    }



    public  <T> T get (String name, T defaultValue)
    {
        return getPreference(name, defaultValue);
    }

    public  <T> T getPreference (String name, T defaultValue)
    {
        if (defaultValue instanceof String)
        {
            String got =  INSTANCE.preferences.get(name, (String)defaultValue);
            if (got.equals(defaultValue))
            {
                 putPreference(name, defaultValue);
            }
            return (T) got;
        }
        else if (defaultValue instanceof Integer)
        {
            Integer got =  INSTANCE.preferences.getInt(name, (Integer)defaultValue);
            if (got.equals(defaultValue))
            {
                putPreference(name, defaultValue);
            }
            return (T) Integer.valueOf(INSTANCE.preferences.getInt(name, (Integer) defaultValue));
        }
        else if (defaultValue instanceof Boolean)
        {
            Boolean got = INSTANCE.preferences.getBoolean(name, (Boolean) defaultValue);
            if (got.equals(defaultValue))
            {
                putPreference(name, defaultValue);
            }
            return (T) Boolean.valueOf(INSTANCE.preferences.getBoolean(name, (Boolean) defaultValue));
        }
        else if (defaultValue instanceof Float)
        {
            Float got = INSTANCE.preferences.getFloat(name, (Float) defaultValue);
            if (got.equals(defaultValue))
            {
                putPreference(name, defaultValue);
            }
            return (T) Float.valueOf(INSTANCE.preferences.getFloat(name, (Float) defaultValue));
        }

        throw new IllegalArgumentException("unsupported preference type " + defaultValue.getClass());
    }

    public <T> PreferencesAccess put (String name, T value) {
        return putPreference(name, value);
    }
    public <T> PreferencesAccess putPreference (String name, T value)
    {

        if (value instanceof String)
        {
            String invalid = null;
            String oldVal = INSTANCE.preferences.get(name, invalid);

            String svalue = ((String)value).trim();
            INSTANCE.preferences.put(name, svalue);
            if (oldVal == null || !oldVal.equals(svalue))
            {
                fireEvent(name, null, svalue);
            }
        }
        else if (value instanceof Integer)
        {
            Integer invalid = Integer.MIN_VALUE;
            Integer oldVal = INSTANCE.preferences.getInt(name, invalid);

            INSTANCE.preferences.putInt(name, (Integer) value);
            if (!oldVal.equals(value))
            {
                fireEvent(name, null, value);
            }
        }
        else if (value instanceof Float)
        {
            Float invalid = Float.MIN_VALUE;
            Float oldVal = INSTANCE.preferences.getFloat(name, invalid);

            INSTANCE.preferences.putFloat(name, (Float) value);
            if (!oldVal.equals(value))
            {
                fireEvent(name, null, value);
            }
        }
        else if (value instanceof Boolean)
        {
            Boolean invalid = false;
            Boolean oldVal = INSTANCE.preferences.getBoolean(name, invalid);

            INSTANCE.preferences.putBoolean(name, (Boolean) value);
            if (!oldVal.equals(value))
            {
                fireEvent(name, null, value);
            }
        }
        else
        {
            throw new IllegalArgumentException("unsupported preference type " + value.getClass());
        }
        return INSTANCE;
    }

    public void clear ()
    {
        try
        {
            INSTANCE.preferences.clear();
        }
        catch (BackingStoreException ex)
        {
            // TODO: handle this
        }
    }
}
