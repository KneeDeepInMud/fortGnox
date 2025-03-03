/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainFrame.java
 *
 * Created on 26.02.2011, 18:47:39
 */
package org.mockenhaupt.fortgnox;


import org.apache.commons.io.FilenameUtils;
import org.mockenhaupt.fortgnox.misc.FileUtils;
import org.mockenhaupt.fortgnox.misc.History;
import org.mockenhaupt.fortgnox.misc.StringUtils;
import org.mockenhaupt.fortgnox.otp.OtpQRUtil;
import org.mockenhaupt.fortgnox.swing.FgOptionsDialog;
import org.mockenhaupt.fortgnox.swing.FgPanelTextArea;
import org.mockenhaupt.fortgnox.swing.FgTextFilter;
import org.mockenhaupt.fortgnox.swing.LAFChooser;
import org.mockenhaupt.fortgnox.tags.TagsStore;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static org.mockenhaupt.fortgnox.DebugWindow.Category.*;
import static org.mockenhaupt.fortgnox.FgPreferences.*;

/**
 * @author fmoc
 */
public class MainFrame extends JFrame implements
        FgGPGProcess.SecretListListener,
        FgGPGProcess.ResultListListener,
        ActionListener,
        PropertyChangeListener,
        EditWindow.EditHandler,
        FgTextFilter.TextFilterHandler
{

    private FgGPGProcess gpgProcess;
    private EditWindow editWindow;
    private PassphraseDialog passDlg;
    final private Timer clearTimer;
    final private Timer passTimer;
    private int clearSeconds = 0;
    private int passSeconds = 0;
    private int prefClearSeconds = 30;
    private int prefClipboardSeconds = 10;
    private int prefPasswordSeconds = 60 * 5;
    private final int CLEAR_SECONDS_DEFAULT = 30;
    private final int CLIP_SECONDS_DEFAULT = 10;
    private final int PASSWORD_SECONDS_DEFAULT = 60 * 5;
    private final int MIN_TIMER_VALUE = 1;
    private String toDecode = "";
    private int prefNumberFavorites = 8;
    private int prefFavoritesMinHitCount = 2;
    private boolean prefShowFavoritesCount = false;
    private boolean prefUseSearchTags = true;

    private boolean prefShowSearchTags = true;

    private String[] allSecretFiles = new String[]{""};
    private final List<String> secretListModel = new ArrayList<>();

    private final LinkedHashMap<String, Integer> favorites = new LinkedHashMap<>();
    private final java.util.List<String> favoritesList = new ArrayList<>();
    final private List<String> filteredFavorites = new ArrayList<>();

    public static final String VERSION_PROJECT = "project.version";
    public static final String VERSION_BUILD = "buildNumber";

    public static final String CLIENTDATA_EDIT = "editGpg";
    private JButton buttonClearPass;
    private JButton buttonHistoryForward;
    private JButton buttonHistoryBackward;
    private History history;

    //    private JButton buttonClearTextarea;
    private JButton buttonExit;
    private JButton buttonAbout;
    private JButton jButtonSettings;
    private JButton buttonClearFavorites;
    private JButton buttonNew;
    private JButton buttonEdit;


    private JList jListSecrets;
    private JProgressBar progressClearTimer;
    private JProgressBar progressPassTimer;
    private FgTextFilter fgTextFilter;
    private JScrollPane scrollPaneSecrets;
    private JSplitPane jSplitPaneLR;
    private FgPanelTextArea fgPanelTextArea;
    private JLabel labelSecretInfo;
    private JPanel jToolBarPanel;
    JToolBar aboutPanel;
    private JToolBar jToolBarMainFunctions;
    private boolean prefUseFavoriteList = true;
    private boolean prefFilterFavoriteList = true;
    private boolean prefShowToobarTexts = true;
    private int prefSecretListFontSize = 12;
    private Boolean editMode;
    private FgOptionsDialog fgOptionsDialog;
    static private MainFrame INSTANCE;

    /**
     * @param args the command line arguments
     */
    public static void main (String[] args)
    {
        for (int i = 0; i < args.length; ++i)
        {
            String thisArg = args[i];
            if (thisArg.startsWith("-q") || thisArg.startsWith("--q"))
            {
                 if (i + 1 < args.length)
                 {
                     i++;
                     printTextFromQrCode(args[i]);
                     System.exit(0);
                 }
            }
        }

        // Show fortGnox main window
        showMainWindow(args);
    }

    // otpauth -link `java -jar target/fortgnox-v1.1.0.jar -q QR_GoogleAuthenticatorAllTokens.jpg`
    private static void printTextFromQrCode(String args)
    {
        String qrImage = args;
        System.out.println(OtpQRUtil.decodeQrCode(qrImage));
    }

    private static void showMainWindow(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run ()
            {

                boolean showDebug = false;
                for (int i = 0; i < args.length; ++i)
                {
                    String thisArg = args[i];

                    if (thisArg.startsWith("-d") || thisArg.startsWith("--d"))
                    {
                        showDebug = true;
                        if (i + 1 >= args.length)
                        {
                            DebugWindow.get().setDebugMask(Integer.MAX_VALUE);
                        }
                        else
                        {
                            String nextArg = null;
                            ++i;
                            nextArg = args[i];

                            if (nextArg.toUpperCase().startsWith("DIR"))
                            {
                                DebugWindow.get().enableDebugCategory(DIR);
                            }
                            else if (nextArg.toUpperCase().startsWith("FAV"))
                            {
                                DebugWindow.get().enableDebugCategory(FAV);
                            }
                            else if (nextArg.toUpperCase().startsWith("FIL"))
                            {
                                DebugWindow.get().enableDebugCategory(FILTER);
                            }
                            else if (nextArg.toUpperCase().startsWith("LI"))
                            {
                                DebugWindow.get().enableDebugCategory(LIST);
                            }
                            else if (nextArg.toUpperCase().startsWith("G"))
                            {
                                DebugWindow.get().enableDebugCategory(GPG);
                            }
                            else
                            {
                                try
                                {
                                    int val = Integer.parseInt(nextArg);
                                    DebugWindow.get().setDebugMask(val);
                                }
                                catch (Exception ex)
                                {
                                    System.err.println("Invalid argument");
                                    System.exit(1);
                                }
                            }
                        }
                    }
                }

                if (showDebug)
                {
                    DebugWindow.get().setWindowVisible(true);
                }

                INSTANCE = new MainFrame();

                String lafPref = FgPreferences.get().get(PREF_LOOK_AND_FEEL);
                if (lafPref == null || lafPref.isEmpty())
                {
                    if (!LAFChooser.get().set("windows", INSTANCE))
                    {
                        LAFChooser.get().set("gtk", INSTANCE);
                    }
                    FgPreferences.get().putPreference(PREF_LOOK_AND_FEEL, LAFChooser.get().getCurrentLAF().getClassName());
                }
                else
                {
                    LAFChooser.get().set(lafPref, INSTANCE);
                }

                INSTANCE.setVisible(true);

            }
        });
    }

    private static MainFrame getInstance ()
    {
        return INSTANCE;
    }

    private void loadPreferences ()
    {
        this.setTitle("fortGnox " + getVersionFromManifest().computeIfAbsent(VERSION_PROJECT, k -> "UNKNOWN"));

        PreferencesAccess preferences = FgPreferences.get();

        prefClearSeconds = preferences.get(PREF_CLEAR_SECONDS, CLEAR_SECONDS_DEFAULT);
        prefClipboardSeconds = preferences.get(PREF_CLIP_SECONDS, CLIP_SECONDS_DEFAULT);
        prefPasswordSeconds = preferences.get(PREF_PASSWORD_SECONDS, PASSWORD_SECONDS_DEFAULT);
        prefNumberFavorites = preferences.get(PREF_NUMBER_FAVORITES, prefNumberFavorites);
        prefFavoritesMinHitCount = preferences.get(PREF_FAVORITES_MIN_HIT_COUNT, prefFavoritesMinHitCount);
        prefShowFavoritesCount = preferences.get(PREF_FAVORITES_SHOW_COUNT, prefShowFavoritesCount);
        prefUseSearchTags = preferences.get(PREF_USE_SEARCH_TAGS, prefUseSearchTags);
        prefShowSearchTags = preferences.get(PREF_SHOW_SEARCH_TAGS, prefShowSearchTags);
        prefFilterFavoriteList = preferences.get(PREF_FILTER_FAVORITES, prefFilterFavoriteList);
        prefShowToobarTexts = preferences.get(PREF_SHOW_TB_BUTTON_TEXT, prefShowToobarTexts);
        prefSecretListFontSize = preferences.get(PREF_SECRETLIST_FONT_SIZE, prefSecretListFontSize);
        setPrefUseFavorites(preferences.get(PREF_USE_FAVORITES, prefUseFavoriteList));
        favoritesParseFromJson(preferences.get(PREF_FAVORITES, "{}"));
    }

    private int getPrefClearSeconds ()
    {
        return prefClearSeconds;
    }

    private int getPrefClipboardSeconds ()
    {
        return prefClipboardSeconds;
    }

    private void setPrefClipboardSeconds (int clipSeconds)
    {
        clipSeconds = Math.max(MIN_TIMER_VALUE, clipSeconds);
        this.prefClipboardSeconds = clipSeconds;
    }

    private void setPrefClearSeconds (int clearSec)
    {
        clearSec = Math.max(MIN_TIMER_VALUE, clearSec);
        this.prefClearSeconds = clearSec;
        progressClearTimer.setMaximum(prefClearSeconds);
        startTimer();
    }

    public int getPrefPasswordSeconds ()
    {
        return prefPasswordSeconds;
    }

    private void setPrefPasswordSeconds (int passSec)
    {
        passSec = Math.max(MIN_TIMER_VALUE, passSec);
        this.prefPasswordSeconds = passSec;
        progressPassTimer.setMaximum(prefPasswordSeconds);
        startTimer();
    }

    private void revalidateAllKids (Component component)
    {
        if (component instanceof Container)
        {
            Container container = (Container) component;
            for (int i = 0; i < container.getComponentCount(); ++i)
            {
                revalidateAllKids(container.getComponent(i));
            }
        }
        component.invalidate();
        component.repaint();
    }

    private void setPrefUseFavorites (boolean useFavorites)
    {
        this.prefUseFavoriteList = useFavorites;
        buttonClearFavorites.setVisible(useFavorites);

        // Desperation actions to repaint the hierachy correctly
        refreshSecretList();
        refreshFavorites();
    }

    public static void toClipboard (String clipBoardText, String whatInfo, boolean startTimer)
    {
        FgGPGProcess.clip(clipBoardText);
        MainFrame.getInstance().setUserTextareaStatus("Copied " + whatInfo + " to clipboard");
        if (startTimer)
        {
            MainFrame.getInstance().startTimer(true);
        }
    }

    public void clearTextArea (boolean setFocus)
    {
        clearTextArea(toDecode.isEmpty() ? "" : "Cleared contents of " + toDecode);
        if (setFocus)
        {
            fgTextFilter.requestFocus();
        }
    }

    private void clearTextArea (String err)
    {
        fgPanelTextArea.clear(err);
        stopClearTimer();
    }

    public void startTimer ()
    {
        startTimer(null);
    }

    public void startTimer (Boolean clipboard)
    {

        if (clipboard != null)
        {
            this.clipboard = clipboard;
        }

        if (!fgPanelTextArea.isClear() || this.clipboard)
        {

            clearSeconds = 0;
            progressClearTimer.setMaximum(getClearSeconds());
            progressClearTimer.setValue(0);
            File tmp = new File(toDecode);
            progressClearTimer.setString(String.format(
                    "Results of %s will be cleared in %d seconds", tmp.getName(),
                    (getClearSeconds() - clearSeconds)));
            clearTimer.restart();
            progressClearTimer.setVisible(true);
        }
        else
        {
            stopClearTimer();
        }

        startPassTimer();
        setPassStatusText();
    }

    /**
     * Returns configured value to either clear text area or flush clipboard.
     *
     * @return
     */
    private int getClearSeconds ()
    {
        if (this.clipboard)
        {
            return getPrefClipboardSeconds();
        }
        return getPrefClearSeconds();
    }

    private void stopClearTimer ()
    {
        if (clearTimer != null && clearTimer.isRunning())
        {
            clearTimer.stop();
            clearTextArea(false);
        }
        progressClearTimer.setVisible(false);
        FgGPGProcess.clearClipboardIfNotChanged();
        this.clipboard = false;
    }

    private void startPassTimer ()
    {
        passSeconds = 0;
        if (!passDlg.getPassPhrase().isEmpty())
        {
            passTimer.restart();
        }
        setPassStatusText();
    }

    private void stopPassTimer ()
    {
        passDlg.setPassPhrase("");
        clearTextArea("Cleared passphrase");
        passTimer.stop();
        setPassStatusText();
    }

    private void setPassStatusText ()
    {
        if (passDlg.getPassPhrase() == null || passDlg.getPassPhrase().isEmpty())
        {
            progressPassTimer.setVisible(false);
        }
        else
        {
            progressPassTimer.setVisible(true);
            if (passTimer.isRunning())
            {
                String time;
                int rest = prefPasswordSeconds - passSeconds;
                if (rest > 60)
                {

                    int min = rest / 60;
                    int sec = rest - (min * 60);
                    time = String.format("%02d:%02dmin", min, sec);
                }
                else
                {
                    time = String.format("00:%02dmin", rest);
                }
                progressPassTimer.setValue(passSeconds);
                progressPassTimer.setString(String.format(
                        "Passphrase will be cleared in %s", time));
            }
            else
            {
                progressPassTimer.setString("Passphrase is stored ");
            }
        }
    }

    private void decrypt ()
    {
        this.decrypt(jListSecrets.getSelectedValue());
    }

