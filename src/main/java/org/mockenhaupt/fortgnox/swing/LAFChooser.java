package org.mockenhaupt.fortgnox.swing;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.mockenhaupt.fortgnox.FgPreferences;
import org.mockenhaupt.fortgnox.MainFrame;
import org.yaml.snakeyaml.util.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockenhaupt.fortgnox.FgPreferences.PREF_LOOK_AND_FEEL;

public class LAFChooser
{
    private UIManager.LookAndFeelInfo[] lafs;
    private int currLafIX = 0;

    static private  LAFChooser lafChooser;

    private LAFChooser ()
    {
        init();
    }


    public static boolean setPreferenceLaf (Component c)
    {
        return get().set(FgPreferences.get().get(PREF_LOOK_AND_FEEL), c);
    }

    public static LAFChooser get()
    {
        if (lafChooser == null)
        {
            lafChooser = new LAFChooser();
        }
        return lafChooser;
    }

    public boolean set (String laf, Component c)
    {
        UIManager.LookAndFeelInfo o = Arrays.stream(lafs)
                .filter(lookAndFeelInfo -> lookAndFeelInfo.getName().toLowerCase().contains(laf.toLowerCase()))
                .findFirst()
                .orElse(null);
        if (o != null) return set(o, c);
        return false;

    }

    public UIManager.LookAndFeelInfo getCurrentLAF ()
    {
        String current = UIManager.getLookAndFeel().getClass().getName();

        return Arrays.stream(lafs)
                .filter(lookAndFeelInfo ->
                {
                    boolean ret = current.equals(lookAndFeelInfo.getClassName());
                    return ret;
                })
                .findFirst()
                .orElse(null);

    }

    public boolean set (UIManager.LookAndFeelInfo laf, Component c)
    {
        if (laf == null || c == null)
        {
            return false;
        }
        try
        {
            UIManager.setLookAndFeel(laf.getClassName());
            SwingUtilities.updateComponentTreeUI(c);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public UIManager.LookAndFeelInfo[] getLafs ()
    {
        return lafs;
    }

    private void init() {

        ArrayList<UIManager.LookAndFeelInfo> ail = new ArrayList();
        ail.addAll(ArrayUtils.toUnmodifiableList(UIManager.getInstalledLookAndFeels()));

        Class clazzes[] = {FlatDarkLaf.class, FlatDarculaLaf.class, FlatLightLaf.class, FlatIntelliJLaf.class};
        for (Class clazz : clazzes) {
            Method method = null;
            try {
                method = clazz.getMethod("setup");
                if (method == null) continue;

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            try {
                Object o = method.invoke(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            UIManager.LookAndFeelInfo lafi = new UIManager.LookAndFeelInfo(clazz.getSimpleName(), clazz.getName());
            ail.add(lafi);
        }
        lafs = ail.toArray(new UIManager.LookAndFeelInfo[]{});
    }


    private void buttonLAFActionPerformed (java.awt.event.ActionEvent evt)
    {
        try
        {
            currLafIX++;
            if (currLafIX >= lafs.length)
            {
                currLafIX = 0;
            }
            UIManager.setLookAndFeel(lafs[currLafIX].getClassName());
//            SwingUtilities.updateComponentTreeUI(this);
//            buttonLAF.setText(
//                    "Change Look and Feel, current: " + lafs[currLafIX].getName());

        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex)
        {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
    }


//        buttonLAF.setIcon(new ImageIcon(getClass().getResource("/org/mockenhaupt/fortgnox/1346515642_gnome-settings-theme.png"))); // NOI18N
//        buttonLAF.setText("Change theme");
//        buttonLAF.setFocusable(false);
//        buttonLAF.setHorizontalTextPosition(SwingConstants.RIGHT);
//        buttonLAF.addActionListener(new java.awt.event.ActionListener()
//    {
//        public void actionPerformed(java.awt.event.ActionEvent evt)
//        {
////                buttonLAFActionPerformed(evt);
//        }
//    });

}
