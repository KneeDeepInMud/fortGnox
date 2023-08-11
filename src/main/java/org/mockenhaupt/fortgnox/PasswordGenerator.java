package org.mockenhaupt.fortgnox;

import org.mockenhaupt.fortgnox.misc.PasswordCharacterPool;
import org.mockenhaupt.fortgnox.swing.JCheckBoxPersistent;
import org.mockenhaupt.fortgnox.swing.LAFChooser;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_PASS_CHARPOOL_DIGIT;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_PASS_CHARPOOL_LOWER;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_PASS_CHARPOOL_SPECIAL;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_PASS_CHARPOOL_UPPER;

public class PasswordGenerator implements PropertyChangeListener
{
    private JDialog generatorWindow;
    private final JFrame parent;
    private List<Character> digits = new ArrayList<>();
    private List<Character> uppercase = new ArrayList<>();
    private List<Character> lowercase = new ArrayList<>();
    private List<Character> special = new ArrayList<>();

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

    @Override
    public void propertyChange (PropertyChangeEvent propertyChangeEvent)
    {
        switch (propertyChangeEvent.getPropertyName())
        {
            case PREF_GPG_PASS_CHARPOOL_DIGIT:
            case PREF_GPG_PASS_CHARPOOL_LOWER:
            case PREF_GPG_PASS_CHARPOOL_UPPER:
            case PREF_GPG_PASS_CHARPOOL_SPECIAL:
                initCharacterPools();
                break;
        }
    }

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
        FgPreferences.get().addPropertyChangeListener(this);
    }

    public PasswordGenerator (PasswordInsertListener listener)
    {
        this.parent = null;
        initCharacterPools();
        passwordInsertListener = listener;
        FgPreferences.get().addPropertyChangeListener(this);
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

    private void initCharacterPools ()
    {
        addCharacters(uppercase, FgPreferences.get().get(PREF_GPG_PASS_CHARPOOL_UPPER, PasswordCharacterPool.getUppercase()));
        addCharacters(lowercase, FgPreferences.get().get(PREF_GPG_PASS_CHARPOOL_LOWER, PasswordCharacterPool.getLowercase()));
        addCharacters(digits, FgPreferences.get().get(PREF_GPG_PASS_CHARPOOL_DIGIT, PasswordCharacterPool.getDigits()));
        addCharacters(special, FgPreferences.get().get(PREF_GPG_PASS_CHARPOOL_SPECIAL, PasswordCharacterPool.getSpecial()));
    }

    private void addCharacters (List<Character> characters, String s)
    {
        characters.clear();
        for (int i = 0; i < s.length(); ++i)
        {
            characters.add(s.charAt(i));
        }
    }


    private void initDialog ()
    {
        if (generatorWindow == null)
        {
            generatorWindow = new JDialog(parent, true);
            generatorWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            JPanel generatorPanelRef = getGeneratorPanel();

            generatorWindow.add(generatorPanelRef);
            generatorWindow.pack();
            generatorWindow.setVisible(true);
        }
    }


    public void generatePassword (int len,
                                  boolean digit,
                                  boolean upper,
                                  boolean lower,
                                  boolean useSpecial,
                                  Consumer<String> handleError,
                                  Consumer<String> handlePass)
    {

        if (len < 4 || len > 256) {
            handleError.accept("Password length not supported");
            return;
        }
        StringBuilder sb = new StringBuilder();

        Random rnd = new SecureRandom();

        List<List<Character>> pool = new ArrayList<>();
        if (digit) pool.add(digits);
        if (upper) pool.add(uppercase);
        if (lower) pool.add(lowercase);
        if (useSpecial) pool.add(special);

        if (pool.size() == 0)
        {
            return;
        }

        int poolIx = 0;
        int poolMod = pool.size();
        HashMap<Integer, Character> passMap = new HashMap<>();
        while (passMap.size() < len)
        {
            List<Character> curPool = pool.get(poolIx);

            int curPoolIx = Math.abs(rnd.nextInt()) % curPool.size();
            Character passChar = curPool.get(curPoolIx);
            int insertPos = Math.abs(rnd.nextInt()) % len;

            while (passMap.get(insertPos) != null)
            {
                insertPos++;
                insertPos %= len;
            }
            passMap.put(insertPos, passChar);

            poolIx++;
            poolIx %= poolMod;
        }

        passMap.keySet().stream().sorted().forEach(pos -> sb.append(passMap.get(pos)));
        handlePass.accept(sb.toString());
    }

    private void generatePassword ()
    {
        Integer len = Integer.parseInt(textFieldLength.getText());

        len = Math.abs(len);
        len = Math.min(len, 256);
        len = Math.max(len, 4);

        textFieldLength.setValue(len);

        generatePassword(len,
                cbDigit.isSelected(),
                cbUpper.isSelected(),
                cbLower.isSelected(),
                cbSpecial.isSelected(),
                errorMsg -> JOptionPane.showMessageDialog(parent, errorMsg, "fortGnox WARNING", JOptionPane.ERROR_MESSAGE),
                password -> addPassword(password));
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


    public void resetPasswords ()
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


    public List<Character> getDigits ()
    {
        return digits;
    }

    public List<Character> getUppercase ()
    {
        return uppercase;
    }

    public List<Character> getLowercase ()
    {
        return lowercase;
    }

    public List<Character> getSpecial ()
    {
        return special;
    }
}
