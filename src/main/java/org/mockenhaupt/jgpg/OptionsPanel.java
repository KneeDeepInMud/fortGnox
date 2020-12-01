/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mockenhaupt.jgpg;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.event.KeyEvent;

import static org.mockenhaupt.jgpg.JgpgPreferences.*;

/**
 *
 * @author fmoc
 */
public class OptionsPanel extends javax.swing.JDialog
{
    /**
     * Creates new form OptionsPanel
     */
    public OptionsPanel (MainFrame parent)
    {
        super(parent, "JGPG Settings", true);
        initComponents();
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
        this.jFormattedTextareaClearTimeout.setText(String.format("%d", pa.getInt(PREF_CLEAR_SECONDS)));
        this.jFormattedTextPassClearTimeout.setText(String.format("%d", pa.getInt(PREF_PASSWORD_SECONDS))); //mainFrame.getPASSWORD_SECONDS()));
        this.jFormattedTextareaClipTimeout.setText(String.format("%d", pa.getInt(PREF_CLIP_SECONDS)));
        this.jFormattedTextFieldResetMaskButton.setText(String.format("%d", pa.getInt(PREF_RESET_MASK_BUTTON_SECONDS)));
        this.jFormattedTextTextAreaFontSize.setText(String.format("%d", pa.getInt(PREF_TEXTAREA_FONT_SIZE)));
        this.textFieldPassPatterns.setText(pa.get(PREF_PASSWORD_MASK_PATTERNS));
        this.comboBoxCharset.setSelectedItem(pa.get(PREF_CHARSET));
        this.textFieldUsernamePatterns.setText(pa.get(PREF_USERNAME_MASK_PATTERNS));
    }



    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        jTextGpgExe = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextGpgHome = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextSecretDirs = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jFormattedTextPassClearTimeout = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        jFormattedTextareaClearTimeout = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        jCheckBoxUsePassDialog = new javax.swing.JCheckBox();
        jCheckBoxPasswordShortcuts = new javax.swing.JCheckBox();
        jCheckboxReloadAgent = new javax.swing.JCheckBox();
        jCheckboxMastFirstLine = new javax.swing.JCheckBox();
        jButtonSave = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();

        jFormattedTextareaClipTimeout = new javax.swing.JFormattedTextField();
        jFormattedTextFieldResetMaskButton = new javax.swing.JFormattedTextField();
        jFormattedTextTextAreaFontSize = new javax.swing.JFormattedTextField();

        getContentPane().setLayout(new java.awt.GridLayout(18, 2, 10, 1));

        jLabel1.setText("GPG Executable");
        getContentPane().add(jLabel1);
        jTextGpgExe.setMinimumSize(new java.awt.Dimension(300, 20));
        jTextGpgExe.setPreferredSize(new java.awt.Dimension(300, 20));
        getContentPane().add(jTextGpgExe);

        JLabel jLabelGpgConf = new JLabel("GPGCONF Executable");
        getContentPane().add(jLabelGpgConf);
        jTexfFieldGpgConf = new JTextField();
        jTexfFieldGpgConf.setMinimumSize(new java.awt.Dimension(300, 20));
        jTexfFieldGpgConf.setPreferredSize(new java.awt.Dimension(300, 20));
        getContentPane().add(jTexfFieldGpgConf);

        JLabel jLabelOpenBrowserCommand = new JLabel("Browser launch command");
        getContentPane().add(jLabelOpenBrowserCommand);
        jTexfFieldBrowserOpen = new JTextField();
        jTexfFieldBrowserOpen.setMinimumSize(new java.awt.Dimension(300, 20));
        jTexfFieldBrowserOpen.setPreferredSize(new java.awt.Dimension(300, 20));
        getContentPane().add(jTexfFieldBrowserOpen);


        jLabel3.setText("GPG Home");
        getContentPane().add(jLabel3);
        getContentPane().add(jTextGpgHome);

        jLabel2.setText("Data directories (separate with \";\")");
        getContentPane().add(jLabel2);
        getContentPane().add(jTextSecretDirs);

        jLabel5.setText("Password clear timeout [s]");
        getContentPane().add(jLabel5);

        jFormattedTextPassClearTimeout.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        getContentPane().add(jFormattedTextPassClearTimeout);

