package org.mockenhaupt.fortgnox.swing;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static javax.swing.BoxLayout.Y_AXIS;

public class FgDirChooser extends JFileChooser
{
    JDialog dialog;

    final JPanel list = new JPanel();

    public FgDirChooser ()
    {
        super();
    }


    public static void main (String[] a)
    {
        FgDirChooser dirChooser = new FgDirChooser();
        dirChooser.showDirectoryDialog(null, null);
        System.out.println("XXX dirChooser.getSelectedFiles() = " + dirChooser.getSelectedDirectories());
    }

    public FgDirChooser showDirectoryDialog (Component parent, List<String> directoryList)
    {
        initialize();
        showDialog(parent, "");
        return this;
    }

    final SortedSet<String> selectedDirectories = new TreeSet<>();
    public Set<String> getSelectedDirectories ()
    {
        return selectedDirectories;
    }

    private void initialize ()
    {
        setAcceptAllFileFilterUsed(false);
        setMultiSelectionEnabled(true);
        setControlButtonsAreShown(false);
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }



    @Override
    protected JDialog createDialog (Component parent) throws HeadlessException
    {
        dialog = super.createDialog(parent);

        JPanel dirPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPaneList = new JScrollPane(list);
        BoxLayout boxLayout = new BoxLayout(list, Y_AXIS);
        list.setLayout(boxLayout);

        JButton buttonAdd = new JButton("Add");
        buttonAdd.addActionListener(e ->
        {
            selectedDirectories.addAll(Arrays.asList(getSelectedFiles()).stream().map(file -> file.getAbsolutePath()).collect(Collectors.toList()));
            handleSelectedFilesChanged();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(buttonAdd);
        JButton buttonClose;
        buttonPanel.add(buttonClose = new JButton("Close"));
        buttonClose.addActionListener(e -> dialog.dispose());
        JButton buttonClear;
        buttonPanel.add(buttonClear = new JButton("Clear"));
        buttonClear.addActionListener(e -> {
            selectedDirectories.clear();
            handleSelectedFilesChanged();
        });

        dirPanel.add(scrollPaneList, BorderLayout.CENTER);
        dirPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.getContentPane().add(dirPanel, BorderLayout.EAST);
        return dialog;
    }

    private void handleSelectedFilesChanged ()
    {
        list.removeAll();
        selectedDirectories.stream().forEach(s -> {
            list.add(new JTextField(s));
        });
        list.revalidate();
        list.repaint();
    }
}
