/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mockenhaupt.jgpg;


import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_CHARSET;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_CLEAR_SECONDS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_CLIP_SECONDS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_FAVORITES_SHOW_COUNT;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_FILTER_FAVORITES;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_GPGCONF_COMMAND;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_GPG_COMMAND;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_GPG_DEFAULT_RID;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_GPG_HOMEDIR;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_GPG_RID_FILE;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_GPG_USE_ASCII;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_MASK_FIRST_LINE;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_NUMBER_FAVORITES;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_PASSWORD_MASK_PATTERNS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_PASSWORD_SECONDS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_RESET_MASK_BUTTON_SECONDS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_SECRETDIRS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_SHOW_PASSWORD_SHORTCUT_BAR;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_TEXTAREA_FONT_SIZE;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_URL_OPEN_COMMAND;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_USERNAME_MASK_PATTERNS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_USE_FAVORITES;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_USE_GPG_AGENT;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_USE_PASS_DIALOG;

/**
 *
 * @author fmoc
 */
public class OptionsPanel extends javax.swing.JDialog
{

    private JTabbedPane jTabbedPane = new JTabbedPane();
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JPanel gpgPanel;


    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBoxUsePassDialog;
    private javax.swing.JCheckBox jCheckBoxPasswordShortcuts;
    private javax.swing.JCheckBox jCheckboxReloadAgent;
    private javax.swing.JCheckBox jCheckboxMastFirstLine;
    private javax.swing.JCheckBox jCheckBoxUseFavorites;
    private javax.swing.JCheckBox jCheckBoxGpgUseAsciiFormat;
    private javax.swing.JCheckBox jCheckBoxFilterFavorites;
    private javax.swing.JCheckBox jCheckBoxShowFavoritesCount;
    private javax.swing.JCheckBox jCheckBoxShowDebugWindow;
    private javax.swing.JFormattedTextField jFormattedTextPassClearTimeout;
    private javax.swing.JFormattedTextField jFormattedTextareaClearTimeout;
    private javax.swing.JFormattedTextField jFormattedTextareaClipTimeout;
    private javax.swing.JFormattedTextField jFormattedTextFieldResetMaskButton;
    private javax.swing.JFormattedTextField jFormattedTextFieldNumberFavorites;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField jTextGpgExe;
    private JTextField jTexfFieldGpgConf;
    private JTextField jTexfFieldBrowserOpen;
    private JTextField jTexfFieldGpgRIDFile;
    private JTextField jTexfFieldGpgDefaultRID;
    private JTextField textFieldPassPatterns;
    private JComboBox<String> comboBoxCharset;
    private JTextField textFieldUsernamePatterns;
    private javax.swing.JTextField jTextGpgHome;
    private javax.swing.JTextField jTextSecretDirs;
    private javax.swing.JFormattedTextField jFormattedTextTextAreaFontSize;
    // End of variables declaration


    public OptionsPanel (MainFrame parent)
    {
        super(parent, "JGPG Settings", true);
        optionsPanel = new JPanel();
        buttonPanel = new JPanel(new FlowLayout());
        jTabbedPane.add("General", optionsPanel);
        jTabbedPane.add("GPG", gpgPanel = new JPanel());
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jTabbedPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        initComponents();
        initGpgSettings();
    }

