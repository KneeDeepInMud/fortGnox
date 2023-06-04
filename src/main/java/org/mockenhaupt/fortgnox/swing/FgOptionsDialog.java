/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mockenhaupt.fortgnox.swing;


import org.mockenhaupt.fortgnox.DebugWindow;
import org.mockenhaupt.fortgnox.FgPreferences;
import org.mockenhaupt.fortgnox.MainFrame;
import org.mockenhaupt.fortgnox.PreferencesAccess;
import org.mockenhaupt.fortgnox.misc.PasswordCharacterPool;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockenhaupt.fortgnox.FgPreferences.PREF_ADD_CHANGED_DATE_TIME;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_CHARSET;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_CLEAR_SECONDS;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_CLIP_SECONDS;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_FAVORITES_MIN_HIT_COUNT;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_FAVORITES_SHOW_COUNT;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_FILTER_FAVORITES;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPGCONF_COMMAND;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_COMMAND;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_DEFAULT_RID;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_HOMEDIR;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_PASS_CHARPOOL_DIGIT;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_PASS_CHARPOOL_LOWER;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_PASS_CHARPOOL_SPECIAL;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_PASS_CHARPOOL_UPPER;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_POST_COMMAND;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_RID_FILE;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_USE_ASCII;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_HISTORY_SIZE;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_NEW_TEMPLATE;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_SECRETLIST_FONT_SIZE;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_LOOK_AND_FEEL;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_MASK_FIRST_LINE;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_NUMBER_FAVORITES;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_PASSWORD_MASK_PATTERNS;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_PASSWORD_SECONDS;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_RESET_MASK_BUTTON_SECONDS;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_SECRETDIRS;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_SHOW_PASSWORD_SHORTCUT_BAR;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_SHOW_SEARCH_TAGS;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_SHOW_TB_BUTTON_TEXT;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_TEXTAREA_FONT_SIZE;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_URL_OPEN_COMMAND;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_USERNAME_MASK_PATTERNS;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_USE_FAVORITES;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_USE_GPG_AGENT;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_USE_PASS_DIALOG;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_USE_SEARCH_TAGS;

/**
 *
 * @author fmoc
 */
public class FgOptionsDialog extends javax.swing.JDialog
{

    private JTabbedPane jTabbedPane = new JTabbedPane();
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JPanel gpgPanel;
    private JPanel passwordGeneratorPanel;


    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonSave;
    private JCheckBoxPersistent jCheckBoxUsePassDialog;
    private javax.swing.JCheckBox jCheckBoxPasswordShortcuts;
    private javax.swing.JCheckBox jCheckBoxShowTbButtonText;
    private javax.swing.JCheckBox jCheckboxReloadAgent;
    private javax.swing.JCheckBox jCheckboxMastFirstLine;
    private javax.swing.JCheckBox jCheckBoxUseFavorites;
    private javax.swing.JCheckBox jCheckBoxGpgUseAsciiFormat;
    private javax.swing.JCheckBox jCheckBoxFilterFavorites;
    private JCheckBoxPersistent jCheckBoxShowFavoritesCount;
    private JCheckBoxPersistent jCheckBoxUseSearchTags;
    private JCheckBoxPersistent jCheckBoxShowSearchTags;
    private JCheckBoxPersistent jCheckBoxAddChangedDateTime;
    private javax.swing.JCheckBox jCheckBoxShowDebugWindow;
    private javax.swing.JFormattedTextField jFormattedTextPassClearTimeout;
    private javax.swing.JFormattedTextField jFormattedTextareaClearTimeout;
    private javax.swing.JFormattedTextField jFormattedTextareaClipTimeout;
    private javax.swing.JFormattedTextField jFormattedTextFieldResetMaskButton;
    private javax.swing.JFormattedTextField jFormattedTextFieldNumberFavorites;
    private javax.swing.JFormattedTextField jFormattedTextFieldMinFavoriteCount;

    private JFormattedTextField textFieldDigits;
    private JTextField textFieldCharsUpper;
    private JTextField textFieldCharsLower;
    private JTextField textFieldSpecial;

    private javax.swing.JLabel labelDataDirs;
    private javax.swing.JLabel labelGpgHome;
    private javax.swing.JLabel jLabelPassClearTimeout;
    private javax.swing.JLabel jLabelTextClearTimeout;
    private javax.swing.JTextField jTextGpgExe;
    private javax.swing.JTextField jTextGpgPostCommand;
    private javax.swing.JTextField jTextNewFileTemplate;
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
    private javax.swing.JFormattedTextField jFormattedTextHistorySize;
    private javax.swing.JFormattedTextField jFormattedTextSecretListFontSize;
    private JComboBox<String> lookAndFeelInfoJComboBox;
    private JFrame parent;