//    private void decrypt (JList jList)
//    {
//        FgGPGProcess.clearClipboardIfNotChanged();
//        this.decrypt(false, jList.getSelectedValue(), null);
//        fgPanelTextArea.requestFocus();
//    }

    private void decrypt (boolean toClipboard)
    {
        this.decrypt(toClipboard, jListSecrets.getSelectedValue(), null);
    }

    private void decrypt (Object clientData)
    {
        this.decrypt(false, jListSecrets.getSelectedValue(), clientData);
    }

    private boolean clipboard = false;

    public void decrypt (File file)
    {
        try
        {
            decrypt(false, file.getCanonicalPath(), null);
        }
        catch (IOException e)
        {
            setUserTextareaStatus("Failed to decrypt " + file.getName());
        }
    }
    private void decrypt (boolean toClipboard, Object filename, Object clientData)
    {
        this.clipboard = toClipboard;
        boolean isPassDialog = gpgProcess.isPrefUsePasswordDialog();
        if (isPassDialog && passDlg.getPassPhrase().isEmpty())
        {
            passDlg.setLocationRelativeTo(this);
            passDlg.setVisible(true);
        }
        if (!passDlg.getPassPhrase().isEmpty() || !isPassDialog)
        {
            if (filename instanceof String && !((String) filename).isEmpty())
            {
                toDecode = (String) filename;
                if (toDecode == null || toDecode.isEmpty())
                {
                    handleGpgResult("", "Nothing selected to decode ...");
                    return;
                }
                handleGpgResult("", "Decoding " + toDecode + "...");
                String decryptEntry = (String) filename;
                handleForFavoritesList(decryptEntry);

                gpgProcess.decrypt(decryptEntry, passDlg.getPassPhrase(),
                        toClipboard, getPrefClipboardSeconds(), clientData);
                startTimer();
                setPassStatusText();
            }
        }
    }


    private Map<String, String> getVersionFromManifest ()
    {
        Map<String, String> result = new HashMap<>();
        URL url = this.getClass().getResource("/org/mockenhaupt/fortgnox/version.txt");
        Pattern fieldPattern = Pattern.compile("^\\s*([^:]*)\\s*:\\s*(.*)\\s*$", Pattern.CASE_INSENSITIVE);
        try
        {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                while (br.ready()) {
                    Matcher matcher = fieldPattern.matcher(br.readLine());
                    if (matcher.matches() && matcher.groupCount() == 2) {
                        result.put(matcher.group(1).trim(), matcher.group(2).trim());
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Creates new form MainFrame
     */
    private MainFrame ()
    {
        gpgProcess = new FgGPGProcess();

        FgPreferences.get().addPropertyChangeListener(this);
        initComponents();

        URL url = this.getClass().getResource("fortGnox48.png");
        this.setIconImage(Toolkit.getDefaultToolkit().createImage(url));

        progressClearTimer.setMaximum(prefClearSeconds);
        progressPassTimer.setMaximum(prefPasswordSeconds);
        clearTimer = new Timer(1000, this);
        passTimer = new Timer(1000, this);
        stopClearTimer();


        passDlg = new PassphraseDialog(this, true);
        setPassStatusText();

        initSecretListEventHandling(jListSecrets);

        editWindow = new EditWindow(this, gpgProcess, this);

        gpgProcess.addSecretListListener(this);
        gpgProcess.addResultListener(this);
        setUsePasswordDialog(gpgProcess.isPrefUsePasswordDialog());

        fgOptionsDialog = new FgOptionsDialog(this);

        addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved (MouseEvent e)
            {
                super.mouseMoved(e);
                startTimer();
            }
        });

        loadPreferences();
        updateTbButtonTexts();

        initSecretListCellRenderer();
        initSecretListFont();
        handleListSelection();

        setSize(880, 640);

        SwingUtilities.invokeLater(()->handleUiWidth());
    }


    private void initSecretListEventHandling (JList jList)
    {
        jList.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased (KeyEvent e)
            {
                super.keyReleased(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    decrypt();
                }
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    fgTextFilter.setText("");
                    fgTextFilter.requestFocus();
                }
            }
        });


        jList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked (MouseEvent e)
            {
                super.mouseClicked(e);
                if (clearTimer.isRunning())
                {
                    startTimer();
                }
                startPassTimer();

                if (e.getClickCount() == 2 && !editMode)
                {
                    decrypt();
                }
            }

            public void mousePressed (MouseEvent e)
            {
                if ((SwingUtilities.isRightMouseButton(e) || e.getButton() == 2) && !editMode)
                {
                    JList list = (JList) e.getSource();
                    int row = list.locationToIndex(e.getPoint());
                    if (list.getModel().getElementAt(row) instanceof String)
                    {
                        list.setSelectedIndex(row);
                    }
                }

                maybeShowPopup(e);
            }

            public void mouseReleased (MouseEvent e)
            {
                maybeShowPopup(e);
                if (e.getButton() == 2 && favorites.containsKey(jList.getSelectedValue()))
                {
                    favorites.remove(jList.getSelectedValue());
                    refreshFavorites();
                    jList.setSelectedIndex(-1);
                    FgPreferences.get().put(PREF_FAVORITES, favoritesAsJson());
                }
            }

            private void maybeShowPopup (MouseEvent e)
            {
                if (e.isPopupTrigger() && !editMode)
                {
                    if (jList.getSelectedValue() != null)
                    {
                        JPopupMenu popupMenu = getSecretsPopupMenu();
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

        });

        jList.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased (KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown())
                {
                    e.consume();
                    decrypt(true, jList.getSelectedValue(), null);
                }
                else
                {
                    super.keyReleased(e);
                }
            }
        });

        jList.addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved (MouseEvent e)
            {
                super.mouseMoved(e);
                startTimer();
            }
        });
        jListSecrets.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged (ListSelectionEvent e)
            {
                handleListSelection();
            }
        });
        jListSecrets.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped (KeyEvent e)
            {
                super.keyTyped(e);
                super.keyTyped(e);
                if (((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
                        || ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0))
                {
                    return;
                }
                fgTextFilter.setText(fgTextFilter.getText() + e.getKeyChar());
                fgTextFilter.requestFocus();
            }
        });
    }

    private void initSecretListFont ()
    {
        jListSecrets.setFont(jListSecrets.getFont().deriveFont((float) prefSecretListFontSize));
    }


    public JPopupMenu getSecretsPopupMenu ()
    {
        return getSecretsPopupMenu(false);
    }

    public void executePostCommand ()
    {
        String postCommand = FgPreferences.get().get(PREF_GPG_POST_COMMAND);
        boolean doPostCommand = postCommand != null && !postCommand.isEmpty();

        if (doPostCommand)
        {
            if (JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Execute post command '" + postCommand +"'?",
                    "Confirm post command execution",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null) == YES_OPTION)
            {
                fgPanelTextArea.setStatusText("Executing post command '" + postCommand + "' after tags change");
                gpgProcess.command(postCommand, "", this, (out, err, filename, clientData, exitCode) ->
                {
                    if (exitCode != 0)
                    {
                        fgPanelTextArea.setStatusText("Post command failure '" + out + "" + err + "'");
                        JOptionPane.showMessageDialog(MainFrame.this, "Output:" + out + " Error:" + err, "fortGnox POST", JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                        fgPanelTextArea.setStatusText("Post command success '" + out + " " + err + "'");
                        JOptionPane.showMessageDialog(MainFrame.this, out + err, "fortGnox POST", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        }
    }


    public JPopupMenu getSecretsPopupMenu (boolean launchedFromEditor)
    {
        if (editMode)
        {
            return new JPopupMenu();
        }

        JList jList = jListSecrets;
        JPopupMenu popupMenu = new JPopupMenu();
        boolean hasEntries = false;
        if (!launchedFromEditor)
        {
            hasEntries = true;

            JMenuItem miRefreshList = new JMenuItem("Reload password list from disk (F5)");
            miRefreshList.addActionListener(actionEvent ->
            {
                gpgProcess.rebuildSecretList();
            });
            popupMenu.add(miRefreshList);


            if (favorites.containsKey(jList.getSelectedValue()))
            {
                popupMenu.add(new JSeparator());
                JMenuItem miRemoveFavorites = new JMenuItem("Remove selected entry from favorites");
                miRemoveFavorites.addActionListener(actionEvent ->
                {
                    favorites.remove(jList.getSelectedValue());
                    refreshFavorites();
                    jList.setSelectedIndex(-1);
                    FgPreferences.get().put(PREF_FAVORITES, favoritesAsJson());
                });
                popupMenu.add(miRemoveFavorites);

                JMenuItem miCompressFavs = new JMenuItem("Compress favorites");
                miCompressFavs.addActionListener(actionEvent ->
                {
                    if (!editMode) compressFavorites();
                });
                popupMenu.add(miCompressFavs);
                // --------
                JMenuItem miRemoveFavs = new JMenuItem("Remove all favorites");
                miRemoveFavs.addActionListener(getFortGnoxDeleteFavorites());
                popupMenu.add(miRemoveFavs);
            }


            JMenuItem miToggleSort = new JMenuItem("Toggle sort order of list");
            miToggleSort.addActionListener(a ->
            {
                boolean sortReverse = FgPreferences.get().getBoolean(PREF_SECRETDIR_SORTING);
                FgPreferences.get().put(PREF_SECRETDIR_SORTING, !sortReverse);
            });
            popupMenu.add(new JSeparator());
            popupMenu.add(miToggleSort);
        }

        if (hasEntries)
        {
            popupMenu.add(new JSeparator());
        }

        String editEntry = launchedFromEditor ? toDecode : jList.getSelectedValue().toString();
        if (editEntry != null && !editEntry.isEmpty())
        {
            String shortName = FilenameUtils.getBaseName(editEntry);
            JMenuItem miEdit = new JMenuItem("Edit file \"" + shortName + "\"");
            miEdit.addActionListener(actionEvent ->
            {
                decrypt(false, editEntry, CLIENTDATA_EDIT);
            });
            popupMenu.add(miEdit);


            // Tags menu
            JMenuItem miTags = new JMenuItem("Edit tags for \"" + shortName + "\"");
            miTags.addActionListener(e ->
            {
                String newTags = (String) JOptionPane.showInputDialog(
                        this,
                        "Add / edit alias search tags for file \"" + shortName + "\"\n"
                        + "(white space separated list)",
                        "Tags for " + shortName,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        TagsStore.getTagsOfFile(editEntry, true));

                if (newTags != null)
                {
                    try
                    {
                        TagsStore.saveTagsForFile(editEntry, newTags);
                        executePostCommand();
                    }
                    catch (IOException ex)
                    {
                        handleGpgResult("", "Error saving tags, " + ex.getMessage());
                    }
                }
            });
            popupMenu.add(miTags);
        }


        return popupMenu;
    }


    public Map<String, String> getSecretsList ()
    {
        Map<String, String> dict = new HashMap<>();
        Arrays.stream(allSecretFiles).forEach(s -> {
            File f = new File(s);
            dict.put(s, f.getName());
        });
        return dict;
    }


    private void initSecretListCellRenderer ()
    {
        jListSecrets.setCellRenderer(new DefaultListCellRenderer()
        {

            @Override
            public Component getListCellRendererComponent (
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus)

            {
                Component c = super.getListCellRendererComponent(list, value, index,
                        isSelected,
                        false);

                if (value instanceof String)
                {
                    if (prefUseFavoriteList && favorites.containsKey(value) && index < filteredFavorites.size())
                    {
                        String info = (prefShowFavoritesCount ? "" + favorites.get(value) : null);
                        setText(gpgProcess.getShortFileName((String) value, info, true));
                    }
                    else
                    {
                        String tags = "";
                        if (prefShowSearchTags)
                        {
                            tags = TagsStore.getTagsOfFile((String) value);
                            if (!tags.isEmpty())
                            {
                                tags = " " + tags;
                            }
                        }
                        setText(gpgProcess.getShortFileName((String) value, false) + tags);
                    }
                }
                else if (value instanceof JSeparator)
                {
                    return (Component) value;
                }
                if (filteredFavorites.contains(value) && prefUseFavoriteList)
                {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
                else
                {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));

                }
                return this;
            }

        });
    }

    /**
     * Avoid dependencies to Jackson etc. by parsing simple JSON "by hand"
     *
     * @param json
     * @return
     */
    private void favoritesParseFromJson (String json)
    {
        Pattern p = Pattern.compile("\"([^\"]+)\":([^,}]*)[,}]");
        Matcher m = p.matcher(json);
        while (m.find())
        {
            String fname = m.group(1);
            if (new File(fname).isFile())
            {
                favorites.put(fname, Integer.parseInt(m.group(2)));
            }
            else
            {
                dbg(FAV, "Removed favorite " + fname + ", file does not exist");
            }
        }

        // rewrite loaded and filtered (missing files) favorites back to disk
        FgPreferences.get().put(PREF_FAVORITES, favoritesAsJson());

        refreshFavorites();
    }

    private String favoritesAsJson ()
    {
        StringBuilder jsonBuilder = new StringBuilder();
        Iterator<Map.Entry<String, Integer>> iter = favorites.entrySet().iterator();
        jsonBuilder.append("{");
        while (iter.hasNext())
        {
            Map.Entry<String, Integer> entry = iter.next();
            jsonBuilder.append('"');
            jsonBuilder.append(entry.getKey());
            jsonBuilder.append('"');
            jsonBuilder.append(':');
            jsonBuilder.append(entry.getValue());
            if (iter.hasNext())
            {
                jsonBuilder.append(',');
            }
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

//    private String favoritesAsJsonX ()
//    {
//        String json = "";
//        try
//        {
//            ObjectMapper objectMapper = new ObjectMapper();
//
//            json = objectMapper.writeValueAsString(favorites);
//
//        }
//        catch (JsonProcessingException e)
//        {
//            e.printStackTrace();
//        }
//        return json;
//    }

    private void sortFavorites ()
    {
        final LinkedHashMap<String, Integer> newFavorites = new LinkedHashMap<>(favorites);

        favorites.clear();

        newFavorites.entrySet()
                .stream().sorted((t2, t1) -> t1.getValue() - t2.getValue())
                .forEach(stringIntegerEntry ->
                {
                    favorites.put(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());
                });

        FgPreferences.get().put(PREF_FAVORITES, favoritesAsJson());
    }

    private void compressFavorites ()
    {
        final LinkedHashMap<String, Integer> oldFavorites = new LinkedHashMap<>(favorites);
        int high = prefNumberFavorites >0 ? prefNumberFavorites : favorites.size();
        high = Math.min(high, favorites.size());
        AtomicInteger i = new AtomicInteger(high + 1 + prefFavoritesMinHitCount);

        favorites.clear();

        oldFavorites.entrySet()
                .stream().sorted((t2, t1) -> t1.getValue() - t2.getValue())
                .forEach(stringIntegerEntry ->
                {
                    if (i.get() > 0)
                    {
                        favorites.put(stringIntegerEntry.getKey(), i.decrementAndGet());
                    }
                });
        refreshFavorites();
        FgPreferences.get().put(PREF_FAVORITES, favoritesAsJson());
    }


    private void handleForFavoritesList (String entry)
    {
        final LinkedHashMap<String, Integer> oldFavorites = new LinkedHashMap<>(favorites);


        Integer i = favorites.computeIfAbsent(entry, s -> 0);

        favorites.put(entry, ++i);
        if (favoritesAsJson().length() >= Preferences.MAX_VALUE_LENGTH)
        {
//            if (JOptionPane.showConfirmDialog(MainFrame.this,
//                    "Favorites exceeded max storable size, compress favorites?\n" +
//                            "Chosing cancel will ignore current selection for favorites",
//                    "Compact Favorites", OK_CANCEL_OPTION) == OK_OPTION)
//            {
                compressFavorites();
//            }
//            else
//            {
//                favorites.clear();
//                favorites.putAll(oldFavorites);
//                return;
//            }
        }

        dbg(FAV, entry + " weight:" + i);

        sortFavorites();
        refreshFavorites();
    }

    private void refreshFavorites ()
    {
        favoritesList.clear();
        favoritesList.addAll(favorites.keySet()
                .stream()
                .filter(s ->
                {
                    boolean isFav = isFavoritesPrefMinHits(s);
                    // ensure to show only favorites from one of the secret dirs
                    // which may have changed in preferences
                    return isFav && gpgProcess.getSecretdirs().stream().filter(s1 -> s.startsWith(s1)).findFirst().isPresent();
                })
                .collect(Collectors.toList())
        );
        refreshSecretList();
    }

    private boolean isFavoritesPrefMinHits (String file)
    {
        return favorites.get(file) >= prefFavoritesMinHitCount;
    }

    public void refreshSecretList ()
    {
        handleSecretList(this.allSecretFiles);
    }


    final String sepChar = Pattern.quote(File.separator);
    private Pattern pattern = Pattern.compile("^.*[^" + sepChar + "]+" + sepChar + "(.*$)");

    private boolean filterFile (String fileName)
    {
        if (fgTextFilter.getText() == null || fgTextFilter.getText().isEmpty())
        {
            return true;
        }
        Matcher m = pattern.matcher(fileName);
        if (m.matches())
        {
            String needle = fgTextFilter.getText().trim();

            // all tags concatenated w/o blanks
            String tags = TagsStore.getTagsOfFile(fileName, true);
            tags = tags.replaceAll("\\s", "");

            String baseName = m.group(1);
            String name2 = baseName.toLowerCase().replace(".asc", "");

            // simply concat tags and filename and look in all
            name2 = name2.toLowerCase().replace(".gpg", "");
            name2 += tags;
            boolean ret = StringUtils.andMatcher(name2, needle);
            dbg(FILTER, needle + (ret ? " match   " : " nomatch ") + fileName);
            return ret;

        }
        else
        {
            dbg(FILTER, "matcher matches() failed " + fileName);
        }
        return false;
    }

    private void dbg (DebugWindow.Category cat, String text)
    {
        DebugWindow.get().debug(cat, text);
    }

    private List<String> getConfiguredNumberFavorites ()
    {
        if (prefNumberFavorites <= 0)
        {
            return favoritesList;
        }
        else
        {
            return favoritesList.subList(0, Math.min(favoritesList.size(), prefNumberFavorites));
        }
    }

    @Override
    public void handleSecretList (String[] list)
    {
        this.allSecretFiles = list;

        filteredFavorites.clear();
        filteredFavorites.addAll(
                getConfiguredNumberFavorites()
                        .stream()
                        .filter(s -> (!prefFilterFavoriteList || filterFile(s)) && new File(s).exists())
                        .collect(Collectors.toList()));

        secretListModel.clear();
        secretListModel.addAll(Arrays.asList(list).stream().filter(s -> filterFile(s)).collect(Collectors.toList()));

        String secretListInfo = "";

        secretListInfo = " " + secretListModel.size() + " entries";
        jListSecrets.setModel(new AbstractListModel()
        {
            public int getSize ()
            {
                int size;
                if (prefUseFavoriteList)
                {
                    size = secretListModel.size() + filteredFavorites.size() + (filteredFavorites.size() > 0 ? 1 : 0);
                }
                else
                {
                    size = secretListModel.size();
                }

                dbg(LIST, "model size secret list: " + size);
                return size;
            }

            public Object getElementAt (int i)
            {
                Object val;
                try
                {
                    if (prefUseFavoriteList)
                    {
                        int favSize = filteredFavorites.size();

                        if (favSize > 0)
                        {
                            if (i == favSize)
                            {
                                val = new JSeparator(JSeparator.HORIZONTAL);
                            }
                            else if (i < favSize)
                            {
                                val = filteredFavorites.get(i);
                            }
                            else
                            {
                                val = secretListModel.get(i - favSize - 1);
                            }
                        }
                        else
                        {
                            val = secretListModel.get(i);
                        }
                    }
                    else
                    {
                        val = secretListModel.get(i);
                    }
                }
                catch (Exception e)
                {
                    return "ix:" + i + " " + e.getMessage();
                }
                dbg(LIST, "list ix: " + i + " value: " + val);
                return val;

            }
        });

        updateSecretListInfo("");


        updateSecretListInfo(secretListInfo);
    }

    private void updateSecretListInfo (String text)
    {
        this.labelSecretInfo.setText(text);
        this.labelSecretInfo.setVisible(text != null && !text.isEmpty());
    }

    @Override
    public void handleGpgResult (String out, String err)
    {
        handleGpgResult(out, err, "");
    }

    public void handleGpgResult (String out, String err, String fileName)
    {
        String showFilename = fileName;
        if (fileName != null)
        {
            File fName = new File(fileName);
            showFilename = fName.getName();
        }
        fgPanelTextArea.setText(out, err, showFilename, fileName);

        startTimer();
        SwingUtilities.invokeLater(() -> fgPanelTextArea.requestFocus());
    }

    static String lastFileName = null;
    @Override
    public void handleGpgResult (String out, String err, String filename, Object clientData)
    {
        if (filename != null && !filename.isEmpty())
        {
            jListSecrets.setSelectedValue(filename, true);

            if (lastFileName != null && !history.equals(clientData))
            {
                if (!filename.equals(lastFileName))
                {
                    history.add(lastFileName);
                }
            }
            lastFileName = filename;


            // edit file
            if (CLIENTDATA_EDIT.equals(clientData))
            {
                editWindow.setText(out, "Loaded for editing " + filename, filename);
                setEditMode(true);
                clearTextArea(true);
                return;
            }
            else
            {
                setEditMode(false);
            }
        }
        handleGpgResult(out, err, filename);
    }

    private void setEditMode (boolean editMode)
    {
        if (this.editMode != null && editMode == this.editMode)
        {
            return;
        }
        this.editMode = editMode;

        // may not have been created in initialization phase
        if (editWindow != null)
        {
            editWindow.reset();
        }

        if (editMode && editWindow != null)
        {
            jSplitPaneLR.setRightComponent(editWindow.getTextArea());
        }
        else
        {
            jSplitPaneLR.setRightComponent(fgPanelTextArea);
            fgTextFilter.requestFocus();
        }
        setEnabledHierachy(jToolBarMainFunctions, !editMode);
        setEnabledHierachy(scrollPaneSecrets, !editMode);
    }

    private void setEnabledHierachy (Component c, boolean enabled)
    {
        if (c instanceof Container && ((Container) c).getComponents().length > 0)
        {
            for (Component cc : ((Container) c).getComponents())
            {
                c.setEnabled(enabled);
                setEnabledHierachy(cc, enabled);
            }
        }
        else
        {
            c.setEnabled(enabled);
        }
    }

    private void setUserTextareaStatus (String err)
    {
        fgPanelTextArea.setText(null, err);
    }

    private void updateClearPassVisibility ()
    {
        buttonClearPass.setVisible(gpgProcess.isPrefUsePasswordDialog() || gpgProcess.isPrefConnectToGpgAgent());
    }


    private void setUsePasswordDialog (boolean mode)
    {
        updateClearPassVisibility();
        progressPassTimer.setVisible(mode);
    }


    private ImageIcon getIcon (String path, int size)
    {
        return FileUtils.getScaledIcon(new ImageIcon(getClass().getResource(path)), size, size);
    }

    private ImageIcon getIcon (String path)
    {
        return getIcon(path, 28);
    }


    private void optionalSetText (Consumer<String> setter, String value)
    {
        if (prefShowToobarTexts)
        {
            setter.accept(value);
        }
        else
        {
            setter.accept(null);
        }
    }

    private void updateTbButtonTexts ()
    {
        optionalSetText(s -> buttonNew.setText(s), "New Password");
        optionalSetText(s -> buttonEdit.setText(s), "Edit Password");
//        optionalSetText(s -> buttonClearTextarea.setText(s), "Wipe Textarea");
        optionalSetText(s -> buttonClearPass.setText(s), "Forget Passphrase");
        optionalSetText(s -> buttonHistoryForward.setText(s), "Forward");
        optionalSetText(s -> buttonHistoryBackward.setText(s), "Backward");
        optionalSetText(s -> buttonClearFavorites.setText(s), "Clear Favorites");
        optionalSetText(s -> jButtonSettings.setText(s), "Open Settings");
        optionalSetText(s -> buttonAbout.setText(s), "About");
        optionalSetText(s -> buttonExit.setText(s), "Exit");

        handleUiWidth();
    }

    private void handleUiWidth ()
    {
        jToolBarMainFunctions.revalidate();
        int minWidth = jToolBarMainFunctions.getMinimumSize().width + aboutPanel.getMinimumSize().width + 20;
        setMinimumSize(new Dimension(minWidth, 400));
    }

    private void handleListSelection ()
    {
        buttonEdit.setEnabled(jListSecrets.getSelectedValue() != null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents ()
    {
        //        ToolTipManager.sharedInstance().setInitialDelay(100);
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher()
                {
                    @Override
                    public boolean dispatchKeyEvent(KeyEvent e)
                    {
                        if (e.getKeyCode() == KeyEvent.VK_F5 && e.getID() == KeyEvent.KEY_RELEASED)
                        {
                            gpgProcess.rebuildSecretList();
                        }
                        return false;
                    }
                });

        jSplitPaneLR = new JSplitPane();
        JPanel panelList = new JPanel();
        scrollPaneSecrets = new JScrollPane();

        // gpg files and favorite gpg files

        labelSecretInfo = new JLabel();
        jListSecrets = new JList();
        jToolBarPanel = new JPanel(new BorderLayout());
        jToolBarMainFunctions = new JToolBar();
        jToolBarMainFunctions.setFloatable(false);
        jToolBarMainFunctions.setFocusable(false);
        buttonClearPass = new JButton();
        buttonHistoryForward = new JButton();
        buttonHistoryBackward = new JButton();
        history = new History(buttonHistoryBackward, buttonHistoryForward);

        buttonClearFavorites = new JButton();
        buttonClearPass.setMnemonic(KeyEvent.VK_P);
//        buttonClearTextarea = new JButton();
        buttonExit = new JButton();
        JToggleButton buttonOptions = new JToggleButton();
        buttonAbout = new JButton();
        buttonExit.setMnemonic(KeyEvent.VK_X);
        buttonOptions.setMnemonic(KeyEvent.VK_O);
        jButtonSettings = new JButton();
        jButtonSettings.setMnemonic(KeyEvent.VK_S);
        jButtonSettings.setToolTipText("Open fortGnox preferences");
        JButton jButtonClipboard = new JButton();
        jButtonClipboard.setMnemonic(KeyEvent.VK_C);
        buttonNew = new JButton();
        buttonEdit = new JButton();
        JPanel statusBarPanel = new JPanel();
        progressClearTimer = new JProgressBar();
        progressPassTimer = new JProgressBar();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing (WindowEvent e)
            {
                buttonExitActionPerformed(null);
            }
        });

        setTitle("fortGnox");
//        setMinimumSize(new java.awt.Dimension(800, 600));

        jSplitPaneLR.setBackground(new java.awt.Color(204, 204, 204));
        jSplitPaneLR.setMinimumSize(new java.awt.Dimension(200, 102));
        jSplitPaneLR.setPreferredSize(new java.awt.Dimension(400, 400));

        panelList.setMinimumSize(new java.awt.Dimension(200, 20));
        panelList.setPreferredSize(new java.awt.Dimension(300, 246));
        panelList.setLayout(new java.awt.BorderLayout());


        fgTextFilter = new FgTextFilter(this);
        panelList.add(fgTextFilter, java.awt.BorderLayout.NORTH);

        jListSecrets.setToolTipText("Press CTRL+C to decode first line to clipboard");

        scrollPaneSecrets.setViewportView(jListSecrets);
        panelList.add(scrollPaneSecrets, java.awt.BorderLayout.CENTER);
        panelList.add(labelSecretInfo, BorderLayout.SOUTH);
        labelSecretInfo.setVisible(false);
        labelSecretInfo.setFocusable(false);

        jSplitPaneLR.setLeftComponent(panelList);

        fgPanelTextArea = new FgPanelTextArea(this);

        setEditMode(false);

        getContentPane().add(jSplitPaneLR, java.awt.BorderLayout.CENTER);

        jToolBarMainFunctions.setRollover(true);


        jToolBarMainFunctions.add(buttonHistoryBackward);
        buttonHistoryBackward.setToolTipText("TODO");
        buttonHistoryBackward.setBorderPainted(false);
        buttonHistoryBackward.setFocusable(false);
        buttonHistoryBackward.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonHistoryBackward.setVerticalTextPosition(SwingConstants.BOTTOM);
        buttonHistoryBackward.setIcon(getIcon("/org/mockenhaupt/fortgnox/backward48.png"));
        buttonHistoryBackward.addActionListener(e -> {
            String lastFile = history.popBack(jListSecrets.getSelectedValue());
            if (lastFile != null)
            {
                decrypt (false, lastFile, history);
            }
        });


        jToolBarMainFunctions.add(buttonHistoryForward);
        buttonHistoryForward.setIcon(getIcon("/org/mockenhaupt/fortgnox/forward48.png"));
        buttonHistoryForward.setToolTipText("TODO");
        buttonHistoryForward.setBorderPainted(false);
        buttonHistoryForward.setFocusable(false);
        buttonHistoryForward.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonHistoryForward.setVerticalTextPosition(SwingConstants.BOTTOM);
        buttonHistoryForward.addActionListener(e -> {
            String lastFile = history.popForward(jListSecrets.getSelectedValue());
            if (lastFile != null)
            {
                decrypt (false, lastFile, history);
            }
        });

        // ---------------------------------




        buttonNew.setIcon(getIcon("/org/mockenhaupt/fortgnox/addplus48.png"));
        buttonNew.setMnemonic('n');
        buttonNew.setFocusable(false);
        buttonNew.setToolTipText("Insert new password");
        buttonNew.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonNew.setVerticalTextPosition(SwingConstants.BOTTOM);
        buttonNew.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed (java.awt.event.ActionEvent evt)
            {
                editWindow.showNew();
            }
        });
        jToolBarMainFunctions.add(buttonNew);

        buttonEdit.setIcon(getIcon("/org/mockenhaupt/fortgnox/edit48.png"));
        buttonEdit.setMnemonic('d');
        buttonEdit.setFocusable(false);
        buttonEdit.setToolTipText("Edit selected password");
        buttonEdit.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonEdit.setVerticalTextPosition(SwingConstants.BOTTOM);
        buttonEdit.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                if (jListSecrets.getSelectedValue() instanceof String)
                {
                    if (JOptionPane.showConfirmDialog(MainFrame.this,
                            "The file content will be shown unencrypted, continue?",
                            "Confirm edit", OK_CANCEL_OPTION) == OK_OPTION)
                    {
                        decrypt(false, jListSecrets.getSelectedValue(), CLIENTDATA_EDIT);
                    }
                }
            }
        });
        jToolBarMainFunctions.add(buttonEdit);


        jButtonClipboard.setIcon(getIcon("/org/mockenhaupt/fortgnox/clipboard.png")); // NOI18N
//        jButtonClipboard.setText("Clipboard First Line");
        jButtonClipboard.setToolTipText("Save the first line of decoded file to clipboard");
        jButtonClipboard.setFocusable(false);
        jButtonClipboard.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonClipboard.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonClipboard.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed (java.awt.event.ActionEvent evt)
            {
                jButtonClipboardActionPerformed(evt);
            }
        });
