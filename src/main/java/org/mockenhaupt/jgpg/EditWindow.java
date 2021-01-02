package org.mockenhaupt.jgpg;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_GPG_DEFAULT_RID;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_GPG_POST_COMMAND;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_GPG_USE_ASCII;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_LOOK_AND_FEEL;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_SECRETDIRS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_TEXTAREA_FONT_SIZE;

public class EditWindow implements JGPGProcess.EncrypionListener,
        PropertyChangeListener,
        JGPGProcess.CommandListener,
        PasswordGenerator.PasswordInsertListener
{
    private JDialog editWindow;
    private JTextArea textArea;
    private JTextArea textAreaStatus;
    private JComboBox<String> comboBoxDirectories;
    private JTextField textFieldFilename;
    private final JLabel labelRID = new JLabel("RID");
    private JTextField textFieldRID;
    final private JGPGProcess jgpgProcess;
    private boolean modified = false;
    private JButton saveButton;
    private JCheckBox cbSkipPost;
    final private JFrame parentWindow;

    private String recipientId = "";

    final private List<String> directories = new ArrayList<>();

    private final PasswordGenerator passwordGenerator;

    EditHandler editHandler;
    public EditWindow (JFrame parent, JGPGProcess jgpgProcess, EditHandler editHandler)
    {
        this.jgpgProcess = jgpgProcess;
        this.parentWindow = parent;
        this.editHandler = editHandler;
        passwordGenerator = new PasswordGenerator(this);
        init(parent);
        jgpgProcess.addEncryptionListener(this);
        jgpgProcess.addCommandListener(this);
        JgpgPreferences.get().addPropertyChangeListener(this);
    }

    interface EditHandler
    {
        void handleFinished ();
        void handleNewFile (String fname);
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

    public void setText (String text, String status, String filename)
    {
        if (textArea == null)
        {
            return;
        }

        textArea.setText(text);
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
        this.modified = modified;
    }

    private final UndoManager undo = new UndoManager();

    private void init (JFrame parent)
    {
        if (editWindow == null)
        {
            editWindow = new JDialog(parent, "JGPG Edit", true);
            editWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            URL url = this.getClass().getResource("kgpg_identity.png");
            editWindow.setIconImage(Toolkit.getDefaultToolkit().createImage(url));

            textArea = new JTextArea();
            Document doc = textArea.getDocument();
            doc.addUndoableEditListener(new UndoableEditListener()
            {
                public void undoableEditHappened (UndoableEditEvent evt)
                {
                    undo.addEdit(evt.getEdit());
                }
            });

            textArea.getActionMap().put("Undo",
                    new AbstractAction("Undo")
                    {
                        public void actionPerformed (ActionEvent evt)
                        {
                            try
                            {
                                if (undo.canUndo())
                                {
                                    undo.undo();
                                }
                            }
                            catch (CannotUndoException e)
                            {
                            }
                        }
                    });

            textArea.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

            textArea.getActionMap().put("Redo",
                    new AbstractAction("Redo")
                    {
                        public void actionPerformed (ActionEvent evt)
                        {
                            try
                            {
                                if (undo.canRedo())
                                {
                                    undo.redo();
                                }
                            }
                            catch (CannotRedoException e)
                            {
                            }
                        }
                    });

            textArea.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");


            textArea.setFont(new Font("monospaced", Font.PLAIN,
                    JgpgPreferences.get().get(PREF_TEXTAREA_FONT_SIZE, 14)));

            JScrollPane editorScrollPane = new JScrollPane(textArea);
            editorScrollPane.setViewportView(textArea);

            editorScrollPane.setPreferredSize(new Dimension(800, 600));
            textArea.getDocument()
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

    public Container getTextArea ()
    {
        SwingUtilities.invokeLater(()->textArea.requestFocus());
        return editWindow.getRootPane();
    }

    public void show ()
    {
        Point location = MouseInfo.getPointerInfo().getLocation();
        editWindow.setLocation(location);
        SwingUtilities.invokeLater(()->textArea.requestFocus());

        editWindow.setVisible(true);
    }

    public void showNew ()
    {
        JDialog directoryChooser = new JDialog(parentWindow, "JGPG New Password", true);
        directoryChooser.getRootPane().registerKeyboardAction(e ->
                directoryChooser.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        directoryChooser.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        directoryChooser.setLayout(new BorderLayout());


        JPanel directoryChooserPanel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(directoryChooserPanel);
        directoryChooserPanel.setLayout(groupLayout);

        directoryChooser.add(directoryChooserPanel, BorderLayout.CENTER);

        directoryChooser.setPreferredSize(new Dimension(650, 200));
        directoryChooser.setMinimumSize(directoryChooser.getPreferredSize());


        GpgFileUtils.ParsedDirectories parsedDirectories =
                GpgFileUtils.splitDirectoryString(JgpgPreferences.get().get(PREF_SECRETDIRS));
        JComboBox<String> comboBoxDirectories = new JComboBox<>();
        comboBoxDirectories.setMaximumSize(new Dimension(500, 35));

        comboBoxDirectories.setModel(new DefaultComboBoxModel<String>()
        {
            @Override
            public int getSize ()
            {
                return parsedDirectories.directoryList.size();
            }

            @Override
            public String getElementAt (int index)
            {
                return parsedDirectories.directoryList.get(index);
            }
        });
        JTextField fileNameResulting = new JTextField("");
        JTextField fileNameText = new JTextField();
        comboBoxDirectories.addActionListener(actionEvent -> updateFilenamePreview(comboBoxDirectories, fileNameResulting, fileNameText));
        if (comboBoxDirectories.getModel().getSize() > 0)
        {
            comboBoxDirectories.setSelectedIndex(0);
        }
        fileNameText.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate (DocumentEvent documentEvent)
            {
                update();
            }

            @Override
            public void removeUpdate (DocumentEvent documentEvent)
            {
                update();
            }

            @Override
            public void changedUpdate (DocumentEvent documentEvent)
            {
                update();
            }

            private void update ()
            {
                updateFilenamePreview(comboBoxDirectories, fileNameResulting, fileNameText);
            }
        });
        fileNameResulting.setEnabled(false);
        fileNameText.setMaximumSize(new Dimension(500, 35));

        JLabel dirNameLabel = new JLabel("Directory", SwingConstants.RIGHT);
        JLabel fileNameLabel = new JLabel("New filename:", SwingConstants.RIGHT);


        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);

        // horizontal
        GroupLayout gl = groupLayout;
        gl.setHorizontalGroup(gl.createParallelGroup()
                .addGroup(gl.createSequentialGroup()
                        .addGroup(gl.createParallelGroup()
                                .addComponent(dirNameLabel)
                                .addComponent(fileNameLabel))
                        .addGroup(gl.createParallelGroup()
                                .addComponent(comboBoxDirectories)
                                .addComponent(fileNameText)))
                .addComponent(fileNameResulting));

        // vertical
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup().addComponent(dirNameLabel).addComponent(comboBoxDirectories))
                .addGroup(gl.createParallelGroup().addComponent(fileNameLabel).addComponent(fileNameText))
                .addComponent(fileNameResulting));

        JButton buttonCreate;
        JButton buttonCancel;

        JPanel buttonPanel = new JPanel();
        directoryChooser.add(buttonPanel, BorderLayout.SOUTH);
        buttonCreate = new JButton("Create New");
        buttonCreate.setMnemonic(KeyEvent.VK_N);
        buttonCancel = new JButton("Cancel");
        buttonCancel.setMnemonic(KeyEvent.VK_C);

        buttonPanel.add(buttonCreate);
        buttonPanel.add(buttonCancel);

        buttonCancel.addActionListener(actionEvent -> directoryChooser.dispose());
        buttonCreate.addActionListener(actionEvent ->
        {
            handleButtonNewFileSelected(directoryChooser, comboBoxDirectories, fileNameText);
        });

        fileNameText.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate (DocumentEvent documentEvent)
            {
                handleChange();
            }

            @Override
            public void removeUpdate (DocumentEvent documentEvent)
            {
                handleChange();
            }

            @Override
            public void changedUpdate (DocumentEvent documentEvent)
            {
                handleChange();
            }

            void handleChange ()
            {
                String ftest = getNewFilename(comboBoxDirectories, fileNameText);
                File ftestFile = new File(ftest);
                buttonCreate.setEnabled(!ftestFile.exists() && !fileNameText.getText().isEmpty());
            }



        });

        buttonCreate.setEnabled(false);



        directoryChooser.getRootPane().registerKeyboardAction(e ->
                {
                    if (buttonCreate.isEnabled())
                    {
                        handleButtonNewFileSelected(directoryChooser, comboBoxDirectories, fileNameText);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);


        Point location = MouseInfo.getPointerInfo().getLocation();
        directoryChooser.setLocation(location);
        SwingUtilities.invokeLater(() -> fileNameText.requestFocus());
        directoryChooser.setVisible(true);
    }

    private void handleButtonNewFileSelected (JDialog directoryChooser, JComboBox<String> comboBoxDirectories, JTextField fileNameText)
    {
        String file = getNewFilename(comboBoxDirectories, fileNameText);
        setText("", "Enter new file " + file, file);
        directoryChooser.dispose();
        if (editHandler != null)
        {
            editHandler.handleNewFile(file);
        }
        else
        {
            show();
        }
    }

    private void updateFilenamePreview (JComboBox<String> comboBoxDirectories, JTextField fileNameResulting, JTextField fileNameText)
    {
        if (fileNameText.getText().isEmpty())
        {
            fileNameResulting.setText("");
        }
        else
        {
            fileNameResulting.setText(comboBoxDirectories.getSelectedItem() + File.separator + fileNameText.getText() + getSuffix());
        }
    }

    private String getNewFilename (JComboBox<String> comboBoxDirectories, JTextField fileName)
    {
        String suffix = getSuffix();

        return comboBoxDirectories.getSelectedItem() + File.separator + fileName.getText() + suffix;
    }

    private String getSuffix ()
    {
        String suffix;
        if (JgpgPreferences.get().getBoolean(PREF_GPG_USE_ASCII))
        {
            suffix = ".asc";
        }
        else
        {
            suffix = ".gpg";
        }
        return suffix;
    }


    private Container commandToolbar ()
    {
        JPanel jToolBar = new JPanel();
        GroupLayout gl = new GroupLayout(jToolBar);
        jToolBar.setLayout(gl);


        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        // Button: Cancel
        JButton cancelButton = new JButton("Cancel / Close");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(actionEvent ->
        {
            if (!modified || OK_OPTION == JOptionPane.showConfirmDialog(parentWindow,
                    "File is modified, close discarding changes?",
                    "JGPG Close Confirmation", OK_CANCEL_OPTION))
            {
                if (editWindow.isVisible())
                {
                    editWindow.setVisible(false);
                    editWindow.dispose();
                }
                if (editHandler != null)
                {
                    editHandler.handleFinished();
                }
            }
        });


        // Button: Save
        saveButton = new JButton("Save / Encrypt");
        saveButton.setMnemonic(KeyEvent.VK_S);
        saveButton.addActionListener(actionEvent -> doEncrypt());

        cbSkipPost = new JCheckBox("Skip post");
        cbSkipPost.setToolTipText("Do not execute the post command defined in the settings");
        cbSkipPost.setVisible(!JgpgPreferences.get().get(PREF_GPG_POST_COMMAND).isEmpty());
        cbSkipPost.setMnemonic('k');

        textFieldFilename = new JTextField();
        textFieldFilename.setEnabled(false);

        textFieldRID = new JTextField();
//        textFieldRID.setMinimumSize(new Dimension(200, 30));
//        textFieldRID.setPreferredSize(new Dimension(200, 30));

        comboBoxDirectories = new JComboBox<>();
        comboBoxDirectories.setVisible(false);

        JPanel passwordGeneratorPanel = passwordGenerator.getGeneratorPanel();
        gl.setHorizontalGroup(gl.createParallelGroup()
                .addGroup(gl.createSequentialGroup()
                        .addComponent(cancelButton)
                        .addComponent(saveButton)
                        .addComponent(comboBoxDirectories, 100, 200, 300)
                        .addComponent(textFieldFilename, 100, 100, 300)
                        .addComponent(labelRID)
                        .addComponent(textFieldRID, 20, 100, 200)
                        .addComponent(cbSkipPost)
                )
                .addComponent(passwordGeneratorPanel)
        );

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(cancelButton)
                        .addComponent(saveButton)
                        .addComponent(comboBoxDirectories)
                        .addComponent(textFieldFilename)
                        .addComponent(labelRID)
                        .addComponent(textFieldRID)
                        .addComponent(cbSkipPost)
                )
                .addComponent(passwordGeneratorPanel)
        );

        return jToolBar;
    }

    private String getRecipient (File gpgFile)
    {
        // password store file with recipient ID in directory
        String ridFileName = JgpgPreferences.get().get(JgpgPreferences.PREF_GPG_RID_FILE);
        File ridFile = new File(gpgFile.getParent() + File.separator + ridFileName);
        if (ridFile.exists() && ridFileName != null && !ridFileName.isEmpty())
        {
            DebugWindow.dbg(DebugWindow.Category.GPG, "Found " + ridFile);
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
//        File file = new File(textFieldFilename.getText());
//        if (!file.exists())
//        {
//            JOptionPane.showMessageDialog(editWindow, "file does not exist", "JGPG WARNING", WARNING_MESSAGE);
//            return;
//        }

        String rid = textFieldRID.getText();

        if (rid == null || rid.isEmpty())
        {
            JOptionPane.showMessageDialog(editWindow, "Cannot determine recipient", "JGPG WARNING", WARNING_MESSAGE);
            return;
        }

        this.recipientId = rid;
        jgpgProcess.encrypt(textFieldFilename.getText(), textArea.getText(), rid, EditWindow.this);
    }

    @Override
    public void handleGpgEncryptResult (String out, String err, String filename, Object clientData)
    {
        SwingUtilities.invokeLater(() ->
        {
            String rid = "";
            if (clientData instanceof EditWindow)
            {
                rid = "\nwith recipient ID " + ((EditWindow) clientData).recipientId;
            }
            if (err == null || err.isEmpty())
            {
                String status = "Successfully encrypted " + filename + rid;
                setText("", status, filename);
                String postCommand = JgpgPreferences.get().get(PREF_GPG_POST_COMMAND);
                if (postCommand != null && !postCommand.isEmpty() && !cbSkipPost.isSelected())
                {
                    jgpgProcess.command(postCommand, filename, this);
                    editWindow.dispose();
                }
                else
                {
                    editWindow.dispose();
                    JOptionPane.showMessageDialog(editWindow, status, "JGPG INFO", JOptionPane.INFORMATION_MESSAGE);
                    if (editHandler != null)
                    {
                        editHandler.handleFinished();
                    }
                }
            }
            else
            {
                textAreaStatus.setText("Failure encrypting " + filename + rid + ", " + err);
                JOptionPane.showMessageDialog(editWindow, err, "JGPG WARNING", JOptionPane.ERROR_MESSAGE);
                if (editHandler != null)
                {
                    editHandler.handleFinished();
                }
            }
        });
    }


    @Override
    public void propertyChange (PropertyChangeEvent propertyChangeEvent)
    {
        switch (propertyChangeEvent.getPropertyName())
        {
            case PREF_TEXTAREA_FONT_SIZE:
                if (textArea != null)
                {
                    textArea.setFont(new Font("monospaced", Font.PLAIN,
                            JgpgPreferences.get().get(PREF_TEXTAREA_FONT_SIZE, 14)));
                }
                break;
            case PREF_GPG_POST_COMMAND:
                if (cbSkipPost != null)
                {
                    cbSkipPost.setVisible(!JgpgPreferences.get().get(PREF_GPG_POST_COMMAND).isEmpty());
                }
                break;
            case PREF_LOOK_AND_FEEL:
                LAFChooser.get().set((String)propertyChangeEvent.getNewValue(),editWindow);
                break;

        }
    }

    @Override
    public void handleGpgCommandResult (String out, String err, String filename, Object clientData)
    {
        if (err != null && !err.isEmpty())
        {
            JOptionPane.showMessageDialog(editWindow, out + err, "JGPG POST", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            JOptionPane.showMessageDialog(editWindow, out + err, "JGPG POST", JOptionPane.INFORMATION_MESSAGE);
        }
        if (editHandler != null)
        {
            editHandler.handleFinished();
        }
    }

    @Override
    public void handlePasswordInsert (String password)
    {
        try
        {
            textArea.getDocument().insertString(textArea.getCaretPosition(), password, null);
            textArea.requestFocus();
        }
        catch (BadLocationException e)
        {
            textAreaStatus.setText("Failure inserting password, " + e.toString());
        }
    }
}
