package org.mockenhaupt.fortgnox.swing;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

public class FgDirChooser extends JFileChooser
{
    public FgDirChooser ()
    {
        super();
    }

    public static void main (String[] a)
    {
        FgDirChooser dirChooser = new FgDirChooser();
        dirChooser.setAcceptAllFileFilterUsed(false);
        dirChooser.setMultiSelectionEnabled(true);
        dirChooser.setFileFilter(new FileFilter()
        {
            @Override
            public boolean accept (File f)
            {
                return f.isDirectory();
            }

            @Override
            public String getDescription ()
            {
                return "Directory";
            }
        });
        dirChooser.showOpenDialog(null);
        System.out.println("dirChooser.getSelectedFiles() = " + dirChooser.getSelectedFiles());
    }

    @Override
    protected JDialog createDialog (Component parent) throws HeadlessException
    {
        JDialog d =  super.createDialog(parent);
        d.getContentPane().add(new JLabel("FPOOOOO"), BorderLayout.EAST);
        return d;
    }
}