//        jToolBar1.add(jButtonClipboard);


        // ---------------------------------
//        buttonClearTextarea.setIcon(getIcon("/org/mockenhaupt/fortgnox/wipe48.png"));
//        buttonClearTextarea.setMnemonic(KeyEvent.VK_I);
//        buttonClearTextarea.setToolTipText("Clears the textarea and the clipboard in case a password has been stored there");
//        buttonClearTextarea.setBorderPainted(false);
//        buttonClearTextarea.setFocusable(false);
//        buttonClearTextarea.setHorizontalTextPosition(SwingConstants.CENTER);
//        buttonClearTextarea.setVerticalTextPosition(SwingConstants.BOTTOM);
//        buttonClearTextarea.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed (java.awt.event.ActionEvent evt)
//            {
//                buttonClearTextareaActionPerformed(evt);
//            }
//        });
//        jToolBarMainFunctions.add(buttonClearTextarea);

        // ---------------------------------

        buttonClearPass.setIcon(getIcon("/org/mockenhaupt/fortgnox/lock48.png"));
//        buttonClearPass.setText("Clear");
        buttonClearPass.setToolTipText("Clears the internally stored passphrase and optionally the GPG agenty password (see Settings)");
        buttonClearPass.setBorderPainted(false);
        buttonClearPass.setFocusable(false);
        buttonClearPass.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonClearPass.setVerticalTextPosition(SwingConstants.BOTTOM);
        buttonClearPass.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed (java.awt.event.ActionEvent evt)
            {
                buttonClearPassActionPerformed(evt);
            }
        });
        jToolBarMainFunctions.add(buttonClearPass);
        // ---------------------------------
        buttonClearFavorites.setIcon(getIcon("/org/mockenhaupt/fortgnox/bookmark48.png"));