    public FgOptionsDialog (MainFrame parent)
    {
        super(parent, "fortGnox Settings", true);
        this.parent = parent;
        this.setPreferredSize(new Dimension(960, 740));
        optionsPanel = new JPanel();
        gpgPanel = new JPanel();
        passwordGeneratorPanel = new JPanel();
        buttonPanel = new JPanel(new FlowLayout());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jTabbedPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        initComponents();
        initGpgSettings();
        initPasswordPanel();

        JScrollPane scrollPaneGeneral = new JScrollPane(optionsPanel);
        jTabbedPane.add("General", scrollPaneGeneral);
        JScrollPane scrollPaneGpg = new JScrollPane(gpgPanel);
        jTabbedPane.add("GPG", scrollPaneGpg);
        JScrollPane scrollPanePasswordGenerator = new JScrollPane(passwordGeneratorPanel);
        jTabbedPane.add("Password generator characters", scrollPanePasswordGenerator);
    }

    private void initPasswordPanel ()
    {
        GroupLayout gl = new GroupLayout(passwordGeneratorPanel);
        passwordGeneratorPanel.setLayout(gl);

        textFieldDigits = new JFormattedTextField();
        textFieldDigits.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        JLabel jLabelDigits = new JLabel("Digits");

        textFieldCharsUpper = new JFormattedTextField();
        JLabel jLabelCharacters = new JLabel("Uppcarcase Characters");

        textFieldCharsLower = new JFormattedTextField();
        JLabel jLabelCharactersLower = new JLabel("Lowercase Characters");

        textFieldSpecial = new JFormattedTextField();
        JLabel jLabelSpecialChars = new JLabel("Special Characters");

        JButton buttonReset = new JButton("Restore default character pools");
        buttonReset.addActionListener(e ->
        {
            resetToDefaultCharacterPools(textFieldDigits, textFieldCharsUpper, textFieldCharsLower, textFieldSpecial);
        });

        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);


        JLabel labelInfoMessage = new JLabel();
        labelInfoMessage.setText("<html>Random characters from these sets will be used in the password generator<hr></html>");

        gl.setHorizontalGroup(
                gl.createParallelGroup()
                        .addComponent(labelInfoMessage)
                        .addGroup(
                                gl.createSequentialGroup().addGroup(
                                        gl.createParallelGroup()
                                                .addComponent(jLabelDigits)
                                                .addComponent(jLabelCharacters)
                                                .addComponent(jLabelCharactersLower)
                                                .addComponent(jLabelSpecialChars)
                                )
                                        .addGroup(gl.createParallelGroup()
                                                .addComponent(textFieldDigits)
                                                .addComponent(textFieldCharsUpper)
                                                .addComponent(textFieldCharsLower)
                                                .addComponent(textFieldSpecial)
                                        )
                        )
                        .addComponent(buttonReset)
        );

