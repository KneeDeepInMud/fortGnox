package org.mockenhaupt.jgpg;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Component;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LAFChooser
{
    private UIManager.LookAndFeelInfo[] lafs;
    private int currLafIX = 0;

    static private  LAFChooser lafChooser;

    private LAFChooser ()
    {
        init();
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
                .filter(lookAndFeelInfo -> lookAndFeelInfo.getClassName().toLowerCase().contains(laf.toLowerCase()))
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
        try
        {
            UIManager.setLookAndFeel(laf.getClassName());
            SwingUtilities.updateComponentTreeUI(c);
            return true;
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public UIManager.LookAndFeelInfo[] getLafs ()
    {
        return lafs;
    }

    private void init ()
    {
        lafs = UIManager.getInstalledLookAndFeels();
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


//        buttonLAF.setIcon(new ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/1346515642_gnome-settings-theme.png"))); // NOI18N
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