//        buttonClearFavorites.setText("Clear Favorites");
        buttonClearFavorites.setToolTipText("Clears the favorites list");
        buttonClearFavorites.setBorderPainted(false);
        buttonClearFavorites.setFocusable(false);
        buttonClearFavorites.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonClearFavorites.setVerticalTextPosition(SwingConstants.BOTTOM);
        buttonClearFavorites.addActionListener(getFortGnoxDeleteFavorites());
//  XXXXX #30      jToolBarMainFunctions.add(buttonClearFavorites);

        // ---------------------------------


        buttonExit.setIcon(getIcon("/org/mockenhaupt/fortgnox/poweroff48.png"));
        buttonExit.setToolTipText("Exit fortGnox");
        buttonExit.setBorderPainted(false);
        buttonExit.setFocusable(false);
        buttonExit.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonExit.setVerticalTextPosition(SwingConstants.BOTTOM);
        buttonExit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed (java.awt.event.ActionEvent evt)
            {
                buttonExitActionPerformed(evt);
            }
        });

        buttonOptions.setIcon(getIcon("/org/mockenhaupt/fortgnox/toolbar.png")); // NOI18N
//        buttonOptions.setText("Text Options");
        buttonOptions.setBorderPainted(false);
        buttonOptions.setFocusable(false);
        buttonOptions.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonOptions.setVerticalTextPosition(SwingConstants.BOTTOM);
        buttonOptions.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed (java.awt.event.ActionEvent evt)
            {
                buttonOptionsActionPerformed(evt);
            }
        });

        fgPanelTextArea.setButtonToolbarVisible(buttonOptions.isSelected());

        jToolBarMainFunctions.add(jButtonSettings);