        jLabel6.setText("Textarea clear timeout [s]");
        getContentPane().add(jLabel6);
        jFormattedTextareaClearTimeout.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        getContentPane().add(jFormattedTextareaClearTimeout);

        JLabel jLabel7 = new JLabel("Clipboard flush timeout [s]");
        getContentPane().add(jLabel7);
        jFormattedTextareaClipTimeout.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        getContentPane().add(jFormattedTextareaClipTimeout);

        JLabel jLabel9 = new JLabel("Restore mask password button timeout [s] (< 0 to disable)");
        getContentPane().add(jLabel9);
        jFormattedTextFieldResetMaskButton.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        getContentPane().add(jFormattedTextFieldResetMaskButton);



        JLabel labelPassPatterns = new JLabel("Password patterns (\"|\" separated)");
        textFieldPassPatterns = new JTextField();
        getContentPane().add(labelPassPatterns);
        getContentPane().add(textFieldPassPatterns);

        JLabel labelCharset = new JLabel("Character Set");
        String[] charsets = {"ISO-8859-15", "ISO-8859-1", "UTF-8"};
        comboBoxCharset = new JComboBox(charsets);
        getContentPane().add(labelCharset);
        getContentPane().add(comboBoxCharset);

        JLabel jLabel8 = new JLabel("Font size (text area)");
        getContentPane().add(jLabel8);
        jFormattedTextTextAreaFontSize.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        getContentPane().add(jFormattedTextTextAreaFontSize);



        JLabel labelUsernamePatterns = new JLabel("Username patterns (\"|\" separated)");
        textFieldUsernamePatterns = new JTextField();
        getContentPane().add(labelUsernamePatterns);
        getContentPane().add(textFieldUsernamePatterns);


        getContentPane().add(jLabel4);
        jCheckBoxUsePassDialog.setText("Show password dialog (might not be required with GPG agent)");
        getContentPane().add(jCheckBoxUsePassDialog);

        getContentPane().add(new JLabel());
        jCheckBoxPasswordShortcuts.setText("Show password shortcut bar");
        getContentPane().add(jCheckBoxPasswordShortcuts);

        getContentPane().add(new JLabel());
        jCheckboxReloadAgent.setText("Allow flushing password from GPG agent");
        getContentPane().add(jCheckboxReloadAgent);

        getContentPane().add(new JLabel());
        jCheckboxMastFirstLine.setText("Mask the first line on program start");
        getContentPane().add(jCheckboxMastFirstLine);

        jButtonSave.setText("Save");
        jButtonSave.setMnemonic(KeyEvent.VK_S);
        jButtonSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSaveActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSave);

        jButtonClose.setText("Close");
        jButtonSave.setMnemonic(KeyEvent.VK_L);
        jButtonClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonCloseActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonClose);

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
        JgpgPreferences.get().put(PREF_CHARSET, comboBoxCharset.getSelectedItem());
        setVisible(false);
    }


    @Override
    public void setVisible(boolean b) {
        initPreferences();
        super.setVisible(b);
    }

    // Variables declaration - do not modify
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBoxUsePassDialog;
    private javax.swing.JCheckBox jCheckBoxPasswordShortcuts;
    private javax.swing.JCheckBox jCheckboxReloadAgent;
    private javax.swing.JCheckBox jCheckboxMastFirstLine;
    private javax.swing.JFormattedTextField jFormattedTextPassClearTimeout;
    private javax.swing.JFormattedTextField jFormattedTextareaClearTimeout;
    private javax.swing.JFormattedTextField jFormattedTextareaClipTimeout;
    private javax.swing.JFormattedTextField jFormattedTextFieldResetMaskButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField jTextGpgExe;
    private JTextField jTexfFieldGpgConf;
    private JTextField jTexfFieldBrowserOpen;
    private JTextField textFieldPassPatterns;
    private JComboBox<String> comboBoxCharset;
    private JTextField textFieldUsernamePatterns;
    private javax.swing.JTextField jTextGpgHome;
    private javax.swing.JTextField jTextSecretDirs;
    private javax.swing.JFormattedTextField jFormattedTextTextAreaFontSize;
    // End of variables declaration
}
