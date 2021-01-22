package org.mockenhaupt.fortgnox.swing;

import org.mockenhaupt.fortgnox.misc.FileUtils;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

    public enum Response
    {
        APPLY,
        CANCEL
    }

    public FgDirChooser ()
    {
        super();
    }


    class DirTextField extends JPanel
    {
        private Runnable onSel;
        private Runnable onDel;
        private String text;

        public DirTextField (String text, Runnable onDel, Runnable onSel)
        {
            this.onDel = onDel;
            this.onSel = onSel;
            this.text = text;
            init();
        }


        private void init ()
        {
            setLayout(new BorderLayout());
            JButton buttonDel = new JButton();
            buttonDel.setToolTipText("Click to remove entry");
            buttonDel.setIcon(FileUtils.getScaledIcon(new ImageIcon(getClass().getResource("/org/mockenhaupt/fortgnox/cross32.png")), 28));

            buttonDel.addActionListener(e -> onDel.run());
            add(buttonDel, BorderLayout.WEST);

            JButton buttonDir = new JButton(text);
            buttonDir.setHorizontalAlignment(SwingConstants.LEFT);
            buttonDir.setToolTipText("Click to change to directory");
            add(buttonDir, BorderLayout.CENTER);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            buttonDir.addActionListener(e -> onSel.run());
        }
    }

    public static void main (String[] a)
    {
        FgDirChooser dirChooser = new FgDirChooser();
        dirChooser.showDirectoryDialog(null, null);
    }

    public Response showDirectoryDialog (Component parent, List<String> directoryList)
    {
        initialize();
        if (directoryList != null)
        {
            selectedDirectories.addAll(directoryList.stream().filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList()));
            handleSelectedFilesChanged();
        }
        showDialog(parent, "");
        return response;
    }

    public Set<String> getSelectedDirectories ()
    {
        return selectedDirectories;
    }


    private void initialize ()
    {
        setDialogTitle("Select directory(ies) for storing password files");
        setAcceptAllFileFilterUsed(false);
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

        JButton buttonClose = new JButton("Cancel");
        buttonClose.addActionListener(e -> dialog.dispose());

        JButton buttonOk = new JButton("Apply");
        buttonOk.addActionListener(e ->
        {
            response = Response.APPLY;
            dialog.dispose();
        });

//        JButton buttonClear = new JButton("Clear");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(buttonAdd);
//        buttonPanel.add(buttonClear);
        buttonPanel.add(buttonOk);
        buttonPanel.add(buttonClose);
        JCheckBox cbHidden = new JCheckBox("show hidden");
        cbHidden.setSelected(!isFileHidingEnabled());
        buttonPanel.add(cbHidden);
        cbHidden.addActionListener(e -> setFileHidingEnabled(!cbHidden.isSelected()));

//        buttonClear.addActionListener(e -> {
//            selectedDirectories.clear();
//            handleSelectedFilesChanged();
//        });

        dirPanel.add(scrollPaneList, BorderLayout.CENTER);
        dirPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.getContentPane().add(dirPanel, BorderLayout.EAST);
        dialog.setSize(new Dimension(800, dialog.getHeight()));
        return dialog;
    }

    private void handleSelectedFilesChanged ()
    {
        list.removeAll();
        selectedDirectories.stream().forEach(s ->
        {
            list.add(new DirTextField(s,
                    () ->
                    {
                        setCurrentDirectory(new File(s + File.separator + ".."));
                        selectedDirectories.remove(s);
                        handleSelectedFilesChanged();
                    },
//                    () -> setCurrentDirectory(new File(s + File.separator + ".."))
                    () -> setCurrentDirectory(new File(s))
            ));
        });
        list.revalidate();
        list.repaint();
    }
}