//        jToolBarMainFunctions.add(buttonOptions);
//        jToolBarMainFunctions.add(buttonExit);

        jButtonSettings.setIcon(getIcon("/org/mockenhaupt/fortgnox/sprocket48.png"));
//        jButtonSettings.setText("Settings");
        jButtonSettings.setFocusable(false);
        jButtonSettings.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonSettings.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonSettings.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed (java.awt.event.ActionEvent evt)
            {
                jButtonSettingsActionPerformed(evt);
            }
        });


        buttonAbout.setIcon(getIcon("/org/mockenhaupt/fortgnox/info48.png"));
        buttonAbout.setBorderPainted(false);
        buttonAbout.setFocusable(false);
        buttonAbout.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonAbout.setVerticalTextPosition(SwingConstants.BOTTOM);
        buttonAbout.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed (java.awt.event.ActionEvent evt)
            {
                buttonAboutActionPerformed(evt);
            }
        });

        // Separate toolbar for about dialog (ensures correct geometry handling)
        aboutPanel = new JToolBar();
        aboutPanel.setFloatable(false);
        aboutPanel.setFocusable(false);
        aboutPanel.add(buttonExit);
        aboutPanel.add(buttonAbout);
        jToolBarPanel.add(aboutPanel, BorderLayout.LINE_END);

        jToolBarPanel.add(jToolBarMainFunctions, BorderLayout.CENTER);
        getContentPane().add(jToolBarPanel, java.awt.BorderLayout.PAGE_START);

        setMinimumSize(new Dimension(jToolBarMainFunctions.getWidth() + aboutPanel.getWidth(), 400));
        statusBarPanel.setLayout(new java.awt.GridLayout(1, 0, 10, 0));

        progressClearTimer.setMinimumSize(new java.awt.Dimension(10, 25));
        progressClearTimer.setPreferredSize(new java.awt.Dimension(300, 25));
        progressClearTimer.setString("0s");
        progressClearTimer.setStringPainted(true);
        statusBarPanel.add(progressClearTimer);

        progressPassTimer.setMinimumSize(new java.awt.Dimension(10, 25));
        progressPassTimer.setPreferredSize(new java.awt.Dimension(300, 25));
        progressPassTimer.setString("0s");
        progressPassTimer.setStringPainted(true);
        statusBarPanel.add(progressPassTimer);

        getContentPane().add(statusBarPanel, java.awt.BorderLayout.PAGE_END);

        Vector<Component> focusComponentVector = new Vector<>();
        focusComponentVector.add(fgTextFilter);
        focusComponentVector.add(jListSecrets);
        focusComponentVector.add(fgPanelTextArea.getFocusComponent());
        FgFocusTraversalPolicy fgFocusTraversalPolicy = new FgFocusTraversalPolicy(focusComponentVector);
        this.setFocusTraversalPolicy(fgFocusTraversalPolicy);
        pack();
    }

    private ActionListener getFortGnoxDeleteFavorites ()
    {
        return new ActionListener()
        {
            public void actionPerformed (ActionEvent evt)
            {
                if (OK_OPTION == JOptionPane.showConfirmDialog(MainFrame.this, "Really delete all favorites?", "fortGnox Delete Favorites", OK_CANCEL_OPTION))
                {
                    favorites.clear();
                    FgPreferences.get().put(PREF_FAVORITES, favoritesAsJson());
                    refreshFavorites();
                }
            }
        };
    }


    private void buttonExitActionPerformed (java.awt.event.ActionEvent evt)
    {

        boolean doTerminate = !editWindow.isSecretTextModified() || JOptionPane.showConfirmDialog(this,
                "Unsaved text in edit window, really exit fortGnox?",
                "Confirm exit", OK_CANCEL_OPTION) == OK_OPTION;

        if (doTerminate)
        {
            FgGPGProcess.clearClipboardIfNotChanged();
            this.clipboard = false;
            System.exit(0);
            Stream.of(Window.getWindows()).forEach(Window::dispose);
        }
    }

    private void buttonOptionsActionPerformed (java.awt.event.ActionEvent evt)
    {
        if (evt.getSource() instanceof JToggleButton)
        {
            fgPanelTextArea.setButtonToolbarVisible(((JToggleButton) evt.getSource()).isSelected());
        }
    }

    private void buttonClearPassActionPerformed (java.awt.event.ActionEvent evt)
    {
        passDlg.setPassPhrase("");
        fgPanelTextArea.clear("Cleared");
        FgGPGProcess.clearClipboardIfNotChanged();
        this.clipboard = false;
        stopClearTimer();
        stopPassTimer();
        if (gpgProcess != null)
        {
            gpgProcess.gpgAgendCommand("--reload");
        }
        history.clear();
    }

    private void buttonAboutActionPerformed (java.awt.event.ActionEvent evt)
    {
        StringBuilder sb = new StringBuilder();

        String name = getMyVersionFromJar();
        if (name != null && !name.isEmpty())
        {
            sb.append(name);
            sb.append(FgGPGProcess.LINE_SEP);
        }

        URL is = getClass().getResource("/org/mockenhaupt/fortgnox/about.txt");

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader((is.openStream())));
            while (br.ready())
            {
                sb.append(br.readLine());
                sb.append(FgGPGProcess.LINE_SEP);
            }
        }
        catch (IOException ex)
        {
            sb.append("Internal Error: ").append(ex.getMessage());
        }




        JOptionPane.showMessageDialog(this,
                sb.toString(),
                "About fortGnox " + getVersionFromManifest().computeIfAbsent(VERSION_PROJECT, k -> "UNKNOWN"),
                JOptionPane.INFORMATION_MESSAGE,
                getIcon("/org/mockenhaupt/fortgnox/fortGnox128.png", 128));
    }

    private String getMyVersionFromJar ()
    {
        String name = "";

        try
        {
            String getJarPath = this.getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();
            name = new File(getJarPath).getName();
            name = name.replaceAll(".jar", "");
        }
        catch (Exception ex)
        {
            // ignore
            DebugWindow.get().debug(ANY, ex.getMessage());
        }
        return name;
    }


    private void jButtonSettingsActionPerformed (java.awt.event.ActionEvent evt)
    {
        fgOptionsDialog.setVisible(true);
    }

    private void jButtonClipboardActionPerformed (java.awt.event.ActionEvent evt)
    {
        decrypt(true);
    }

    @Override
    public void actionPerformed (ActionEvent e)
    {

        if (e.getSource() == clearTimer)
        {
            clearSeconds++;
            progressClearTimer.setValue(clearSeconds);
            File tmp = new File(toDecode);
            progressClearTimer.setString(String.format(
                    "Results of %s will be cleared in %d seconds", tmp.getName(),
                    (getClearSeconds() - clearSeconds)));

            if (clearSeconds >= getClearSeconds())
            {
                stopClearTimer();
                if (!passTimer.isRunning())
                {
                    startPassTimer();
                }
            }
        }
        else if (e.getSource() == passTimer)
        {
            passSeconds++;
            setPassStatusText();
            if (passSeconds >= prefPasswordSeconds)
            {
                stopPassTimer();
            }
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent propertyChangeEvent)
    {
        switch (propertyChangeEvent.getPropertyName())
        {
            case PREF_CLIP_SECONDS:
                setPrefClipboardSeconds((Integer) propertyChangeEvent.getNewValue());
                break;
            case PREF_USE_FAVORITES:
                setPrefUseFavorites((Boolean) propertyChangeEvent.getNewValue());
                break;
            case PREF_FILTER_FAVORITES:
                prefFilterFavoriteList = (boolean) propertyChangeEvent.getNewValue();
                refreshFavorites();
                break;
            case PREF_CLEAR_SECONDS:
                setPrefClearSeconds((Integer) propertyChangeEvent.getNewValue());
                break;
            case PREF_PASSWORD_SECONDS:
                setPrefPasswordSeconds((Integer) propertyChangeEvent.getNewValue());
                break;
            case PREF_FAVORITES_MIN_HIT_COUNT:
                prefFavoritesMinHitCount = (Integer) propertyChangeEvent.getNewValue();
                SwingUtilities.invokeLater(this::refreshFavorites);
                break;
            case PREF_NUMBER_FAVORITES:
                prefNumberFavorites = (Integer) propertyChangeEvent.getNewValue();
                SwingUtilities.invokeLater(this::refreshFavorites);
                break;
            case PREF_SECRETDIRS:
                SwingUtilities.invokeLater(this::refreshFavorites);
                break;
            case PREF_FAVORITES_SHOW_COUNT:
                prefShowFavoritesCount = (Boolean) propertyChangeEvent.getNewValue();
                refreshFavorites();
                break;
            case PREF_USE_SEARCH_TAGS:
                prefUseSearchTags = (Boolean) propertyChangeEvent.getNewValue();
                refreshSecretList();
                break;
            case PREF_SHOW_SEARCH_TAGS:
                prefShowSearchTags = (Boolean) propertyChangeEvent.getNewValue();
                refreshSecretList();
                break;
            case PREF_LOOK_AND_FEEL:
                LAFChooser.get().set((String) propertyChangeEvent.getNewValue(), INSTANCE);
                SwingUtilities.invokeLater(() -> handleUiWidth());
                break;
            case PREF_SHOW_TB_BUTTON_TEXT:
                prefShowToobarTexts = (boolean) propertyChangeEvent.getNewValue();
                SwingUtilities.invokeLater(this::updateTbButtonTexts);
                break;
            case PREF_HISTORY_SIZE:
                SwingUtilities.invokeLater(this::updateHistoryButtonVisibility);
                break;
            case PREF_SECRETLIST_FONT_SIZE:
                if (jListSecrets != null)
                {
                    prefSecretListFontSize = (Integer) propertyChangeEvent.getNewValue();
                    initSecretListFont();
                }
                break;
            case PREF_EXCEPTION:
                String msg = propertyChangeEvent.getNewValue().toString();
                int len = msg.length();
                if (msg.length() > 100) {
                    msg = msg.substring(0, 100);
                    msg = String.format("Failed to store propery of length '%d', %s ....", len, msg);
                }

                JOptionPane.showMessageDialog(MainFrame.this,
                        msg, propertyChangeEvent.getOldValue().toString(),
                        JOptionPane.ERROR_MESSAGE);

            default:
                // nothing to do
        }
        SwingUtilities.invokeLater(this::updateClearPassVisibility);
    }

    private void updateHistoryButtonVisibility ()
    {
        if (history != null)
        {
            buttonHistoryBackward.setVisible(history.isHistoryEnabled());
            buttonHistoryForward.setVisible(history.isHistoryEnabled());
        }
    }

    @Override
    public void handleFinished ()
    {
        setEditMode(false);
    }

    @Override
    public void handleNewFile (String fname)
    {
        setEditMode(true);
    }

    @Override
    public void handleTextFilterChanged (String filter)
    {
        refreshSecretList();
    }
}