        gl.setVerticalGroup(
                gl.createSequentialGroup()
                        .addComponent(labelInfoMessage)
                        .addGroup(gl.createParallelGroup().addComponent(jLabelDigits).addComponent(textFieldDigits))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelCharacters).addComponent(textFieldCharsUpper))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelCharactersLower).addComponent(textFieldCharsLower))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelSpecialChars).addComponent(textFieldSpecial))
                        .addComponent(buttonReset)
                        .addGap(0, 400, 800)

        );
    }

    private void resetToDefaultCharacterPools (JTextField textFieldDigits, JTextField textFieldCharsUpper, JTextField textFieldCharsLower, JTextField textFieldSpecial)
    {
        textFieldDigits.setText(PasswordCharacterPool.getDigits());
        textFieldCharsUpper.setText(PasswordCharacterPool.getUppercase());
        textFieldCharsLower.setText(PasswordCharacterPool.getLowercase());
        textFieldSpecial.setText(PasswordCharacterPool.getSpecial());
    }





    void initPreferences ()
    {
        PreferencesAccess pa = FgPreferences.get();

        this.textFieldCharsUpper.setText(pa.get(PREF_GPG_PASS_CHARPOOL_UPPER, PasswordCharacterPool.getUppercase()));
        this.textFieldCharsLower.setText(pa.get(PREF_GPG_PASS_CHARPOOL_LOWER, PasswordCharacterPool.getLowercase()));
        this.textFieldDigits.setText(pa.get(PREF_GPG_PASS_CHARPOOL_DIGIT, PasswordCharacterPool.getDigits()));
        this.textFieldSpecial.setText(pa.get(PREF_GPG_PASS_CHARPOOL_SPECIAL, PasswordCharacterPool.getSpecial()));

        this.jTextGpgExe.setText(pa.get(PREF_GPG_COMMAND));
        this.jTextGpgPostCommand.setText(pa.get(PREF_GPG_POST_COMMAND));
        this.jTextNewFileTemplate.setText(pa.get(PREF_NEW_TEMPLATE));
        this.jTextGpgHome.setText(pa.get(PREF_GPG_HOMEDIR));
        this.jTexfFieldGpgConf.setText(pa.get(PREF_GPGCONF_COMMAND));
        this.jTexfFieldBrowserOpen.setText(pa.get(PREF_URL_OPEN_COMMAND));
        this.jTextSecretDirs.setText(pa.get(PREF_SECRETDIRS));
        this.jCheckBoxUsePassDialog.setSelected(pa.getBoolean(PREF_USE_PASS_DIALOG));
        this.jCheckBoxPasswordShortcuts.setSelected(pa.getBoolean(PREF_SHOW_PASSWORD_SHORTCUT_BAR));
        this.jCheckBoxShowTbButtonText.setSelected(pa.get(PREF_SHOW_TB_BUTTON_TEXT, true));
        this.jCheckboxReloadAgent.setSelected(pa.getBoolean(PREF_USE_GPG_AGENT));
        this.jCheckboxMastFirstLine.setSelected(pa.getBoolean(PREF_MASK_FIRST_LINE));
        this.jCheckBoxUseFavorites.setSelected(pa.getBoolean(PREF_USE_FAVORITES));
        this.jCheckBoxFilterFavorites.setSelected(pa.getBoolean(PREF_FILTER_FAVORITES));
        this.jCheckBoxShowFavoritesCount.setSelected(pa.getBoolean(PREF_FAVORITES_SHOW_COUNT));
        this.jCheckBoxUseSearchTags.setSelected(pa.getBoolean(PREF_USE_SEARCH_TAGS));
        this.jCheckBoxShowSearchTags.setSelected(pa.getBoolean(PREF_SHOW_SEARCH_TAGS));
        this.jCheckBoxAddChangedDateTime.setSelected(pa.getBoolean(PREF_ADD_CHANGED_DATE_TIME));
        this.jFormattedTextareaClearTimeout.setText(String.format("%d", pa.getInt(PREF_CLEAR_SECONDS)));
        this.jFormattedTextPassClearTimeout.setText(String.format("%d", pa.getInt(PREF_PASSWORD_SECONDS)));
        this.jFormattedTextareaClipTimeout.setText(String.format("%d", pa.getInt(PREF_CLIP_SECONDS)));
        this.jFormattedTextFieldResetMaskButton.setText(String.format("%d", pa.getInt(PREF_RESET_MASK_BUTTON_SECONDS)));
        this.jFormattedTextFieldNumberFavorites.setText(String.format("%d", pa.getInt(PREF_NUMBER_FAVORITES)));
        this.jFormattedTextFieldMinFavoriteCount.setText(String.format("%d", pa.getInt(PREF_FAVORITES_MIN_HIT_COUNT)));
        this.jFormattedTextTextAreaFontSize.setText(String.format("%d", pa.getInt(PREF_TEXTAREA_FONT_SIZE)));
        this.jFormattedTextHistorySize.setText(String.format("%d", pa.getInt(PREF_HISTORY_SIZE)));
        this.jFormattedTextSecretListFontSize.setText(String.format("%d", pa.getInt(PREF_SECRETLIST_FONT_SIZE)));
        this.textFieldPassPatterns.setText(pa.get(PREF_PASSWORD_MASK_PATTERNS));
        this.comboBoxCharset.setSelectedItem(pa.get(PREF_CHARSET));
        this.textFieldUsernamePatterns.setText(pa.get(PREF_USERNAME_MASK_PATTERNS));
        this.jTexfFieldGpgRIDFile.setText(pa.get(PREF_GPG_RID_FILE, ".gpg-id"));
        this.jTexfFieldGpgDefaultRID.setText(pa.get(PREF_GPG_DEFAULT_RID, ""));
        this.jCheckBoxGpgUseAsciiFormat.setSelected(pa.getBoolean(PREF_GPG_USE_ASCII));

        this.lookAndFeelInfoJComboBox.setSelectedItem(pa.get(PREF_LOOK_AND_FEEL));

    }



    private void initGpgSettings ()
    {
        GroupLayout gl = new GroupLayout(gpgPanel);
        gpgPanel.setLayout(gl);

        JPanel secretDirPanel = new JPanel(new BorderLayout());
        secretDirPanel.add(jTextSecretDirs, BorderLayout.CENTER);
        JButton dirChooserButton;
        secretDirPanel.add(dirChooserButton = new JButton("Edit"), BorderLayout.EAST);
        dirChooserButton.addActionListener(e -> {
            FgDirChooser dirChooser = new FgDirChooser(this);
            dirChooser.setLocation(MouseInfo.getPointerInfo().getLocation());

            FgDirChooser.Response response = dirChooser.showDirectoryDialog(Arrays.asList(jTextSecretDirs.getText().split(";")));
            if (response != FgDirChooser.Response.CANCEL)
            {

                String[] newDirs = dirChooser.getSelectedDirectories()
                        .stream()
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.toList())
                        .toArray(new String[]{});
                jTextSecretDirs.setText(String.join(";", newDirs));
            }
        });

        JLabel jLabelGpgConf = new JLabel("GPGCONF Executable");
        jTexfFieldGpgConf = new JTextField();
        labelGpgHome.setText("GPG Home (optional or leave blank)");
        labelDataDirs.setText("Data directories (separate with \";\")");
        jTexfFieldGpgRIDFile = new JTextField();
        jTexfFieldGpgDefaultRID = new JTextField();
        jCheckBoxGpgUseAsciiFormat.setText("Encrypt in ASCII format");
        JLabel labelRID = new JLabel("Recipient ID hint file");
        JLabel labelDefaultRID = new JLabel("Default recipient ID for password encryption");
        JLabel labelGpgPostCommand = new JLabel("GPG Post Command (will be executed after encryption)");
        JLabel labelTextNewFileTemplate = new JLabel("Template file for new password entries (overrides default template)");

        JLabel labelGpgExe = new JLabel("GPG Executable");

        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(
                gl.createParallelGroup()
                    .addGroup(
                        gl.createSequentialGroup()
                            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(labelGpgExe)
                                    .addComponent(jLabelGpgConf)
                                    .addComponent(labelGpgHome)
                                    .addComponent(labelDataDirs)
                                    .addComponent(labelRID)
                                    .addComponent(labelDefaultRID)
                                    .addComponent(labelGpgPostCommand)
                                    .addComponent(labelTextNewFileTemplate)
                            )
                            .addGroup(gl.createParallelGroup()
                                    .addComponent(jTextGpgExe, 10, 300, 600)
                                    .addComponent(jTexfFieldGpgConf, 10, 300, 600)
                                    .addComponent(jTextGpgHome, 10, 300, 600)
                                    .addComponent(secretDirPanel, 10, 300, 600)
                                    .addComponent(jTexfFieldGpgRIDFile, 10, 300, 600)
                                    .addComponent(jTexfFieldGpgDefaultRID, 10, 300, 600)
                                    .addComponent(jTextGpgPostCommand, 10, 300, 600)
                                    .addComponent(jTextNewFileTemplate, 10, 300, 600)
                            )
                    )
                    .addComponent(jCheckBoxGpgUseAsciiFormat)
        );

        gl.setVerticalGroup(
                gl.createSequentialGroup()
                    .addGroup(gl.createParallelGroup().addComponent(labelGpgExe).addComponent(jTextGpgExe))
                    .addGroup(gl.createParallelGroup().addComponent(jLabelGpgConf).addComponent(jTexfFieldGpgConf))
                    .addGroup(gl.createParallelGroup().addComponent(labelGpgHome).addComponent(jTextGpgHome))
                    .addGroup(gl.createParallelGroup().addComponent(labelDataDirs).addComponent(secretDirPanel))
                    .addGroup(gl.createParallelGroup().addComponent(labelRID).addComponent(jTexfFieldGpgRIDFile))
                    .addGroup(gl.createParallelGroup().addComponent(labelDefaultRID).addComponent(jTexfFieldGpgDefaultRID))
                    .addGroup(gl.createParallelGroup().addComponent(labelGpgPostCommand).addComponent(jTextGpgPostCommand))
                    .addGroup(gl.createParallelGroup().addComponent(labelTextNewFileTemplate).addComponent(jTextNewFileTemplate))
                    .addComponent(jCheckBoxGpgUseAsciiFormat)
                    .addGap(200)
                    );


    }


    private void updateLafList ()
    {
        UIManager.LookAndFeelInfo[] lookAndFeelInfos = LAFChooser.get().getLafs();

        lookAndFeelInfoJComboBox.setModel(new DefaultComboBoxModel<String>()
        {
            @Override
            public int getSize ()
            {
                return lookAndFeelInfos.length;
            }

            @Override
            public String getElementAt (int index)
            {
                return lookAndFeelInfos[index].getName();
            }
        });

        lookAndFeelInfoJComboBox.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent (JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
            {
                if (value instanceof UIManager.LookAndFeelInfo)
                {
                    return super.getListCellRendererComponent(list, ((UIManager.LookAndFeelInfo)value).getName(),
                            index, isSelected, cellHasFocus);
                }
                else
                {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });
    }

    private void initComponents()
    {
        jTextGpgExe = new javax.swing.JTextField();
        jTextGpgPostCommand = new javax.swing.JTextField();
        jTextNewFileTemplate = new javax.swing.JTextField();
        labelGpgHome = new javax.swing.JLabel();
        jTextGpgHome = new javax.swing.JTextField();
        labelDataDirs = new javax.swing.JLabel();
        jTextSecretDirs = new javax.swing.JTextField();
        jLabelPassClearTimeout = new javax.swing.JLabel();
        jFormattedTextPassClearTimeout = new javax.swing.JFormattedTextField();
        jLabelTextClearTimeout = new javax.swing.JLabel();
        jFormattedTextareaClearTimeout = new javax.swing.JFormattedTextField();
        jCheckBoxUsePassDialog = new JCheckBoxPersistent(PREF_USE_PASS_DIALOG);
        jCheckBoxPasswordShortcuts = new JCheckBoxPersistent(PREF_SHOW_PASSWORD_SHORTCUT_BAR);
        jCheckBoxShowTbButtonText = new JCheckBoxPersistent(PREF_SHOW_TB_BUTTON_TEXT);
        jCheckboxReloadAgent = new JCheckBoxPersistent(PREF_USE_GPG_AGENT);
        jCheckboxMastFirstLine = new JCheckBoxPersistent(PREF_MASK_FIRST_LINE);
        jCheckBoxUseFavorites = new JCheckBoxPersistent(PREF_USE_FAVORITES);
        jCheckBoxGpgUseAsciiFormat = new JCheckBoxPersistent(PREF_GPG_USE_ASCII);
        jCheckBoxFilterFavorites = new JCheckBoxPersistent(PREF_FILTER_FAVORITES);
        jCheckBoxShowFavoritesCount = new JCheckBoxPersistent(PREF_FAVORITES_SHOW_COUNT);
        jCheckBoxUseSearchTags = new JCheckBoxPersistent(PREF_USE_SEARCH_TAGS, true);
        jCheckBoxShowSearchTags = new JCheckBoxPersistent(PREF_SHOW_SEARCH_TAGS, true);
        jCheckBoxAddChangedDateTime = new JCheckBoxPersistent(PREF_ADD_CHANGED_DATE_TIME, false);
        jButtonApply = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();
        jCheckBoxShowDebugWindow = new JCheckBox("Show debug window");
        jFormattedTextareaClipTimeout = new javax.swing.JFormattedTextField();
        jFormattedTextFieldResetMaskButton = new javax.swing.JFormattedTextField();
        jFormattedTextFieldNumberFavorites = new javax.swing.JFormattedTextField();
        jFormattedTextFieldMinFavoriteCount = new javax.swing.JFormattedTextField();
        jFormattedTextTextAreaFontSize = new javax.swing.JFormattedTextField();
        jFormattedTextHistorySize = new javax.swing.JFormattedTextField();
        jFormattedTextSecretListFontSize = new javax.swing.JFormattedTextField();
        lookAndFeelInfoJComboBox = new JComboBox<>();
        lookAndFeelInfoJComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent actionEvent)
            {
                String selected = (String)lookAndFeelInfoJComboBox.getSelectedItem();
                if (LAFChooser.get().set(selected,  FgOptionsDialog.this))
                {
                    FgPreferences.get().putPreference(PREF_LOOK_AND_FEEL, selected);
                }
            }
        });

        updateLafList();

        GroupLayout gl = new GroupLayout(optionsPanel);
        optionsPanel.setLayout(gl);

        JLabel jLabelOpenBrowserCommand = new JLabel("Browser launch command");
        jTexfFieldBrowserOpen = new JTextField();

        jLabelPassClearTimeout.setText("Password clear timeout [s]");

        jFormattedTextPassClearTimeout.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        jLabelTextClearTimeout.setText("Textarea clear timeout [s]");
        jFormattedTextareaClearTimeout.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        JLabel jLabelClipboardTimeout = new JLabel("Clipboard flush timeout [s]");
        jFormattedTextareaClipTimeout.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        JLabel jLabelResetMaskTimeout = new JLabel("Restore mask password button timeout [s] (< 0 to disable)");
        jFormattedTextFieldResetMaskButton.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        JLabel jLabelNumberFavorites = new JLabel("Number favorites (< 0 unlimited)");
        jFormattedTextFieldNumberFavorites.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        JLabel jLabelMinFavoriteCount = new JLabel("Minimal selections of an entry to become a favorite");
        jFormattedTextFieldMinFavoriteCount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        JLabel jLabelPassPatterns = new JLabel("Password patterns (\"|\" separated)");
        textFieldPassPatterns = new JTextField();

        JLabel labelCharset = new JLabel("Character Set");
        String[] charsets = {"ISO-8859-15", "ISO-8859-1", "UTF-8"};
        comboBoxCharset = new JComboBox(charsets);

        JLabel jLabelFontSize = new JLabel("Font size (text area)");
        jFormattedTextTextAreaFontSize.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        JLabel jLabelSecretListFontSize = new JLabel("Font size (password list)");
        jFormattedTextSecretListFontSize.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        JLabel jLabelHistorySize = new JLabel("History entries (0 to disable)");
        jFormattedTextHistorySize.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        JLabel labelUsernamePatterns = new JLabel("Username patterns (\"|\" separated)");
        textFieldUsernamePatterns = new JTextField();
        JLabel labelLAF = new JLabel("Application \"Look and Feel\"");

        jCheckBoxUsePassDialog.setText("Show password dialog (might not be required with GPG agent)");
        jCheckBoxPasswordShortcuts.setText("Show password shortcut bar");
        jCheckBoxShowTbButtonText.setText("Show description text below toolbar buttons");
        jCheckboxReloadAgent.setText("Allow flushing password from GPG agent");
        jCheckboxMastFirstLine.setText("Mask the first line on program start");
        jCheckBoxUseFavorites.setText("Show list of favorites");
        jCheckBoxFilterFavorites.setText("Filter favorites in addition to passwords");
        jCheckBoxShowFavoritesCount.setText("Show count of individual favorite");
        jCheckBoxUseSearchTags.setText("Use additional search tags when filtering list of passwords");
        jCheckBoxShowSearchTags.setText("Show search text in the password file list (behind the password file) ");
        jCheckBoxAddChangedDateTime.setText("Add changed mark to each edited file");

        // show/hide debug window
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

        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);
        JLabel dummyLabel = new JLabel("");
        gl.setHorizontalGroup(
                gl.createParallelGroup()
                        .addGroup(
                                gl.createSequentialGroup()
                                        .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabelOpenBrowserCommand)
                                                .addComponent(jLabelPassClearTimeout)
                                                .addComponent(jLabelTextClearTimeout)
                                                .addComponent(jLabelClipboardTimeout)
                                                .addComponent(jLabelResetMaskTimeout)
                                                .addComponent(jLabelNumberFavorites)
                                                .addComponent(jLabelMinFavoriteCount)
                                                .addComponent(jLabelPassPatterns)
                                                .addComponent(labelCharset)
                                                .addComponent(jLabelFontSize)
                                                .addComponent(jLabelSecretListFontSize)
                                                .addComponent(jLabelHistorySize)
                                                .addComponent(labelUsernamePatterns)
                                                .addComponent(labelLAF)
                                                .addComponent(jCheckBoxUsePassDialog)
                                                .addComponent(jCheckboxReloadAgent)
                                                .addComponent(jCheckBoxUseFavorites)
                                                .addComponent(jCheckBoxShowFavoritesCount)
                                                .addComponent(jCheckBoxUseSearchTags)
                                                .addComponent(jCheckBoxShowSearchTags)
                                        )
                                        .addGroup(gl.createParallelGroup()
                                                .addComponent(jTexfFieldBrowserOpen)
                                                .addComponent(jFormattedTextPassClearTimeout)
                                                .addComponent(jFormattedTextareaClearTimeout)
                                                .addComponent(jFormattedTextareaClipTimeout)
                                                .addComponent(jFormattedTextFieldResetMaskButton)
                                                .addComponent(jFormattedTextFieldNumberFavorites)
                                                .addComponent(jFormattedTextFieldMinFavoriteCount)
                                                .addComponent(textFieldPassPatterns)
                                                .addComponent(comboBoxCharset)
                                                .addComponent(jFormattedTextTextAreaFontSize)
                                                .addComponent(jFormattedTextSecretListFontSize)
                                                .addComponent(jFormattedTextHistorySize)
                                                .addComponent(textFieldUsernamePatterns)
                                                .addComponent(lookAndFeelInfoJComboBox)
                                                .addComponent(jCheckBoxPasswordShortcuts)
                                                .addComponent(jCheckboxMastFirstLine)
                                                .addComponent(jCheckBoxFilterFavorites)
                                                .addComponent(jCheckBoxShowTbButtonText)
                                                .addComponent(jCheckBoxAddChangedDateTime)
                                                .addComponent(dummyLabel)
                                        )
                        )
        );

        gl.setVerticalGroup(
                gl.createSequentialGroup()
                        .addGroup(gl.createParallelGroup().addComponent(jLabelOpenBrowserCommand).addComponent(jTexfFieldBrowserOpen))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelPassClearTimeout).addComponent(jFormattedTextPassClearTimeout))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelTextClearTimeout).addComponent(jFormattedTextareaClearTimeout))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelClipboardTimeout).addComponent(jFormattedTextareaClipTimeout))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelResetMaskTimeout).addComponent(jFormattedTextFieldResetMaskButton))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelNumberFavorites).addComponent(jFormattedTextFieldNumberFavorites))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelMinFavoriteCount).addComponent(jFormattedTextFieldMinFavoriteCount))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelPassPatterns).addComponent(textFieldPassPatterns))
                        .addGroup(gl.createParallelGroup().addComponent(labelCharset).addComponent(comboBoxCharset))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelFontSize).addComponent(jFormattedTextTextAreaFontSize))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelSecretListFontSize).addComponent(jFormattedTextSecretListFontSize))
                        .addGroup(gl.createParallelGroup().addComponent(jLabelHistorySize).addComponent(jFormattedTextHistorySize))
                        .addGroup(gl.createParallelGroup().addComponent(labelUsernamePatterns).addComponent(textFieldUsernamePatterns))
                        .addGroup(gl.createParallelGroup().addComponent(labelLAF).addComponent(lookAndFeelInfoJComboBox))
                        .addGroup(gl.createParallelGroup().addComponent(jCheckBoxUsePassDialog).addComponent(jCheckBoxPasswordShortcuts))
                        .addGroup(gl.createParallelGroup().addComponent(jCheckboxReloadAgent).addComponent(jCheckboxMastFirstLine))
                        .addGroup(gl.createParallelGroup().addComponent(jCheckBoxUseFavorites).addComponent(jCheckBoxFilterFavorites))
                        .addGroup(gl.createParallelGroup().addComponent(jCheckBoxShowFavoritesCount).addComponent(jCheckBoxShowTbButtonText))
                        .addGroup(gl.createParallelGroup().addComponent(jCheckBoxUseSearchTags).addComponent(jCheckBoxAddChangedDateTime))
                        .addGroup(gl.createParallelGroup().addComponent(jCheckBoxShowSearchTags).addComponent(dummyLabel))
        );


        // ==============================================================

        jButtonApply.setText("Apply");
        jButtonApply.setMnemonic(KeyEvent.VK_A);
        jButtonApply.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonApplyActionPerformed(evt);
            }
        });

        jButtonSave.setText("Save and Close");
        jButtonSave.setMnemonic(KeyEvent.VK_S);
        jButtonSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonApplyActionPerformed(evt);
                setVisible(false);
            }
        });

        jButtonClose.setText("Close");
        jButtonApply.setMnemonic('c');
        jButtonClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonCloseActionPerformed(evt);
            }
        });

        buttonPanel.add(jButtonApply);
        buttonPanel.add(jButtonSave);
        buttonPanel.add(jButtonClose);

        pack();
    }

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt)
    {
        setVisible(false);
    }

    private void jButtonApplyActionPerformed (java.awt.event.ActionEvent evt)
    {

        Integer clearTimeout;
        Integer clipTimeout;
        Integer passTimeout;
        Integer resetMaskButtonTimeout;
        Integer numberFavorites;
        Integer minNumberFavoritesCount;
        Integer textAreaFontSize;
        Integer secretListFontSize;
        Integer historySize;

        try
        {
            textAreaFontSize = Integer.parseInt(jFormattedTextTextAreaFontSize.getText());
            textAreaFontSize = Math.max(textAreaFontSize, 8);
            textAreaFontSize = Math.min(textAreaFontSize, 60);
            FgPreferences.get().put(PREF_TEXTAREA_FONT_SIZE, textAreaFontSize);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }
        try
        {
            historySize = Integer.parseInt(jFormattedTextHistorySize.getText());
            historySize = Math.max(historySize, 0);
            historySize = Math.min(historySize, 400);
            FgPreferences.get().put(PREF_HISTORY_SIZE, historySize);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }

        try
        {
            secretListFontSize = Integer.parseInt(jFormattedTextSecretListFontSize.getText());
            secretListFontSize = Math.max(secretListFontSize, 8);
            secretListFontSize = Math.min(secretListFontSize, 60);
            FgPreferences.get().put(PREF_SECRETLIST_FONT_SIZE, secretListFontSize);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }


        try
        {
            clipTimeout = Integer.parseInt(jFormattedTextareaClipTimeout.getText());
            FgPreferences.get().put(PREF_CLIP_SECONDS, clipTimeout);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }


        try
        {
            resetMaskButtonTimeout = Integer.parseInt(jFormattedTextFieldResetMaskButton.getText());
            FgPreferences.get().put(PREF_RESET_MASK_BUTTON_SECONDS, resetMaskButtonTimeout);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }

        try
        {
            numberFavorites = Integer.parseInt(jFormattedTextFieldNumberFavorites.getText());
            FgPreferences.get().put(PREF_NUMBER_FAVORITES, numberFavorites);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }

        try
        {
            minNumberFavoritesCount = Integer.parseInt(jFormattedTextFieldMinFavoriteCount.getText());
            minNumberFavoritesCount = Math.max(minNumberFavoritesCount, 0);
            jFormattedTextFieldMinFavoriteCount.setValue(minNumberFavoritesCount);
            FgPreferences.get().put(PREF_FAVORITES_MIN_HIT_COUNT, minNumberFavoritesCount);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }

        try
        {
            clearTimeout = Integer.parseInt(jFormattedTextareaClearTimeout.getText());
            FgPreferences.get().put(PREF_CLEAR_SECONDS, clearTimeout);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }
        try
        {
            passTimeout = Integer.parseInt(jFormattedTextPassClearTimeout.getText());
            FgPreferences.get().put(PREF_PASSWORD_SECONDS, passTimeout);
        }
        catch (NumberFormatException ex)
        {
            // live with invalid defaults
        }

        FgPreferences.get().put(PREF_PASSWORD_MASK_PATTERNS, textFieldPassPatterns.getText());
        FgPreferences.get().put(PREF_USERNAME_MASK_PATTERNS, textFieldUsernamePatterns.getText());
        FgPreferences.get().put(PREF_GPG_COMMAND, jTextGpgExe.getText());
        FgPreferences.get().put(FgPreferences.PREF_SECRETDIRS, jTextSecretDirs.getText());
        FgPreferences.get().put(FgPreferences.PREF_GPG_HOMEDIR, jTextGpgHome.getText());
        FgPreferences.get().put(FgPreferences.PREF_GPGCONF_COMMAND, jTexfFieldGpgConf.getText());
        FgPreferences.get().put(PREF_URL_OPEN_COMMAND, jTexfFieldBrowserOpen.getText());
        FgPreferences.get().put(FgPreferences.PREF_USE_PASS_DIALOG, jCheckBoxUsePassDialog.isSelected());
        FgPreferences.get().put(PREF_SHOW_PASSWORD_SHORTCUT_BAR, jCheckBoxPasswordShortcuts.isSelected());
        FgPreferences.get().put(PREF_SHOW_TB_BUTTON_TEXT, jCheckBoxShowTbButtonText.isSelected());
        FgPreferences.get().put(PREF_ADD_CHANGED_DATE_TIME, jCheckBoxAddChangedDateTime.isSelected());
        FgPreferences.get().put(FgPreferences.PREF_USE_GPG_AGENT, jCheckboxReloadAgent.isSelected());
        FgPreferences.get().put(PREF_MASK_FIRST_LINE, jCheckboxMastFirstLine.isSelected());
        FgPreferences.get().put(PREF_USE_FAVORITES, jCheckBoxUseFavorites.isSelected());
        FgPreferences.get().put(PREF_FILTER_FAVORITES, jCheckBoxFilterFavorites.isSelected());
        FgPreferences.get().put(PREF_FAVORITES_SHOW_COUNT, jCheckBoxShowFavoritesCount.isSelected());
        FgPreferences.get().put(PREF_USE_SEARCH_TAGS, jCheckBoxUseSearchTags.isSelected());
        FgPreferences.get().put(PREF_SHOW_SEARCH_TAGS, jCheckBoxShowSearchTags.isSelected());
        FgPreferences.get().put(PREF_CHARSET, comboBoxCharset.getSelectedItem());
        FgPreferences.get().put(PREF_GPG_RID_FILE, jTexfFieldGpgRIDFile.getText());
        FgPreferences.get().put(PREF_GPG_DEFAULT_RID, jTexfFieldGpgDefaultRID.getText());
        FgPreferences.get().put(PREF_GPG_USE_ASCII, jCheckBoxGpgUseAsciiFormat.isSelected());
        FgPreferences.get().put(PREF_GPG_POST_COMMAND, jTextGpgPostCommand.getText());
        FgPreferences.get().put(PREF_NEW_TEMPLATE, jTextNewFileTemplate.getText());

        FgPreferences.get().put(PREF_GPG_PASS_CHARPOOL_UPPER, textFieldCharsUpper.getText().toUpperCase());
        FgPreferences.get().put(PREF_GPG_PASS_CHARPOOL_LOWER, textFieldCharsLower.getText().toLowerCase());
        FgPreferences.get().put(PREF_GPG_PASS_CHARPOOL_SPECIAL, textFieldSpecial.getText());
        FgPreferences.get().put(PREF_GPG_PASS_CHARPOOL_DIGIT, textFieldDigits.getText());
        initPreferences();
    }


    @Override
    public void setVisible (boolean b) {

        Point location = MouseInfo.getPointerInfo().getLocation();
        setLocation(location);
        initPreferences();
        SwingUtilities.invokeLater(() ->
                super.setVisible(b));
    }

}
