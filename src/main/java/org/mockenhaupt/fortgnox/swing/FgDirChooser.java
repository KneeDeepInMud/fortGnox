package org.mockenhaupt.fortgnox.swing;

import org.mockenhaupt.fortgnox.misc.FileUtils;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static javax.swing.BoxLayout.Y_AXIS;

public class FgDirChooser extends JDialog
{

    final JPanel list = new JPanel();
    final SortedSet<String> selectedDirectories = new TreeSet<>();
    private Response response = Response.CANCEL;
    Window owner;
    boolean showHiddenFiles = false;
    private File selectedDirectory;
    JButton buttonAdd;
    private String currentAddValue = null;
    JCheckBox cbHidden;

    public FgDirChooser (Window owner)
    {
        super(owner, APPLICATION_MODAL);
        this.owner = owner;
    }

    public enum Response
    {
        APPLY,
        CANCEL
    }

//    public FgDirChooser ()
//    {
//        super();
//    }


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
        FgDirChooser dirChooser = new FgDirChooser(null);
        dirChooser.showDirectoryDialog(null);
    }

    public Response showDirectoryDialog (List<String> directoryList)
    {
        if (directoryList != null)
        {
            selectedDirectories.addAll(directoryList.stream().filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList()));
        }
        initialize();
        handleSelectedFilesChanged();
        setVisible(true);
        return response;
    }

    public Set<String> getSelectedDirectories ()
    {
        return selectedDirectories;
    }


    private void initialize ()
    {
        setTitle("Select directory(ies) for storing password files");
        createDialog(getParent());
        handleAddValue();
        setSize(new Dimension(600, 400));
    }


    protected JDialog createDialog (Component parent) throws HeadlessException
    {
        JPanel dirPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPaneList = new JScrollPane(list);
        BoxLayout boxLayout = new BoxLayout(list, Y_AXIS);
        list.setLayout(boxLayout);

        buttonAdd = new JButton("Add selected");
        buttonAdd.addActionListener(e ->
        {
            if (currentAddValue != null)
            {
                selectedDirectories.add(currentAddValue);
                handleSelectedFilesChanged();
            }

        });

        JButton buttonClose = new JButton("Cancel");
        buttonClose.addActionListener(e -> dispose());

        JButton buttonOk = new JButton("Ok");
        buttonOk.addActionListener(e ->
        {
            response = Response.APPLY;
            dispose();
        });

//        JButton buttonClear = new JButton("Clear");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(buttonAdd);
//        buttonPanel.add(buttonClear);
        buttonPanel.add(buttonOk);
        buttonPanel.add(buttonClose);
        cbHidden = new JCheckBox("show hidden");
        cbHidden.setSelected(isShowHiddenFiles());
        buttonPanel.add(cbHidden);
        cbHidden.addActionListener(e -> setShowHiddenFiles(cbHidden.isSelected()));

//        buttonClear.addActionListener(e -> {
//            selectedDirectories.clear();
//            handleSelectedFilesChanged();
//        });

        dirPanel.add(scrollPaneList, BorderLayout.CENTER);
        dirPanel.add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(getFileList(), BorderLayout.CENTER);
        getContentPane().add(dirPanel, BorderLayout.EAST);
        setSize(new Dimension(800, getHeight()));
        return this;
    }

    private JList jListDirectories;
    private JScrollPane scrollPaneListDirectories = null;

    private Component getFileList ()
    {
        if (scrollPaneListDirectories == null)
        {
            jListDirectories = new JList();
            jListDirectories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            scrollPaneListDirectories = new JScrollPane(jListDirectories);

            jListDirectories.addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged (ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        handleAddValue();
                    }
                }
            });
            jListDirectories.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked (MouseEvent e)
                {
                    if (e.getClickCount() == 2)
                    {
                        setSelectedDirectory(new File(selectedDirectory + File.separator + jListDirectories.getSelectedValue()));
                    }
                }
            });
            String initialDir = selectedDirectories.stream()
                    .filter(s -> new File(s).isDirectory())
                    .findFirst()
                    .orElse("/");
            setSelectedDirectory(new File(initialDir));
        }
        return scrollPaneListDirectories;
    }

    private void handleAddValue ()
    {

        buttonAdd.setEnabled(jListDirectories.getSelectedValue() != null);
        currentAddValue = null;
        if (buttonAdd.isEnabled())
        {
            try
            {
                currentAddValue = new File(getSelectedDirectory() + File.separator + jListDirectories.getSelectedValue()).getCanonicalPath();
            }
            catch (IOException e)
            {
                // ignore here
            }
        }
    }

    private void setSelectedDirectory (File directory)
    {
        if (directory == null) return;
        if (!directory.isDirectory())
        {
            return;
        }
        try
        {
            this.selectedDirectory = new File(directory.getCanonicalPath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        File[] subDirs = selectedDirectory.listFiles(new FileFilter()
        {
            @Override
            public boolean accept (File pathname)
            {
                boolean accept = !pathname.isHidden() || (pathname.isHidden() && cbHidden.isSelected());
                return accept && pathname != null && pathname.isDirectory();
            }
        });

        List<String> fileList = new ArrayList<>();
        fileList.add(".");
        if (selectedDirectory.getParentFile() != null)
        {
            fileList.add("..");
        }

        fileList.addAll(Arrays.asList(subDirs).stream().map(file -> file.getName()).collect(Collectors.toList()));
        jListDirectories.setModel(new AbstractListModel()
        {
            @Override
            public int getSize ()
            {
                return fileList.size();
            }

            @Override
            public Object getElementAt (int index)
            {
                return fileList.get(index);
            }
        });
    }


    private void handleSelectedFilesChanged ()
    {
        list.removeAll();
        selectedDirectories.stream().forEach(s ->
        {
            list.add(new DirTextField(s,
                    () ->
                    {
                        setSelectedDirectory(new File(s + File.separator + ".."));
                        selectedDirectories.remove(s);
                        handleSelectedFilesChanged();
                    },

                    () -> setSelectedDirectory(new File(s))

            ));
        });
        list.revalidate();
        list.repaint();
    }


    public boolean isShowHiddenFiles ()
    {
        return showHiddenFiles;
    }

    public void setShowHiddenFiles (boolean showHiddenFiles)
    {
        this.showHiddenFiles = showHiddenFiles;
        setSelectedDirectory(selectedDirectory);
    }

    public File getSelectedDirectory ()
    {
        return selectedDirectory;
    }
}