    void initPreferences ()
    {
        PreferencesAccess pa = JgpgPreferences.get();
        this.jTextGpgExe.setText(pa.get(PREF_GPG_COMMAND));
        this.jTextGpgHome.setText(pa.get(PREF_GPG_HOMEDIR));
        this.jTexfFieldGpgConf.setText(  pa.get(PREF_GPGCONF_COMMAND)  );//jgpgProcess.getPrefGpgConfCommand());
        this.jTexfFieldBrowserOpen.setText(  pa.get(PREF_URL_OPEN_COMMAND)  );//jgpgProcess.getPrefGpgConfCommand());
        this.jTextSecretDirs.setText(pa.get(PREF_SECRETDIRS));//jgpgProcess.getPrefSecretDirsString());
        this.jCheckBoxUsePassDialog.setSelected(pa.getBoolean(PREF_USE_PASS_DIALOG)); // jgpgProcess.isPrefUsePasswordDialog());
        this.jCheckBoxPasswordShortcuts.setSelected(pa.getBoolean(PREF_SHOW_PASSWORD_SHORTCUT_BAR)); // jgpgProcess.isPrefUsePasswordDialog());
        this.jCheckboxReloadAgent.setSelected(pa.getBoolean(PREF_USE_GPG_AGENT));//jgpgProcess.isPrefConnectToGpgAgent());
        this.jCheckboxMastFirstLine.setSelected(pa.getBoolean(PREF_MASK_FIRST_LINE));//jgpgProcess.isPrefConnectToGpgAgent());
        this.jCheckBoxUseFavorites.setSelected(pa.getBoolean(PREF_USE_FAVORITES));//jgpgProcess.isPrefConnectToGpgAgent());
        this.jCheckBoxFilterFavorites.setSelected(pa.getBoolean(PREF_FILTER_FAVORITES));
        this.jCheckBoxShowFavoritesCount.setSelected(pa.getBoolean(PREF_FAVORITES_SHOW_COUNT));//jgpgProcess.isPrefConnectToGpgAgent());
        this.jFormattedTextareaClearTimeout.setText(String.format("%d", pa.getInt(PREF_CLEAR_SECONDS)));
        this.jFormattedTextPassClearTimeout.setText(String.format("%d", pa.getInt(PREF_PASSWORD_SECONDS))); //mainFrame.getPASSWORD_SECONDS()));
        this.jFormattedTextareaClipTimeout.setText(String.format("%d", pa.getInt(PREF_CLIP_SECONDS)));
        this.jFormattedTextFieldResetMaskButton.setText(String.format("%d", pa.getInt(PREF_RESET_MASK_BUTTON_SECONDS)));
        this.jFormattedTextFieldNumberFavorites.setText(String.format("%d", pa.getInt(PREF_NUMBER_FAVORITES)));
        this.jFormattedTextTextAreaFontSize.setText(String.format("%d", pa.getInt(PREF_TEXTAREA_FONT_SIZE)));
        this.textFieldPassPatterns.setText(pa.get(PREF_PASSWORD_MASK_PATTERNS));
        this.comboBoxCharset.setSelectedItem(pa.get(PREF_CHARSET));
        this.textFieldUsernamePatterns.setText(pa.get(PREF_USERNAME_MASK_PATTERNS));
        this.jTexfFieldGpgRIDFile.setText(pa.get(PREF_GPG_RID_FILE, ".gpg-id"));
        this.jTexfFieldGpgDefaultRID.setText(pa.get(PREF_GPG_DEFAULT_RID, ""));
        this.jCheckBoxGpgUseAsciiFormat.setSelected(pa.getBoolean(PREF_GPG_USE_ASCII));//jgpgProcess.isPrefConnectToGpgAgent());

    }


    private void initGpgSettings ()
    {
        int col = 2;
        int row = 20;
        gpgPanel.setLayout(new java.awt.GridLayout(row, col, 4, 1));

        gpgPanel.add(new JLabel("GPG Executable"));
        gpgPanel.add(jTextGpgExe);

        JLabel jLabelGpgConf = new JLabel("GPGCONF Executable");
        gpgPanel.add(jLabelGpgConf);
        jTexfFieldGpgConf = new JTextField();
        gpgPanel.add(jTexfFieldGpgConf);

        jLabel3.setText("GPG Home");
        gpgPanel.add(jLabel3);
        gpgPanel.add(jTextGpgHome);

        jLabel2.setText("Data directories (separate with \";\")");
        gpgPanel.add(jLabel2);
        gpgPanel.add(jTextSecretDirs);


        gpgPanel.add(new JLabel("Recipient ID hint file"));
        jTexfFieldGpgRIDFile = new JTextField();
        gpgPanel.add(jTexfFieldGpgRIDFile);

        gpgPanel.add(new JLabel("Default recipient ID for password encryption"));
        jTexfFieldGpgDefaultRID = new JTextField();
        gpgPanel.add(jTexfFieldGpgDefaultRID);

        jCheckBoxGpgUseAsciiFormat.setText("Encrypt in ASCII format");
        gpgPanel.add(jCheckBoxGpgUseAsciiFormat);

        for (int i = gpgPanel.getComponentCount(); i < row * col; i++) {
            JLabel l = new JLabel(" ");
//            l.setBorder(BorderFactory.createLineBorder(Color.red));
            gpgPanel.add(l);
        }
    }



