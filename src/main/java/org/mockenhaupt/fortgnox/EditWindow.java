package org.mockenhaupt.fortgnox;

import org.mockenhaupt.fortgnox.misc.FileUtils;
import org.mockenhaupt.fortgnox.swing.FgTextFilter;
import org.mockenhaupt.fortgnox.swing.LAFChooser;
import org.mockenhaupt.fortgnox.tags.TagsStore;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.swing.JOptionPane.*;
import static org.mockenhaupt.fortgnox.FgPreferences.*;

public class EditWindow implements FgGPGProcess.EncrypionListener,
        PropertyChangeListener,
        FgGPGProcess.CommandListener,
        PasswordGenerator.PasswordInsertListener,
        FgTextFilter.TextFilterHandler
{
    private JPanel editPanel;
    private JTextArea textArea;
    private JTextArea textAreaStatus;
    private JComboBox<String> comboBoxDirectories;
    private JTextField textFieldFilename;
    private final JLabel labelRID = new JLabel("RID");
    private JTextField textFieldRID;
    final private FgGPGProcess fgGPGProcess;
    private boolean secretTextModified = false;
    private boolean tagsModified = false;
    private String tagsText = "";
    private JButton saveButton;
    private JCheckBox cbSkipPost;
    final private JFrame parentWindow;
    private String recipientId = "";
    final private List<String> directories = new ArrayList<>();
    private final UndoManager undo = new UndoManager();
    private final PasswordGenerator passwordGenerator;

    private final FgTextFilter tagsEditPanel;

    EditHandler editHandler;

    public EditWindow (JFrame parent, FgGPGProcess fgGPGProcess, EditHandler editHandler)
    {
        this.fgGPGProcess = fgGPGProcess;
        this.parentWindow = parent;
        this.editHandler = editHandler;
        passwordGenerator = new PasswordGenerator(this);
        this.tagsEditPanel = new FgTextFilter("Tags:", this);

        init(parent);
        fgGPGProcess.addEncryptionListener(this);
        fgGPGProcess.addCommandListener(this);
        FgPreferences.get().addPropertyChangeListener(this);
    }

    public void reset ()
    {
        passwordGenerator.resetPasswords();
    }

    @Override
    public void handleTextFilterChanged (String tags)
    {
        if (!textFieldFilename.getText().isEmpty())
        {
            tagsText = tags;
            setTagsModified(true);
        }
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

        this.tagsText = TagsStore.getTagsOfFile(filename, true);
        this.tagsEditPanel.setText(tagsText);

        textArea.setText(text);
        setSecretTextModified(false);
        setTagsModified(false);
        textFieldFilename.setText(filename);
        textFieldRID.setText(getRecipient(new File(filename)));
        setStatusText(status);
    }

    private void setStatusText (String status)
    {
        textAreaStatus.setText(status);
    }

    public boolean isSecretTextModified ()
    {
        return secretTextModified;
    }

    public void setSecretTextModified (boolean secretTextModified)
    {
        this.secretTextModified = secretTextModified;
        this.saveButton.setEnabled(isModified());
    }

    public boolean isModified ()
    {
        return this.tagsModified || this.secretTextModified;
    }

    public boolean isTagsModified ()
    {
        return tagsModified;
    }

    public void setTagsModified (boolean tagsModified)
    {
        this.tagsModified = tagsModified;
        this.saveButton.setEnabled(isModified());
    }

    public Container getTextArea ()
    {
        SwingUtilities.invokeLater(() -> textArea.requestFocus());
        editPanel.setVisible(true);
        return editPanel;
    }

    public void showNew ()
    {
        JDialog directoryChooser = new JDialog(parentWindow, "fortgnox New Password", true);
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


        FileUtils.ParsedDirectories parsedDirectories =
                FileUtils.splitDirectoryString(FgPreferences.get().get(PREF_SECRETDIRS));
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
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
                .addGroup(groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup()
                                .addComponent(dirNameLabel)
                                .addComponent(fileNameLabel))
                        .addGroup(groupLayout.createParallelGroup()
                                .addComponent(comboBoxDirectories)
                                .addComponent(fileNameText)))
                .addComponent(fileNameResulting));

        // vertical
        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup().addComponent(dirNameLabel).addComponent(comboBoxDirectories))
                .addGroup(groupLayout.createParallelGroup().addComponent(fileNameLabel).addComponent(fileNameText))
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

    private void init (JFrame parent)
    {
        if (editPanel == null)
        {
            editPanel = new JPanel();
//            URL url = this.getClass().getResource("fortGnox.png");

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
                    FgPreferences.get().get(PREF_TEXTAREA_FONT_SIZE, 14)));

            JScrollPane editorScrollPane = new JScrollPane(textArea);
            editorScrollPane.setViewportView(textArea);

            editorScrollPane.setPreferredSize(new Dimension(800, 600));
            textArea.getDocument()
                    .addDocumentListener(new DocumentListener()
                    {
                        @Override
                        public void insertUpdate (DocumentEvent documentEvent)
                        {
                            setSecretTextModified(true);
                        }

                        @Override
                        public void removeUpdate (DocumentEvent documentEvent)
                        {
                            setSecretTextModified(true);
                        }

                        @Override
                        public void changedUpdate (DocumentEvent documentEvent)
                        {
                            setSecretTextModified(true);
                        }
                    });


            editPanel.setLayout(new BorderLayout());
            editPanel.add(editorScrollPane, BorderLayout.CENTER);
            editPanel.add(commandToolbar(), BorderLayout.NORTH);

            textAreaStatus = new JTextArea();
            textAreaStatus.setFont(textAreaStatus.getFont().deriveFont(Font.BOLD));
            textAreaStatus.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));

            textAreaStatus.setEditable(false);
            editPanel.add(textAreaStatus, BorderLayout.SOUTH);

            editPanel.setVisible(false);
            setSecretTextModified(false);
            setTagsModified(false);
            setDirectories(fgGPGProcess.getSecretdirs());
            LAFChooser.setPreferenceLaf(textArea);

            LAFChooser.setPreferenceLaf(editPanel);
        }
    }

    private String getNewFileTemplateText (String newFileName) throws IOException
    {
        String fname = FgPreferences.get().get(PREF_NEW_TEMPLATE);

        BufferedReader br = null;
        try
        {
            if (fname == null || fname.isEmpty())
            {
                URL url = this.getClass().getResource("/org/mockenhaupt/fortgnox/template.txt");
                if (url == null) return "";
                br = new BufferedReader(new InputStreamReader(url.openStream()));
            }
            else
            {
                br = new BufferedReader(new FileReader(fname));
            }


            final boolean[] isFirst = {true};
            StringBuilder sb = new StringBuilder();
            br.lines().forEach(s ->
                    {
                        if (!isFirst[0])
                        {
                            sb.append('\n');
                        }
                        if (s.contains("$FILENAME"))
                        {
                            s = s.replaceAll("\\$FILENAME", newFileName);
                        }
                        sb.append(s);
                        isFirst[0] = false;
                    }
            );
            return sb.toString();
        }
        catch (IOException ex)
        {
            throw (ex);
        }
        finally
        {
            if (br != null)
                br.close();
        }

    }

    private void handleButtonNewFileSelected (JDialog directoryChooser, JComboBox<String> comboBoxDirectories, JTextField fileNameText)
    {
        String file = getNewFilename(comboBoxDirectories, fileNameText);
        try
        {
            setText(getNewFileTemplateText(fileNameText.getText()), "Enter new file " + file, file);
        }
        catch (IOException e)
        {
            setText("", "Failed to open template file, " + e.getMessage() + "\nEnter new file " + file, file);
        }
        directoryChooser.dispose();
        if (editHandler != null)
        {
            editHandler.handleNewFile(file);
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
        if (FgPreferences.get().getBoolean(PREF_GPG_USE_ASCII))
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
        JPanel jWrapperPanel = new JPanel(new BorderLayout());

        JPanel jButtonsPanel = new JPanel();

        GroupLayout gl = new GroupLayout(jButtonsPanel);
        jButtonsPanel.setLayout(gl);

        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        // Button: Cancel
        JButton cancelButton = new JButton("Cancel / Close");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(actionEvent ->
        {
            cancelEditing();
        });


        // Button: Save
        saveButton = new JButton("Save / Encrypt");
        saveButton.setMnemonic(KeyEvent.VK_S);
        saveButton.addActionListener(actionEvent -> doEncrypt());

        cbSkipPost = new JCheckBox("Skip post");
        cbSkipPost.setToolTipText("Do not execute the post command defined in the settings");
        cbSkipPost.setVisible(!FgPreferences.get().get(PREF_GPG_POST_COMMAND).isEmpty());
        cbSkipPost.setMnemonic('k');

        textFieldFilename = new JTextField();
        textFieldFilename.setEnabled(false);
        textFieldFilename.setVisible(false);

        JButton pbInsertTemplate = new JButton("Insert Template");
        pbInsertTemplate.setToolTipText("Insert template for password entry at cursor position");
        pbInsertTemplate.addActionListener((e) -> insertTemplateAtCaret());

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
                        .addComponent(pbInsertTemplate, 100, 100, 300)
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
                        .addComponent(pbInsertTemplate)
                        .addComponent(labelRID)
                        .addComponent(textFieldRID)
                        .addComponent(cbSkipPost)
                )
                .addComponent(passwordGeneratorPanel)
        );

        jWrapperPanel.add(jButtonsPanel, BorderLayout.CENTER);
        jWrapperPanel.add(this.tagsEditPanel, BorderLayout.SOUTH);
        return jWrapperPanel;
    }

    private void cancelEditing ()
    {
        if (!isModified() || OK_OPTION == JOptionPane.showConfirmDialog(parentWindow,
                "File is modified, close discarding changes?",
                "fortGnox Close Confirmation", OK_CANCEL_OPTION))
        {
            if (editPanel.isVisible())
            {
                editPanel.setVisible(false);
            }
            setSecretTextModified(false);
            if (editHandler != null)
            {
                editHandler.handleFinished();
            }
        }
    }

    private String getRecipient (File gpgFile)
    {
        // password store file with recipient ID in directory
        String ridFileName = FgPreferences.get().get(FgPreferences.PREF_GPG_RID_FILE);
        File ridFile = new File(gpgFile.getParent() + File.separator + ridFileName);
        if (ridFile.exists() && ridFileName != null && !ridFileName.isEmpty())
        {
            DebugWindow.dbg(DebugWindow.Category.GPG, "Found " + ridFile);
            String rid = FileUtils.getFileContent(ridFile.getAbsolutePath());
            if (rid != null)
            {
                return rid.trim();
            }
        }
        else
        {
            // Try to use RID from directories
            FileUtils.ParsedDirectories parsedDirectories = FileUtils.splitDirectoryString(FgPreferences.get().get(FgPreferences.PREF_SECRETDIRS));
            String rid = parsedDirectories.directoryRecipientMap.get(gpgFile.getParent());
            if (rid != null)
            {
                return rid;
            }

            // default ricipient ID
            return FgPreferences.get().get(PREF_GPG_DEFAULT_RID);
        }
        return null;
    }


    private void doEncrypt ()
    {
        boolean needTriggerPost = false;
        if (isTagsModified() && textFieldFilename.getText() != null)
        {
            try
            {
                TagsStore.saveTagsForFile(textFieldFilename.getText(), tagsText);
                needTriggerPost = true;
            }
            catch (IOException e)
            {
                setStatusText("Failed to save tags");
            }
        }

        if (isSecretTextModified()) {
            String rid = textFieldRID.getText();

            if (rid == null || rid.isEmpty())
            {
                JOptionPane.showMessageDialog(parentWindow, "Cannot determine recipient", "fortGnox WARNING", WARNING_MESSAGE);
                return;
            }

            this.recipientId = rid;
            String textToEncrypt = insertChangeTag(textArea.getText());
            fgGPGProcess.encrypt(textFieldFilename.getText(), textToEncrypt, rid, EditWindow.this);
            needTriggerPost = false;
        }

        if (needTriggerPost)
        {
            executePostCommand();
        }
    }


    static Pattern changedPattern = Pattern.compile("^(.*)(\\$Changed:.*\\$)(.*)$", Pattern.DOTALL);
    static Pattern inChangePattern = Pattern.compile("^\\$Changed:(.*)\\$$", Pattern.DOTALL);
    protected static String insertChangeTag (String text)
    {
        Matcher m = changedPattern.matcher(text);
        ZonedDateTime now = ZonedDateTime.now();
//        String nowString = DateTimeFormatter.ofPattern("dd-MM-yyyy - hh:mm").format(now);
        String nowString = now.toString();

        if (m.matches() && m.groupCount() == 3)
        {
            // Found existing Changed mark in the file, update it in any case
            String changedString = m.group(2);
            Matcher minner = inChangePattern.matcher(changedString);
            if (minner.matches())
            {
                String newText = m.group(1) + "$Changed: " + nowString + "$" +  m.group(3);
                return newText;
            }
            return text;
        }
        else
        {
            // No Changed mark yet in the file, add one on editing?
            if (FgPreferences.get().getBoolean(PREF_ADD_CHANGED_DATE_TIME))
            {
                return text + "\n" + "$Changed: " + nowString + "$";
            }
            else
            {
                return text;
            }
        }

    }

    public void executePostCommand ()
    {
        String postCommand = FgPreferences.get().get(PREF_GPG_POST_COMMAND);
        boolean doPostCommand = postCommand != null && !postCommand.isEmpty() && !cbSkipPost.isSelected();

        if (doPostCommand)
        {
            fgGPGProcess.command(postCommand, "", this);
            setText("", "Executing " + postCommand, "");
        }
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
                String postCommand = FgPreferences.get().get(PREF_GPG_POST_COMMAND);
                boolean doPostCommand = postCommand != null && !postCommand.isEmpty() && !cbSkipPost.isSelected();
                setSecretTextModified(false);
                String status = "Successfully encrypted " + filename + rid;
                if (doPostCommand)
                {
                    status += "\nExecuting post command " + postCommand;
                }
                setText("", status, filename);
                if (doPostCommand)
                {
                    fgGPGProcess.command(postCommand, filename, this);
                }
                else
                {
                    JOptionPane.showMessageDialog(parentWindow, status, "fortGnox INFO", JOptionPane.INFORMATION_MESSAGE);
                    if (editHandler != null)
                    {
                        editHandler.handleFinished();
                    }
                }
            }
            else
            {
                setStatusText("Failure encrypting " + filename + rid + ", " + err);
                JOptionPane.showMessageDialog(parentWindow, err, "fortGnox WARNING", JOptionPane.ERROR_MESSAGE);
                if (editHandler != null)
                {
                    editHandler.handleFinished();
                }
            }

            // refresh in any case, even if no new file has been added, don't hurt too much
            fgGPGProcess.rebuildSecretList();
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
                    textArea.setFont(new Font("monospaced", Font.PLAIN, (int)propertyChangeEvent.getNewValue()));
                }
                break;
            case PREF_GPG_POST_COMMAND:
                if (cbSkipPost != null)
                {
                    cbSkipPost.setVisible(!FgPreferences.get().get(PREF_GPG_POST_COMMAND).isEmpty());
                }
                break;
            case PREF_LOOK_AND_FEEL:
                LAFChooser.get().set((String)propertyChangeEvent.getNewValue(), editPanel);
                break;

        }
    }

    @Override
    public void handleGpgCommandResult (String out, String err, String filename, Object clientData, int exitCode)
    {
        if (exitCode != 0)
        {
            JOptionPane.showMessageDialog(parentWindow, "Output:" + out + " Error:" + err, "fortGnox POST", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            JOptionPane.showMessageDialog(parentWindow, out + err, "fortGnox POST", JOptionPane.INFORMATION_MESSAGE);
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
            setStatusText("Failure inserting password, " + e.toString());
        }
    }


    private void insertTemplateAtCaret ()
    {
        try
        {
            File file = new File(textFieldFilename.getText());
            String basename = file.getName();
            String templateText = getNewFileTemplateText(basename);
            textArea.getDocument().insertString(textArea.getCaretPosition(), templateText, null);
            textArea.requestFocus();
        }
        catch (BadLocationException | IOException e)
        {
            setStatusText("Failure inserting password, " + e.toString());
        }
    }

}
