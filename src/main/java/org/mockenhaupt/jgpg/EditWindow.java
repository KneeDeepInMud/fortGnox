package org.mockenhaupt.jgpg;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EditWindow
{
    private JDialog editWindow;
    private JEditorPane editorPane;
    private JComboBox<String> comboBoxDirectories;
    final private JGPGProcess jgpgProcess;

    final private List<String> directories = new ArrayList<>();

    public EditWindow (JFrame parent, JGPGProcess jgpgProcess)
    {
        this.jgpgProcess = jgpgProcess;
        init(parent);
    }


    public void setDirectories (List<String> directories)
    {
        this.directories.clear();
        this.directories.addAll(directories);

        comboBoxDirectories.setModel(new DefaultComboBoxModel<String>(){
            @Override
            public int getSize ()
            {
                return directories.size();
            }

            @Override
            public String getElementAt (int index)
            {
                return directories.get(index);
            }
        });
        if (comboBoxDirectories.getModel().getSize() > 0) comboBoxDirectories.setSelectedIndex(0);
    }

    public List<String> getDirectories ()
    {
        return directories;
    }

    public void setText (String text, String filename)
    {
        if (editorPane == null)
        {
            return;
        }

        editorPane.setText(text);
    }


    private void init (JFrame parent)
    {
        if (editWindow == null)
        {
            editWindow = new JDialog(parent, "JGPG Edit");
            editWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            URL url = this.getClass().getResource("kgpg_identity.png");
            editWindow.setIconImage(Toolkit.getDefaultToolkit().createImage(url));

            editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(600, 600));

            editWindow.setLayout(new BorderLayout());
            editWindow.add(editorPane, BorderLayout.CENTER);
            editWindow.add(commandToolbar(), BorderLayout.NORTH);

            editWindow.pack();
            editWindow.setVisible(false);

            setDirectories(jgpgProcess.getSecretdirs());
        }
    }


    public void show ()
    {
        editWindow.setVisible(true);
    }


    private JToolBar commandToolbar ()
    {
        JToolBar jToolBar = new JToolBar();

        // Button: Cancel
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(actionEvent ->
        {
            editWindow.setVisible(false);
            editWindow.dispose();
        });


        // Button: Save
        JButton saveButton = new JButton("Save/Encrypt");
        saveButton.addActionListener(actionEvent ->
        {
            EditWindow.this.setText("XXX\nasdfasdf\nLKJNLKJNLKJNLKJNL", null);
        });


        comboBoxDirectories = new JComboBox<>();

        jToolBar.add(saveButton);
        jToolBar.add(cancelButton);
        jToolBar.add(comboBoxDirectories);

        return jToolBar;
    }
}