    private void initComponents()
    {
        jTextGpgExe = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextGpgHome = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextSecretDirs = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jFormattedTextPassClearTimeout = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        jFormattedTextareaClearTimeout = new javax.swing.JFormattedTextField();
        jCheckBoxUsePassDialog = new javax.swing.JCheckBox();
        jCheckBoxPasswordShortcuts = new javax.swing.JCheckBox();
        jCheckboxReloadAgent = new javax.swing.JCheckBox();
        jCheckboxMastFirstLine = new javax.swing.JCheckBox();
        jCheckBoxUseFavorites = new javax.swing.JCheckBox();
        jCheckBoxGpgUseAsciiFormat = new javax.swing.JCheckBox();
        jCheckBoxFilterFavorites = new javax.swing.JCheckBox();
        jCheckBoxShowFavoritesCount = new javax.swing.JCheckBox();
        jButtonSave = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();

        jFormattedTextareaClipTimeout = new javax.swing.JFormattedTextField();
        jFormattedTextFieldResetMaskButton = new javax.swing.JFormattedTextField();
        jFormattedTextFieldNumberFavorites = new javax.swing.JFormattedTextField();
        jFormattedTextTextAreaFontSize = new javax.swing.JFormattedTextField();

        int row = 19;
        int col = 2;
        optionsPanel.setLayout(new java.awt.GridLayout(row, col, 10, 1));

        JLabel jLabelOpenBrowserCommand = new JLabel("Browser launch command");
        optionsPanel.add(jLabelOpenBrowserCommand);
        jTexfFieldBrowserOpen = new JTextField();
        jTexfFieldBrowserOpen.setMinimumSize(new java.awt.Dimension(500, 20));
        jTexfFieldBrowserOpen.setPreferredSize(new java.awt.Dimension(500, 20));
        optionsPanel.add(jTexfFieldBrowserOpen);


        jLabel5.setText("Password clear timeout [s]");
        optionsPanel.add(jLabel5);

        jFormattedTextPassClearTimeout.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        optionsPanel.add(jFormattedTextPassClearTimeout);

        jLabel6.setText("Textarea clear timeout [s]");
        optionsPanel.add(jLabel6);
        jFormattedTextareaClearTimeout.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        optionsPanel.add(jFormattedTextareaClearTimeout);

        JLabel jLabel7 = new JLabel("Clipboard flush timeout [s]");
        optionsPanel.add(jLabel7);
        jFormattedTextareaClipTimeout.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        optionsPanel.add(jFormattedTextareaClipTimeout);

        JLabel jLabel9 = new JLabel("Restore mask password button timeout [s] (< 0 to disable)");
        optionsPanel.add(jLabel9);
        jFormattedTextFieldResetMaskButton.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        optionsPanel.add(jFormattedTextFieldResetMaskButton);


        JLabel jLabel91 = new JLabel("Number favorites (< 0 unlimited)");
        optionsPanel.add(jLabel91);
        jFormattedTextFieldNumberFavorites.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        optionsPanel.add(jFormattedTextFieldNumberFavorites);



        JLabel labelPassPatterns = new JLabel("Password patterns (\"|\" separated)");
        textFieldPassPatterns = new JTextField();
        optionsPanel.add(labelPassPatterns);
        optionsPanel.add(textFieldPassPatterns);

        JLabel labelCharset = new JLabel("Character Set");
        String[] charsets = {"ISO-8859-15", "ISO-8859-1", "UTF-8"};
        comboBoxCharset = new JComboBox(charsets);
        optionsPanel.add(labelCharset);
        optionsPanel.add(comboBoxCharset);

        JLabel jLabel8 = new JLabel("Font size (text area)");
        optionsPanel.add(jLabel8);
        jFormattedTextTextAreaFontSize.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        optionsPanel.add(jFormattedTextTextAreaFontSize);



        JLabel labelUsernamePatterns = new JLabel("Username patterns (\"|\" separated)");
        textFieldUsernamePatterns = new JTextField();
        optionsPanel.add(labelUsernamePatterns);
        optionsPanel.add(textFieldUsernamePatterns);


        jCheckBoxUsePassDialog.setText("Show password dialog (might not be required with GPG agent)");
        optionsPanel.add(jCheckBoxUsePassDialog);

        jCheckBoxPasswordShortcuts.setText("Show password shortcut bar");
        optionsPanel.add(jCheckBoxPasswordShortcuts);

        jCheckboxReloadAgent.setText("Allow flushing password from GPG agent");
        optionsPanel.add(jCheckboxReloadAgent);

        jCheckboxMastFirstLine.setText("Mask the first line on program start");
        optionsPanel.add(jCheckboxMastFirstLine);

        jCheckBoxUseFavorites.setText("Show list of favorites");
        optionsPanel.add(jCheckBoxUseFavorites);

        jCheckBoxFilterFavorites.setText("Filter favorites in addition to passwords");
        optionsPanel.add(jCheckBoxFilterFavorites);

        jCheckBoxShowFavoritesCount.setText("Show count of individual favorite");
        optionsPanel.add(jCheckBoxShowFavoritesCount);


        // show/hide debug window
        optionsPanel.add(jCheckBoxShowDebugWindow = new JCheckBox("Show debug window"));
        DebugWindow.get().addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange (PropertyChangeEvent evt)
            {
                if (DebugWindow.PROP_VISIBLE.equals(evt.getPropertyName()))
                {
                    jCheckBoxShowDebugWindow.setSelected((Boolean) evt.getNewValue());
                }
            }
        });
        jCheckBoxShowDebugWindow.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                DebugWindow.get().setWindowVisible(jCheckBoxShowDebugWindow.isSelected());

            }
        });



        for (int i = optionsPanel.getComponentCount(); i < row * col; i++) {
            JLabel l = new JLabel(" ");
//            l.setBorder(BorderFactory.createLineBorder(Color.red));
            optionsPanel.add(l);
        }


        // ==============================================================

        jButtonSave.setText("Save");
        jButtonSave.setMnemonic(KeyEvent.VK_S);
        jButtonSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonClose.setText("Close");
        jButtonSave.setMnemonic(KeyEvent.VK_L);
        jButtonClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonCloseActionPerformed(evt);
            }
        });

        buttonPanel.add(jButtonSave);
        buttonPanel.add(jButtonClose);

        pack();
    }

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt)
    {
        setVisible(false);
    }

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt)
    {

        Integer clearTimeout;
        Integer clipTimeout;
        Integer passTimeout;
        Integer resetMaskButtonTimeout;
        Integer numberFavorites;
        Integer textAreaFontSize;

        try
        {
            textAreaFontSize = Integer.parseInt(jFormattedTextTextAreaFontSize.getText());
            textAreaFontSize = Math.max(textAreaFontSize, 8);
            textAreaFontSize = Math.min(textAreaFontSize, 60);
            JgpgPreferences.get().put(PREF_TEXTAREA_FONT_SIZE, textAreaFontSize);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }


        try
        {
            clipTimeout = Integer.parseInt(jFormattedTextareaClipTimeout.getText());
            JgpgPreferences.get().put(PREF_CLIP_SECONDS, clipTimeout);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }


        try
        {
            resetMaskButtonTimeout = Integer.parseInt(jFormattedTextFieldResetMaskButton.getText());
            JgpgPreferences.get().put(PREF_RESET_MASK_BUTTON_SECONDS, resetMaskButtonTimeout);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }

        try
        {
            numberFavorites = Integer.parseInt(jFormattedTextFieldNumberFavorites.getText());
            JgpgPreferences.get().put(PREF_NUMBER_FAVORITES, numberFavorites);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }

        try
        {
            clearTimeout = Integer.parseInt(jFormattedTextareaClearTimeout.getText());
            JgpgPreferences.get().put(PREF_CLEAR_SECONDS, clearTimeout);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }
        try
        {
            passTimeout = Integer.parseInt(jFormattedTextPassClearTimeout.getText());
            JgpgPreferences.get().put(PREF_PASSWORD_SECONDS, passTimeout);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }

        JgpgPreferences.get().put(PREF_PASSWORD_MASK_PATTERNS, textFieldPassPatterns.getText());
        JgpgPreferences.get().put(PREF_USERNAME_MASK_PATTERNS, textFieldUsernamePatterns.getText());
        JgpgPreferences.get().put(PREF_GPG_COMMAND, jTextGpgExe.getText());
        JgpgPreferences.get().put(JgpgPreferences.PREF_SECRETDIRS, jTextSecretDirs.getText());
        JgpgPreferences.get().put(JgpgPreferences.PREF_GPG_HOMEDIR, jTextGpgHome.getText());
        JgpgPreferences.get().put(JgpgPreferences.PREF_GPGCONF_COMMAND, jTexfFieldGpgConf.getText());
        JgpgPreferences.get().put(PREF_URL_OPEN_COMMAND, jTexfFieldBrowserOpen.getText());
        JgpgPreferences.get().put(JgpgPreferences.PREF_USE_PASS_DIALOG, jCheckBoxUsePassDialog.isSelected());
        JgpgPreferences.get().put(PREF_SHOW_PASSWORD_SHORTCUT_BAR, jCheckBoxPasswordShortcuts.isSelected());
        JgpgPreferences.get().put(JgpgPreferences.PREF_USE_GPG_AGENT, jCheckboxReloadAgent.isSelected());
        JgpgPreferences.get().put(PREF_MASK_FIRST_LINE, jCheckboxMastFirstLine.isSelected());
        JgpgPreferences.get().put(PREF_USE_FAVORITES, jCheckBoxUseFavorites.isSelected());
        JgpgPreferences.get().put(PREF_FILTER_FAVORITES, jCheckBoxFilterFavorites.isSelected());
        JgpgPreferences.get().put(PREF_FAVORITES_SHOW_COUNT, jCheckBoxShowFavoritesCount.isSelected());
        JgpgPreferences.get().put(PREF_CHARSET, comboBoxCharset.getSelectedItem());
        JgpgPreferences.get().put(PREF_GPG_RID_FILE, jTexfFieldGpgRIDFile.getText());
        JgpgPreferences.get().put(PREF_GPG_DEFAULT_RID, jTexfFieldGpgDefaultRID.getText());
        JgpgPreferences.get().put(PREF_GPG_USE_ASCII, jCheckBoxGpgUseAsciiFormat.isSelected());

        setVisible(false);
    }


    @Override
    public void setVisible(boolean b) {
        initPreferences();
        super.setVisible(b);
    }


}
