package org.mockenhaupt.fortgnox.swing;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javax.swing.BoxLayout.Y_AXIS;

public class FgDirChooser extends JFileChooser
{
    JDialog dialog;

    final JPanel list = new JPanel();
    final SortedSet<String> selectedDirectories = new TreeSet<>();
    private Response response = Response.CANCEL;

    public enum Response {
        APPLY,
        CANCEL
    }

    public FgDirChooser ()
    {
        super();
    }


    class DirTextField extends JButton
    {
        public DirTextField (String text, Consumer<String> onClick)
        {
            super(text);
            setHorizontalAlignment(SwingConstants.LEFT);
            setToolTipText("Click to remove entry");
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            addActionListener(e -> onClick.accept(this.getText()));
        }
    }

    public static void main (String[] a)
    {
        FgDirChooser dirChooser = new FgDirChooser();
        dirChooser.showDirectoryDialog(null, null);
        System.out.println("XXX dirChooser.getSelectedFiles() = " + dirChooser.getSelectedDirectories());
    }

    public Response showDirectoryDialog (Component parent, List<String> directoryList)
    {
        initialize();
        if (directoryList != null)
        {
            selectedDirectories.addAll(directoryList);
            handleSelectedFilesChanged();
        }
        showDialog(parent, "");
        return response;
    }

    public Set<String> getSelectedDirectories ()
    {
        return selectedDirectories;
    }

    public boolean disableTF(Container c) {
        Component[] cmps = c.getComponents();
        for (Component cmp : cmps) {
            if (cmp instanceof JTextField) {
                ((JTextField)cmp).setEnabled(false);
                return true;
            }
            if (cmp instanceof Container) {
                if(disableTF((Container) cmp)) return true;
            }
        }
        return false;
    }

    private void initialize ()
    {
//        disableTF(this);
        setAcceptAllFileFilterUsed(false);
        setFileHidingEnabled(false);
        setMultiSelectionEnabled(true);
        setControlButtonsAreShown(false);
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        setFileFilter(new FileFilter()
        {
            @Override
            public boolean accept (File f)
            {
                return true;
            }

            @Override
            public String getDescription ()
            {
                return "Directory";
            }
        });
    }



    @Override
    protected JDialog createDialog (Component parent) throws HeadlessException
    {
        dialog = super.createDialog(parent);

        JPanel dirPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPaneList = new JScrollPane(list);
        BoxLayout boxLayout = new BoxLayout(list, Y_AXIS);
        list.setLayout(boxLayout);

        JButton buttonAdd = new JButton("Add selected");
        buttonAdd.addActionListener(e ->
        {
            selectedDirectories.addAll(Arrays.asList(getSelectedFiles()).stream().map(file -> file.getAbsolutePath()).collect(Collectors.toList()));
            handleSelectedFilesChanged();
        });

        JButton buttonClose  = new JButton("Cancel");
        buttonClose.addActionListener(e -> dialog.dispose());

        JButton buttonOk = new JButton("Apply");
        buttonOk.addActionListener(e -> {
            response = Response.APPLY;
            dialog.dispose();
        });

        JButton buttonClear = new JButton("Clear");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(buttonAdd);
//        buttonPanel.add(buttonClear);
        buttonPanel.add(buttonOk);
        buttonPanel.add(buttonClose);
        buttonClear.addActionListener(e -> {
            selectedDirectories.clear();
            handleSelectedFilesChanged();
        });

        dirPanel.add(scrollPaneList, BorderLayout.CENTER);
        dirPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.getContentPane().add(dirPanel, BorderLayout.EAST);
        dialog.setSize(new Dimension(800, dialog.getHeight()));
        return dialog;
    }

    private void handleSelectedFilesChanged ()
    {
        list.removeAll();
        selectedDirectories.stream().forEach(s -> {
            list.add(new DirTextField(s, s1 -> {
                selectedDirectories.remove(s);
                handleSelectedFilesChanged();
            }));
        });
        list.revalidate();
        list.repaint();
    }
}
