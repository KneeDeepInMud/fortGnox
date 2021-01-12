package org.mockenhaupt.fortgnox;

import org.mockenhaupt.fortgnox.swing.JCheckBoxPersistent;
import org.mockenhaupt.fortgnox.swing.LAFChooser;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PasswordGenerator
{
    private JDialog generatorWindow;
    private final JFrame parent;
    private final List<Character> digits = new ArrayList<>();
    private final List<Character> uppercase = new ArrayList<>();
    private final List<Character> lowercase = new ArrayList<>();
    private final List<Character> special = new ArrayList<>();
    private final JComboBox<String> comboBoxPasswords = new JComboBox<>();
    private final JFormattedTextField textFieldLength = new JFormattedTextField();
    private final JCheckBoxPersistent cbUpper = new JCheckBoxPersistent(FgPreferences.PREF_GPG_PASS_UPPER, "Uppercase", () -> handleEnabled());
    private final JCheckBoxPersistent cbLower = new JCheckBoxPersistent(FgPreferences.PREF_GPG_PASS_LOWER, "Lowercase", () -> handleEnabled());
    private final JCheckBoxPersistent cbDigit = new JCheckBoxPersistent(FgPreferences.PREF_GPG_PASS_DIGITS, "Digits", () -> handleEnabled());
    private final JCheckBoxPersistent cbSpecial = new JCheckBoxPersistent(FgPreferences.PREF_GPG_PASS_SPECIAL, "Special", () -> handleEnabled());
    private final JButton buttonGenerate = new JButton("Generate Password");
    private final JButton buttonInsert = new JButton("Insert");
    private final JButton buttonCopy = new JButton("Clipboard");
    private final JButton buttonReset = new JButton("Reset");
    private final List<String> passwordList = new ArrayList<>();
    private final PasswordInsertListener passwordInsertListener;
    private JPanel generatorPanel;

    public interface PasswordInsertListener
    {
        void handlePasswordInsert (String password);
    }

    public JPanel getGeneratorPanel ()
    {
        if (generatorPanel == null)
        {
            generatorPanel = new JPanel();
            GroupLayout gl = new GroupLayout(generatorPanel);
            generatorPanel.setLayout(gl);

            gl.setAutoCreateGaps(true);

            buttonReset.setEnabled(false);
            buttonReset.addActionListener(a -> resetPasswords());
            buttonGenerate.addActionListener(actionEvent -> generatePassword());
            buttonGenerate.setMnemonic(KeyEvent.VK_G);

            buttonCopy.addActionListener(actionEvent ->
            {
                if (comboBoxPasswords.getModel().getSize() == 0)
                {
                    generatePassword();
                }
                FgGPGProcess.clip(comboBoxPasswords.getSelectedItem().toString());
            });
            buttonInsert.addActionListener(actionEvent ->
            {
                if (passwordInsertListener != null)
                {
                    if (comboBoxPasswords.getModel().getSize() == 0)
                    {
                        generatePassword();
                    }
                    passwordInsertListener.handlePasswordInsert(comboBoxPasswords.getSelectedItem().toString());
                }
            });
            buttonInsert.setMnemonic(KeyEvent.VK_I);

            comboBoxPasswords.setMaximumSize(new Dimension(300, 40));

            cbUpper.setMnemonic('u');
            cbLower.setMnemonic('o');
            cbDigit.setMnemonic('d');
            cbSpecial.setMnemonic('e');
            buttonCopy.setMnemonic('l');

            textFieldLength.setMinimumSize(new Dimension(40, 10));
            textFieldLength.setValue(FgPreferences.get().getPreference(FgPreferences.PREF_GPG_PASS_LENGTH, 18));
            textFieldLength.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
            textFieldLength.getDocument().addDocumentListener(new DocumentListener()
            {
                void updatePreference ()
                {
                    FgPreferences.get().putPreference(FgPreferences.PREF_GPG_PASS_LENGTH, textFieldLength.getText());
                }

                @Override
                public void insertUpdate (DocumentEvent documentEvent)
                {
                    updatePreference();
                }

                @Override
                public void removeUpdate (DocumentEvent documentEvent)
                {
                    updatePreference();
                }

                @Override
                public void changedUpdate (DocumentEvent documentEvent)
                {
                    updatePreference();
                }
            });

            handleEnabled();
            gl.setHorizontalGroup(
                    gl.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addGroup(gl.createSequentialGroup()
                                    .addComponent(cbUpper)
                                    .addComponent(cbLower)
                                    .addComponent(cbDigit)
                                    .addComponent(cbSpecial)
                                    .addGap(10)
                                    .addComponent(textFieldLength, 20, 40, 60)
                                    .addComponent(buttonGenerate)
                            )
                            .addGroup(gl.createSequentialGroup()
                                    .addComponent(comboBoxPasswords, 20, 100, 400)
                                    .addComponent(buttonInsert)
                                    .addComponent(buttonCopy)
                                    .addComponent(buttonReset)
                            )
            );


            gl.setVerticalGroup(
                    gl.createSequentialGroup()
                            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addComponent(cbUpper)
                                    .addComponent(cbLower)
                                    .addComponent(cbDigit)
                                    .addComponent(cbSpecial)
                                    .addComponent(textFieldLength)
                                    .addComponent(buttonGenerate)
                            )
                            .addGroup(gl.createParallelGroup()
                                    .addComponent(comboBoxPasswords)
                                    .addComponent(buttonInsert)
                                    .addComponent(buttonCopy)
                                    .addComponent(buttonReset)
                            )
            );
            LAFChooser.setPreferenceLaf(generatorPanel);

        }
        return generatorPanel;
    }

    public PasswordGenerator (JFrame parent)
    {
        passwordInsertListener = null;
        this.parent = parent;
        initCharacterPools();
        initDialog();
    }

    public PasswordGenerator (PasswordInsertListener listener)
    {
        this.parent = null;
        initCharacterPools();
        passwordInsertListener = listener;
    }


    public static void main (String args[])
    {
        new PasswordGenerator((JFrame) null);
    }

    public static boolean isPrintableChar (char c)
    {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                c != KeyEvent.CHAR_UNDEFINED &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

    public void initCharacterPools ()
    {
        Set<Character> skip = new HashSet<Character>(Arrays.asList(new Character[]{'"', '\'', '\\'}));
        for (char i = 33; i <= 126; ++i)
        {
            if (skip.contains(i)) continue;
            if (isPrintableChar(i))
            {
                if (Character.isDigit(i))
                {
                    digits.add(i);
                }
                else if (Character.isAlphabetic(i))
                {
                    if (Character.isUpperCase(i))
                        uppercase.add(i);
                    if (Character.isLowerCase(i))
                        lowercase.add(i);

                }
                else
                {
                    special.add(i);
                }
            }
        }
    }


    private void initDialog ()
    {
        if (generatorWindow == null)
        {
            generatorWindow = new JDialog(parent, true);
            generatorWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            JPanel generatorPanel = getGeneratorPanel();

            generatorWindow.add(generatorPanel);
            generatorWindow.pack();
            generatorWindow.setVisible(true);
        }
    }


    private void generatePassword ()
    {
        Long len = Long.parseLong(textFieldLength.getText());

        len = Math.abs(len);
        len = Math.min(len, 256);
        len = Math.max(len, 4);

        textFieldLength.setValue(len);

        StringBuilder sb = new StringBuilder();

        Random rnd = new SecureRandom();

        List<List<Character>> pool = new ArrayList<>();
        if (cbDigit.isSelected()) pool.add(digits);
        if (cbUpper.isSelected()) pool.add(uppercase);
        if (cbLower.isSelected()) pool.add(lowercase);
        if (cbSpecial.isSelected()) pool.add(special);

        if (pool.size() == 0)
        {
            return;
        }

        while (sb.length() < len)
        {
            int poolIx = Math.abs(rnd.nextInt()) % pool.size();
            List<Character> cSet = pool.get(poolIx);
            int cSetIx = Math.abs(rnd.nextInt()) % cSet.size();
            sb.append(cSet.get(cSetIx));
        }

        addPassword(sb.toString());
    }

    private void addPassword (String pass)
    {
        if (pass == null || pass.isEmpty())
        {
            return;
        }
        passwordList.add(pass);
        comboBoxPasswords.setModel(new DefaultComboBoxModel<String>()
        {
            @Override
            public int getSize ()
            {
                return passwordList.size();
            }

            @Override
            public String getElementAt (int index)
            {
                return passwordList.get(passwordList.size() - index - 1);
            }
        });

        if (comboBoxPasswords.getModel().getSize() > 0)
        {
            comboBoxPasswords.setSelectedIndex(0);
            buttonReset.setEnabled(true);
        }
    }


    private void resetPasswords ()
    {
        passwordList.clear();
        comboBoxPasswords.setModel(new DefaultComboBoxModel<String>()
        {
            @Override
            public int getSize ()
            {
                return passwordList.size();
            }

            @Override
            public String getElementAt (int index)
            {
                return passwordList.get(passwordList.size() - index - 1);
            }
        });
        buttonReset.setEnabled(false);
    }


    private void handleEnabled ()
    {
        boolean enabled = cbDigit.isSelected() || cbLower.isSelected() || cbUpper.isSelected() || cbSpecial.isSelected();
//        boolean enabled = !(comboBoxPasswords.getSelectedItem() == null || comboBoxPasswords.getSelectedItem().toString().isEmpty());
        buttonCopy.setEnabled(enabled);
        buttonInsert.setEnabled(enabled);
        buttonGenerate.setEnabled(enabled);
    }


}
