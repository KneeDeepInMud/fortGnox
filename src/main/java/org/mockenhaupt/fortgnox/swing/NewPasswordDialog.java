package org.mockenhaupt.fortgnox.swing;

import org.mockenhaupt.fortgnox.FgPreferences;
import org.mockenhaupt.fortgnox.misc.FileUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

import static org.mockenhaupt.fortgnox.FgPreferences.PREF_SECRETDIRS;

/**
 * Dialog extracted from EditWindow.showNew().
 * Creates UI to select directory and filename for a new password file,
 * and notifies a listener upon creation.
 */
public class NewPasswordDialog extends JDialog {
    public interface Listener {
        void onCreate(String fullPath, String fileNameOnly);
    }

    private final JComboBox<String> comboBoxDirectories = new JComboBox<>();
    private final JTextField fileNameText = new JTextField();
    private final JTextField fileNameResulting = new JTextField("");
    private final JButton buttonCreate = new JButton("Create New");

    public NewPasswordDialog(JFrame parent, Listener listener) {
        super(parent, "fortgnox New Password", true);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel directoryChooserPanel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(directoryChooserPanel);
        directoryChooserPanel.setLayout(groupLayout);
        add(directoryChooserPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(650, 200));
        setMinimumSize(getPreferredSize());

        FileUtils.ParsedDirectories parsedDirectories =
                FileUtils.splitDirectoryString(FgPreferences.get().get(PREF_SECRETDIRS));
        comboBoxDirectories.setMaximumSize(new Dimension(500, 35));

        comboBoxDirectories.setModel(new DefaultComboBoxModel<String>() {
            @Override public int getSize() { return parsedDirectories.directoryList.size(); }
            @Override public String getElementAt(int index) { return parsedDirectories.directoryList.get(index); }
        });

        comboBoxDirectories.addActionListener(actionEvent -> updateFilenamePreview());
        if (comboBoxDirectories.getModel().getSize() > 0) {
            comboBoxDirectories.setSelectedIndex(0);
        }

        fileNameText.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateFilenamePreview(); }
            @Override public void removeUpdate(DocumentEvent e) { updateFilenamePreview(); }
            @Override public void changedUpdate(DocumentEvent e) { updateFilenamePreview(); }
        });
        fileNameResulting.setEnabled(false);
        fileNameText.setMaximumSize(new Dimension(500, 35));

        JLabel dirNameLabel = new JLabel("Directory", SwingConstants.RIGHT);
        JLabel fileNameLabel = new JLabel("New filename:", SwingConstants.RIGHT);

        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);

        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
                .addGroup(groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup()
                                .addComponent(dirNameLabel)
                                .addComponent(fileNameLabel))
                        .addGroup(groupLayout.createParallelGroup()
                                .addComponent(comboBoxDirectories)
                                .addComponent(fileNameText)))
                .addComponent(fileNameResulting));

        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup().addComponent(dirNameLabel).addComponent(comboBoxDirectories))
                .addGroup(groupLayout.createParallelGroup().addComponent(fileNameLabel).addComponent(fileNameText))
                .addComponent(fileNameResulting));

        JPanel buttonPanel = new JPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        buttonCreate.setMnemonic(KeyEvent.VK_N);
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.setMnemonic(KeyEvent.VK_C);
        buttonPanel.add(buttonCreate);
        buttonPanel.add(buttonCancel);

        buttonCancel.addActionListener(actionEvent -> dispose());
        buttonCreate.addActionListener(actionEvent -> {
            String full = getNewFilename();
            if (listener != null) listener.onCreate(full, fileNameText.getText());
            dispose();
        });

        fileNameText.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent documentEvent) { handleChange(); }
            @Override public void removeUpdate(DocumentEvent documentEvent) { handleChange(); }
            @Override public void changedUpdate(DocumentEvent documentEvent) { handleChange(); }
            void handleChange() {
                String ftest = getNewFilename();
                File ftestFile = new File(ftest);
                buttonCreate.setEnabled(!ftestFile.exists() && !fileNameText.getText().isEmpty());
            }
        });
        buttonCreate.setEnabled(false);

        getRootPane().registerKeyboardAction(e -> {
                    if (buttonCreate.isEnabled()) {
                        String full = getNewFilename();
                        if (listener != null) listener.onCreate(full, fileNameText.getText());
                        dispose();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        Point location = MouseInfo.getPointerInfo().getLocation();
        setLocation(location);
        SwingUtilities.invokeLater(() -> fileNameText.requestFocus());
    }

    private void updateFilenamePreview() {
        if (fileNameText.getText().isEmpty()) {
            fileNameResulting.setText("");
        } else {
            fileNameResulting.setText(comboBoxDirectories.getSelectedItem() + File.separator + fileNameText.getText() + getSuffix());
        }
    }

    private String getNewFilename() {
        return comboBoxDirectories.getSelectedItem() + File.separator + fileNameText.getText() + getSuffix();
    }

    private String getSuffix() {
        return org.mockenhaupt.fortgnox.misc.SuffixUtil.getGpgSuffix();
    }
}
