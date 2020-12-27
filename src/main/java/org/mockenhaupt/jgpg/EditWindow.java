package org.mockenhaupt.jgpg;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_GPG_DEFAULT_RID;

public class EditWindow implements JGPGProcess.EncrypionListener
{
    private JDialog editWindow;
    private JEditorPane editorPane;
    private JTextArea textAreaStatus;
    private JComboBox<String> comboBoxDirectories;
    private JTextField textFieldFilename;
    private JTextField textFieldRID;
    final private JGPGProcess jgpgProcess;
    private boolean modified = false;
    private JButton cancelButton;
    private JButton saveButton;
    final private JFrame parentWindow;

    private String recipientId = "";

    final private List<String> directories = new ArrayList<>();

    public EditWindow (JFrame parent, JGPGProcess jgpgProcess)
    {
        this.jgpgProcess = jgpgProcess;
        this.parentWindow = parent;
        init(parent);
        jgpgProcess.addEncryptionListener(this);
    }


    public void setDirectories (List<String> directories)
    {
        this.directories.clear();
        this.directories.addAll(directories);

        comboBoxDirectories.setModel(new DefaultComboBoxModel<String>()
        {
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

    public void setText (String text, String status, String filename)
    {
        if (editorPane == null)
        {
            return;
        }

        editorPane.setText(text);
        setModified(false);
        textFieldFilename.setText(filename);
        textFieldRID.setText(getRecipient(new File(filename)));
        textAreaStatus.setText(status);
    }

    public boolean isModified ()
    {
        return modified;
    }

    public void setModified (boolean modified)
    {
        this.saveButton.setEnabled(modified);
        this.saveButton.setVisible(modified);
        this.modified = modified;
    }

    private void init (JFrame parent)
    {
        if (editWindow == null)
        {
            editWindow = new JDialog(parent, "JGPG Edit", true);
            editWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            URL url = this.getClass().getResource("kgpg_identity.png");
            editWindow.setIconImage(Toolkit.getDefaultToolkit().createImage(url));

            editorPane = new JEditorPane();
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorPane.setBorder(BorderFactory.createLineBorder(Color.BLUE));

            editorPane.setPreferredSize(new Dimension(800, 600));
            editorPane.getDocument()
                    .addDocumentListener(new DocumentListener()
                    {
                        @Override
                        public void insertUpdate (DocumentEvent documentEvent)
                        {
                            setModified(true);
                        }

                        @Override
                        public void removeUpdate (DocumentEvent documentEvent)
                        {
                            setModified(true);
                        }

                        @Override
                        public void changedUpdate (DocumentEvent documentEvent)
                        {
                            setModified(true);
                        }
                    });

            editWindow.setLayout(new BorderLayout());
            editWindow.add(editorScrollPane, BorderLayout.CENTER);
            editWindow.add(commandToolbar(), BorderLayout.NORTH);

            textAreaStatus = new JTextArea();
            textAreaStatus.setFont(textAreaStatus.getFont().deriveFont(Font.BOLD));
            textAreaStatus.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));

            textAreaStatus.setEditable(false);
            editWindow.add(textAreaStatus, BorderLayout.SOUTH);

            editWindow.pack();
            editWindow.setVisible(false);
            setModified(false);
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
        cancelButton = new JButton("Cancel / Close");
        cancelButton.addActionListener(actionEvent ->
        {
            if (!modified || OK_OPTION == JOptionPane.showConfirmDialog(parentWindow,
                    "File is modified, close discarding changes?",
                    "JGPG Close Confirmation", OK_CANCEL_OPTION))
            {
                editWindow.setVisible(false);
                editWindow.dispose();
            }

        });


        // Button: Save
        saveButton = new JButton("Save / Encrypt");
        saveButton.addActionListener(actionEvent -> doEncrypt());

        textFieldFilename = new JTextField();
        textFieldFilename.setEnabled(false);

        textFieldRID = new JTextField();
        textFieldRID.setMinimumSize(new Dimension(200,30));
        textFieldRID.setPreferredSize(new Dimension(200,30));

        comboBoxDirectories = new JComboBox<>();
        comboBoxDirectories.setVisible(false);

        jToolBar.add(cancelButton);
        jToolBar.add(saveButton);
        jToolBar.add(comboBoxDirectories);
        jToolBar.add(textFieldFilename);

        jToolBar.add(new JLabel("RID:"));
        jToolBar.add(textFieldRID);

        return jToolBar;
    }

    private String getRecipient (File gpgFile)
    {
        // password store file with recipient ID in directory
        String ridFileName = JgpgPreferences.get().get(JgpgPreferences.PREF_GPG_RID_FILE);
        File ridFile = new File(gpgFile.getParent() + File.separator + ridFileName);
        if (ridFile.exists())
        {
            String rid = GpgFileUtils.getFileContent(ridFile.getAbsolutePath());
            if (rid != null)
            {
                return rid.trim();
            }
        }
        else
        {
            // Try to use RID from directories
            GpgFileUtils.ParsedDirectories parsedDirectories = GpgFileUtils.splitDirectoryString(JgpgPreferences.get().get(JgpgPreferences.PREF_SECRETDIRS));
            String rid = parsedDirectories.directoryRecipientMap.get(gpgFile.getParent());
            if (rid != null)
            {
                return rid;
            }

            // default ricipient ID
            return JgpgPreferences.get().get(PREF_GPG_DEFAULT_RID);
        }
        return null;
    }


    private void doEncrypt ()
    {
        File file = new File(textFieldFilename.getText());
        if (!file.exists())
        {
            JOptionPane.showMessageDialog(editWindow, "file does not exist", "JGPG WARNING", WARNING_MESSAGE);
            return;
        }

        String rid = textFieldRID.getText();

        if (rid == null || rid.isEmpty())
        {
            JOptionPane.showMessageDialog(editWindow, "Cannot determine recipient", "JGPG WARNING", WARNING_MESSAGE);
            return;
        }

        this.recipientId = rid;
        jgpgProcess.encrypt(textFieldFilename.getText(), editorPane.getText(), rid, EditWindow.this);
    }

    @Override
    public void handleGpgEncryptResult (String out, String err, String filename, Object clientData)
    {
        SwingUtilities.invokeLater(() ->
        {
            String rid = "";
            if (clientData instanceof EditWindow)
            {
                rid = " with recipient ID " + ((EditWindow) clientData).recipientId;
            }
            if (err == null || err.isEmpty())
            {
                textAreaStatus.setText("Successfully encrypted " + filename + rid);
                setModified(false);
            }
            else
            {
                textAreaStatus.setText("Failure encrypting " + filename + rid + ", " + err);
                JOptionPane.showMessageDialog(editWindow, err, "JGPG WARNING", JOptionPane.ERROR_MESSAGE);
            }
        });
    }


}
