/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainFrame.java
 *
 * Created on 26.02.2011, 18:47:39
 */
package org.mockenhaupt.jgpg;


import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static org.mockenhaupt.jgpg.DebugWindow.Category.DIR;
import static org.mockenhaupt.jgpg.DebugWindow.Category.FAV;
import static org.mockenhaupt.jgpg.DebugWindow.Category.FILTER;
import static org.mockenhaupt.jgpg.DebugWindow.Category.LIST;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_CLEAR_SECONDS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_CLIP_SECONDS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_FAVORITES;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_FAVORITES_SHOW_COUNT;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_FILTER_FAVORITES;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_NUMBER_FAVORITES;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_PASSWORD_SECONDS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_SECRETDIRS;
import static org.mockenhaupt.jgpg.JgpgPreferences.PREF_USE_FAVORITES;

/**
 *
 * @author fmoc
 */
public class MainFrame extends javax.swing.JFrame implements
        JGPGProcess.SecretListListener,
        JGPGProcess.ResultListListener,
        ActionListener,
        PropertyChangeListener
{

    private final boolean DEBUG = false;
    private JGPGProcess gpgProcess;
//    private PassphraseDialog passDlg;
    private PassphraseDialog passDlg;
    final private Timer clearTimer;
    final private Timer passTimer;
    private int clearSeconds = 0;
    private int passSeconds = 0;
    private int CLEAR_SECONDS = 30;
    private int CLIP_SECONDS = 10;
    private int PASSWORD_SECONDS = 60 * 5;
    private final int CLEAR_SECONDS_DEFAULT = 30;
    private final int CLIP_SECONDS_DEFAULT = 10;
    private final int PASSWORD_SECONDS_DEFAULT = 60 * 5;
    private final int MIN_TIMER_VALUE = 1;
    private String toDecode = "";
    private JList lastActionList;
    private int NUMBER_FAVORITES = 8;
    private boolean prefShowFavoritesCount = false;

    private String[] allSecretFiles = new String[]{""};
    private final List<String> secretListModel = new ArrayList<>();

    private final LinkedHashMap<String, Integer> favorites = new LinkedHashMap<>();
    private final java.util.List<String> favoritesList = new ArrayList<>();
    final private List<String> filteredFavorites = new ArrayList<>();

    public static final String VERSION_PROJECT = "project.version";
    public static final String VERSION_BUILD = "buildNumber";

    /**
     * @param args the command line arguments
     */
    public static void main (String[] args)
    {
        java.awt.EventQueue.invokeLater(new Runnable()
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
                INSTANCE.setVisible(true);

            }
        });
    }


    static private MainFrame INSTANCE;
    private static MainFrame getInstance()
    {
        return INSTANCE;
    }



    private void loadPreferences ()
    {
        this.setTitle(this.getClass().getPackage().getName().toUpperCase() + " " +
                getVersionFromManifest().computeIfAbsent(VERSION_PROJECT, k -> "UNKNOWN"));

        PreferencesAccess preferences = JgpgPreferences.get();

        CLEAR_SECONDS = preferences.get(PREF_CLEAR_SECONDS, CLEAR_SECONDS_DEFAULT);
        CLIP_SECONDS = preferences.get(PREF_CLIP_SECONDS, CLIP_SECONDS_DEFAULT);
        PASSWORD_SECONDS = preferences.get(PREF_PASSWORD_SECONDS, PASSWORD_SECONDS_DEFAULT);
        NUMBER_FAVORITES = preferences.get(PREF_NUMBER_FAVORITES, NUMBER_FAVORITES);
        prefShowFavoritesCount = preferences.get(PREF_FAVORITES_SHOW_COUNT, prefShowFavoritesCount);
        prefFilterFavoriteList = preferences.get(PREF_FILTER_FAVORITES, prefFilterFavoriteList);
        setPrefUseFavorites(preferences.get(PREF_USE_FAVORITES, prefUseFavoriteList));
        favoritesParseFromJson(preferences.get(PREF_FAVORITES, "{}"));
    }

    private int getCLEAR_SECONDS()
    {
        return CLEAR_SECONDS;
    }

    private int getCLIP_SECONDS()
    {
        return CLIP_SECONDS;
    }

    private void setCLIP_SECONDS(int clipSeconds)
    {
        clipSeconds = Math.max(MIN_TIMER_VALUE, clipSeconds);
        this.CLIP_SECONDS = clipSeconds;
    }


    private void setCLEAR_SECONDS(int clearSec)
    {
        clearSec = Math.max(MIN_TIMER_VALUE, clearSec);
        this.CLEAR_SECONDS = clearSec;
        progressClearTimer.setMaximum(CLEAR_SECONDS);
        startTimer();
    }

    public int getPASSWORD_SECONDS ()
    {
        return PASSWORD_SECONDS;
    }
    
    private void setPASSWORD_SECONDS(int passSec)
    {
        passSec = Math.max(MIN_TIMER_VALUE, passSec);
        this.PASSWORD_SECONDS = passSec;
        progressPassTimer.setMaximum(PASSWORD_SECONDS);
        startTimer();
    }

    private void revalidateAllKids (Component component)
    {
        if (component instanceof Container)
        {
            Container container = (Container) component;
            for (int i = 0; i < container.getComponentCount(); ++i){
               revalidateAllKids(container.getComponent(i));
            }
        }
        component.invalidate();
        component.repaint();
    }

    int lastInc = 1;
    private void setPrefUseFavorites(boolean useFavorites)
    {
        this.prefUseFavoriteList = useFavorites;
        buttonClearFavorites.setVisible(useFavorites);

        // Desperation actions to repaint the hierachy correctly
        refreshSecretList();
        refreshFavorites();
    }


    public static void toClipboard (String clipBoardText, String whatInfo, boolean startTimer)
    {
        JGPGProcess.clip(clipBoardText);
        MainFrame.getInstance().setUserTextareaStatus("Copied " + whatInfo + " to clipboard");
        if (startTimer)
        {
            MainFrame.getInstance().startTimer(true);
        }
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

        if (!jPanelTextArea.isClear() || this.clipboard)
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
            return getCLIP_SECONDS();
        }
        return getCLEAR_SECONDS();
    }

    private void stopClearTimer ()
    {
        if (clearTimer.isRunning())
        {
            clearTimer.stop();
            clearUserTextArea("Cleared " + toDecode);
        }
        progressClearTimer.setVisible(false);
        JGPGProcess.clearClipboardIfNotChanged();
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
        clearUserTextArea("Cleared passphrase");
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
                int rest = PASSWORD_SECONDS - passSeconds;
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
        this.decrypt(lastActionList == null ? jListSecrets : lastActionList);
    }

    private void decrypt (JList jList)
    {
        JGPGProcess.clearClipboardIfNotChanged();
        this.decrypt(false, jList);
        jPanelTextArea.requestFocus();
    }
    private void decrypt (boolean toClipboard)
    {
        this.decrypt(toClipboard, lastActionList == null ? jListSecrets : lastActionList);
    }

    private boolean clipboard = false;
    private void decrypt (boolean toClipboard, JList jList)
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
            if (jList.getSelectedValue() instanceof String)
            {
                toDecode = (String) jList.getSelectedValue();
                if (toDecode == null || toDecode.isEmpty())
                {
                    setUserTextareaText("","Nothing selected to decode ...");
                    return;
                }
                setUserTextareaText("","Decoding " + toDecode + "...");
                String decryptEntry = (String) jList.getSelectedValue();
                handleForFavoritesList(decryptEntry);

                gpgProcess.decrypt(decryptEntry, passDlg.getPassPhrase(), toClipboard, getCLIP_SECONDS());
                startTimer();
                setPassStatusText();
            }
        }
    }
    
    private UIManager.LookAndFeelInfo[] lafs;
    private int currLafIX = 0;

    private OptionsPanel optionsPanel;


    private Map<String, String> getVersionFromManifest ()
    {
        Map<String, String> result = new HashMap<>();
        URL url = this.getClass().getResource("/org/mockenhaupt/jgpg/version.txt");
        Pattern fieldPattern = Pattern.compile("^\\s*([^:]*)\\s*:\\s*(.*)\\s*$", Pattern.CASE_INSENSITIVE);
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            while (br.ready())
            {
                Matcher matcher = fieldPattern.matcher(br.readLine());
                if (matcher.matches() && matcher.groupCount() == 2)
                {
                    result.put(matcher.group(1).trim(), matcher.group(2).trim());
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
    private MainFrame()
    {
        JgpgPreferences.get().addPropertyChangeListener(this);
        initComponents();

        initSecretListCellRenderer(jListSecrets);

        URL url = this.getClass().getResource("kgpg_identity.png");
        this.setIconImage(Toolkit.getDefaultToolkit().createImage(url));

        if (DEBUG)
        {
            CLEAR_SECONDS = 3;
            CLIP_SECONDS = 3;
            PASSWORD_SECONDS = 8;
        }

        lafs = UIManager.getInstalledLookAndFeels();
        try
        {
            for (currLafIX = 0; currLafIX < lafs.length; ++currLafIX)
            {
                UIManager.LookAndFeelInfo info = lafs[currLafIX];
                if (info.getName().toLowerCase().contains("gtk") || info.getName().toLowerCase().contains("windows"))
                {
                    if (info.getName().toLowerCase().contains("windows"))
                    {                        
                        setMinimumSize(new java.awt.Dimension(600, 400));
                        setPreferredSize(new java.awt.Dimension(800, 525));
                        jListSecrets.setFont(new java.awt.Font("Times New Roman", Font.PLAIN, 14)); // NOI18N
//                        jListFavoriteSecrets.setFont(new java.awt.Font("Times New Roman", Font.PLAIN, 14)); // NOI18N
                    }
                    
                    UIManager.setLookAndFeel(info.getClassName());
                    SwingUtilities.updateComponentTreeUI(this);
                    break;
                }
//                if (info.getName().toLowerCase().contains("gnome")) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    SwingUtilities.updateComponentTreeUI(this);
//                    break;
//                }
//                if (info.getName().toLowerCase().contains("nimbus")) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    SwingUtilities.updateComponentTreeUI(this);
//                    break;
//                }
            }
            if (currLafIX < lafs.length)
            {
                buttonLAF.setText(
                        "Change Look and Feel, current: " + lafs[currLafIX].getName());
            }

        }
        catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException ex)
        {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null,
                                                            ex);
        }

        buttonLAF.setVisible(false);
        buttonLAF.setMnemonic('L');
        
        progressClearTimer.setMaximum(CLEAR_SECONDS);
        progressPassTimer.setMaximum(PASSWORD_SECONDS);
        clearTimer = new Timer(1000, this);
        passTimer = new Timer(1000, this);
        stopClearTimer();


        passDlg = new PassphraseDialog(this, true);
        setPassStatusText();

        textFilter.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                super.keyReleased(e);
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    textFilter.setText("");
                }
                refreshSecretList();

//                gpgProcess.setFilter(textFilter.getText());
            }
        });

        initSecretListEventHandling(jListSecrets);

        gpgProcess = new JGPGProcess();
        loadPreferences();

        gpgProcess.addSecretListListener(this);
        gpgProcess.addResultListener(this);
        setUsePasswordDialog(gpgProcess.isPrefUsePasswordDialog());

        optionsPanel = new OptionsPanel(this);



        addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved (MouseEvent e)
            {
                super.mouseMoved(e);
                startTimer();
            }
        });

        setSize(880, 640);
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
                    lastActionList = jList;
                    decrypt();
                }
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    textFilter.setText("");
//                    gpgProcess.setFilter(textFilter.getText());
                    refreshSecretList();
                    textFilter.requestFocus();
                }
            }
        });
        jList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked (MouseEvent e)
            {
                lastActionList = jList;
                super.mouseClicked(e);
                if (clearTimer.isRunning())
                {
                    startTimer();
                }
                startPassTimer();

                if (e.getClickCount() == 2)
                {
                    decrypt();
                }
            }

            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
//                if (e.getButton() == 2)
//                {
//                    System.out.println("e = " + jList.getSelectedValue());
//                }
            }

            private void maybeShowPopup (MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    if (jList.getSelectedValue() != null && favorites.containsKey(jList.getSelectedValue()))
                    {
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem mi = new JMenuItem("Remove from favorites");
                        mi.addActionListener(actionEvent ->
                        {
                            favorites.remove(jList.getSelectedValue());
                            refreshFavorites();
                            jList.setSelectedIndex(-1);
                        });
                        popupMenu.add(mi);
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
                    decrypt(true, jList);
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
    }

    private void initSecretListCellRenderer (JList list)
    {
        list.setCellRenderer(new DefaultListCellRenderer(){

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
                        setText(gpgProcess.getShortFileName((String)value, info, true));
                    }
                    else
                    {
                        setText(gpgProcess.getShortFileName((String) value, false));
                    }
                }
                else if ( value instanceof JSeparator)
                {
                    return (Component)value;
                }
                if (filteredFavorites.contains(value) && prefUseFavoriteList)
                {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
                return this;
            }

        });
    }



//    private String favoritesParseFromJsonX (String json)
//    {
//        try
//        {
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Integer> result = objectMapper.readValue(json, LinkedHashMap.class);
//            result.entrySet().stream().forEach(stringIntegerEntry ->
//                    {
//                        String fname = stringIntegerEntry.getKey();
//                        if (new File(fname).isFile())
//                        {
//                            favorites.put(fname, stringIntegerEntry.getValue());
//                        }
//                    }
//            );
//            refreshFavorites();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        return json;
//    }

    private String favoritesParseFromJson (String json)
    {
        Pattern p = Pattern.compile("\"([^\"]+)\":([^,}]*)[,}]");
        Matcher m = p.matcher(json);
        while (m.find()) {
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
        JgpgPreferences.get().put(PREF_FAVORITES, favoritesAsJson());

        refreshFavorites();

        return json;
    }

    private String favoritesAsJson ()
    {
        StringBuilder jsonBuilder = new StringBuilder();
        Iterator<Map.Entry<String, Integer>> iter = favorites.entrySet().iterator();
        jsonBuilder.append("{");
        while (iter.hasNext()){
            Map.Entry<String, Integer> entry = iter.next();
            jsonBuilder.append('"');
            jsonBuilder.append(entry.getKey());
            jsonBuilder.append('"');
            jsonBuilder.append(':');
            jsonBuilder.append(entry.getValue());
            if (iter.hasNext()) {
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
                .forEach(stringIntegerEntry -> {
                    favorites.put(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());
                });

        JgpgPreferences.get().put(PREF_FAVORITES, favoritesAsJson());
    }


    private void handleForFavoritesList (String entry)
    {
        Integer i = favorites.computeIfAbsent(entry, s -> 0);

        favorites.put(entry, ++i);

        dbg(FAV, entry + " weight:" + i);

        sortFavorites();
        refreshFavorites();
    }

    private void refreshFavorites ()
    {
        favoritesList.clear();
        favoritesList.addAll(favorites.keySet()
                .stream()
                .filter(s -> {
                    return gpgProcess.getSecretdirs().stream().filter(s1 -> s.startsWith(s1)).findFirst().isPresent();
                })
                .collect(Collectors.toList())
        );
        refreshSecretList();
    }

    public void refreshSecretList ()
    {
        handleSecretList(this.allSecretFiles);
    }


    final String sepChar = Pattern.quote(File.separator);
    private Pattern pattern = Pattern.compile("^.*[^"+ sepChar+ "]+" + sepChar + "(.*$)");
    private boolean filterFile (String fileName)
    {
        if (textFilter.getText() == null || textFilter.getText().isEmpty())
        {
            return true;
        }
        Matcher m = pattern.matcher(fileName);
        if (m.matches())
        {
            String baseName = m.group(1);
            String filter = textFilter.getText();
            String name2 = baseName.toLowerCase().replace(".asc", "");
            name2 = name2.toLowerCase().replace(".gpg", "");
            boolean ret = name2.contains(filter.toLowerCase());
            dbg(FILTER, textFilter.getText() + (ret?" match   ":" nomatch ")  + fileName);
            return ret;
        }
        else {
            dbg(FILTER, "matcher matches() failed " + fileName);
        }
        return false;
    }

    private void dbg(DebugWindow.Category cat, String text)
    {
         DebugWindow.get().debug(cat, text);
    }

    private List<String> getConfiguredNumberFavorites ()
    {
        if (NUMBER_FAVORITES <= 0)
        {
            return favoritesList;
        }
        else {
            return favoritesList.subList(0, Math.min(favoritesList.size(), NUMBER_FAVORITES));
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
                .filter(s -> !prefFilterFavoriteList || filterFile(s))
                .collect(Collectors.toList()));

        secretListModel.clear();
        secretListModel.addAll(Arrays.asList(list).stream().filter(s -> filterFile(s)).collect(Collectors.toList()));

        String secretListInfo = "";
        if (secretListModel != null)
        {
            secretListInfo = " " + secretListModel.size() + " entries";
            jListSecrets.setModel(new javax.swing.AbstractListModel()
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

                    dbg(LIST,"model size secret list: " + size);
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
        }

        updateSecretListInfo(secretListInfo);
    }

    private void updateSecretListInfo (String text)
    {
        this.labelSecretInfo.setText(text);
        this.labelSecretInfo.setVisible(text != null && !text.isEmpty());
    }

    private void clearUserTextArea(String err)
    {
        jPanelTextArea.clear(err);
    }

    public void setUserTextareaText (String out, String err)
    {
        jPanelTextArea.setText(out, err);
        {
            startTimer();
        }
    }

    @Override
    public void setUserTextareaText (String out, String err, String filename)
    {
        setUserTextareaText(out, err);
        if (filename != null && !filename.isEmpty())
        {
            jListSecrets.setSelectedValue(filename, true);
        }
    }

    private void setUserTextareaStatus(String err)
    {
        jPanelTextArea.setText(null, err);
    }

    private void updateClearPassVisibility ()
    {
        buttonClearPass.setVisible(gpgProcess.isPrefUsePasswordDialog() || gpgProcess.isPrefConnectToGpgAgent());
        jSeparator1.setVisible(buttonClearPass.isVisible());
    }


    private void setUsePasswordDialog(boolean mode)
    {                      
        updateClearPassVisibility();
        progressPassTimer.setVisible(mode);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents()
    {

        javax.swing.JSplitPane jSplitPane1 = new javax.swing.JSplitPane();
        JPanel panelList = new JPanel();
        textFilter = new javax.swing.JTextField();
        javax.swing.JScrollPane scrollPaneSecrets = new javax.swing.JScrollPane();

        // gpg files and favorite gpg files

        labelSecretInfo = new JLabel();
        jListSecrets = new JList();
        JPanel jToolBarPanel = new JPanel(new BorderLayout());
        javax.swing.JToolBar jToolBar1 = new javax.swing.JToolBar();
        buttonClearPass = new javax.swing.JButton();
        buttonClearFavorites = new javax.swing.JButton();
        buttonClearPass.setMnemonic(KeyEvent.VK_P);
        JButton buttonClearTextarea = new JButton();
        buttonClearTextarea.setMnemonic(KeyEvent.VK_L);
        javax.swing.JToolBar.Separator jSeparator01 = new javax.swing.JToolBar.Separator();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        JButton buttonExit = new JButton();
        JToggleButton buttonOptions = new JToggleButton();
        JButton buttonAbout = new JButton();
        buttonExit.setMnemonic(KeyEvent.VK_X);
        buttonOptions.setMnemonic(KeyEvent.VK_O);
        javax.swing.JToolBar.Separator jSeparator2 = new javax.swing.JToolBar.Separator();
        JButton jButtonSettings = new JButton();
        jButtonSettings.setMnemonic(KeyEvent.VK_S);
        JButton jButtonClipboard = new JButton();
        jButtonClipboard.setMnemonic(KeyEvent.VK_C);
        javax.swing.JToolBar.Separator jSeparator3 = new javax.swing.JToolBar.Separator();
        buttonLAF = new javax.swing.JButton();
        JPanel statusBarPanel = new JPanel();
        progressClearTimer = new javax.swing.JProgressBar();
        progressPassTimer = new javax.swing.JProgressBar();


        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JGPG");
        setMinimumSize(new java.awt.Dimension(800, 600));

        jSplitPane1.setBackground(new java.awt.Color(204, 204, 204));
        jSplitPane1.setMinimumSize(new java.awt.Dimension(200, 102));
        jSplitPane1.setPreferredSize(new java.awt.Dimension(400, 400));

        panelList.setMinimumSize(new java.awt.Dimension(200, 20));
        panelList.setPreferredSize(new java.awt.Dimension(250, 246));
        panelList.setLayout(new java.awt.BorderLayout());


        JPanel textFilterPanel = new JPanel(new BorderLayout());


        textFilter.setMinimumSize(new java.awt.Dimension(150, 30));
        textFilter.setPreferredSize(new java.awt.Dimension(150, 30));

        textFilterPanel.add(textFilter, BorderLayout.CENTER);

        JButton cleanButton = new JButton();
        cleanButton.setMinimumSize(new java.awt.Dimension(30, 30));
        cleanButton.setPreferredSize(new java.awt.Dimension(30, 30));
        ImageIcon cleanButtonIcon = new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/clean.png"));
        cleanButton.setIcon(cleanButtonIcon); // NOI18N
        textFilterPanel.add(cleanButton, BorderLayout.EAST);

        cleanButton.addActionListener(this::cleanButtonActionPerformed);

        panelList.add(textFilterPanel, java.awt.BorderLayout.NORTH);

        jListSecrets.setFont(new java.awt.Font("DejaVu Sans Mono", Font.PLAIN, 14)); // NOI18N
        jListSecrets.setToolTipText("Press CTRL+C to decode first line to clipboard");


        scrollPaneSecrets.setViewportView(jListSecrets);
        panelList.add(scrollPaneSecrets, java.awt.BorderLayout.CENTER);
        panelList.add(labelSecretInfo, BorderLayout.SOUTH);
        labelSecretInfo.setVisible(false);

        jSplitPane1.setLeftComponent(panelList);

        jPanelTextArea = new JPanelTextArea(this);

        jSplitPane1.setRightComponent(jPanelTextArea);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jToolBar1.setRollover(true);



        jButtonClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/icons8-clipboard-30.png"))); // NOI18N
        jButtonClipboard.setText("Clipboard First Line");
        jButtonClipboard.setToolTipText("Save the first line of decoded file to clipboard");
        jButtonClipboard.setFocusable(false);
        jButtonClipboard.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonClipboard.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonClipboard.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonClipboardActionPerformed(evt);
            }
        });
//        jToolBar1.add(jButtonClipboard);

        jSeparator01.setRequestFocusEnabled(false);
        jSeparator01.setSeparatorSize(new java.awt.Dimension(30, 4));


        // ---------------------------------
        buttonClearTextarea.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/1346509543_edit-clear.png"))); // NOI18N
        buttonClearTextarea.setText("Clear Textarea");
        buttonClearTextarea.setToolTipText("Clears the textarea and the clipboard in case a password has been stored there");
        buttonClearTextarea.setBorderPainted(false);
        buttonClearTextarea.setFocusable(false);
        buttonClearTextarea.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonClearTextarea.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonClearTextarea.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonClearTextareaActionPerformed(evt);
            }
        });
        jToolBar1.add(buttonClearTextarea);
        jToolBar1.add(jSeparator01);


        // ---------------------------------
        buttonClearPass.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/1346509520_preferences-desktop-cryptography.png"))); // NOI18N
        buttonClearPass.setText("Clear Passphrase");
        buttonClearPass.setToolTipText("Clears the internally stored passphrase and optionally the GPG agenty password (see Settings)");
        buttonClearPass.setBorderPainted(false);
        buttonClearPass.setFocusable(false);
        buttonClearPass.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonClearPass.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonClearPass.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonClearPassActionPerformed(evt);
            }
        });
        jToolBar1.add(buttonClearPass);
        // ---------------------------------
        buttonClearFavorites.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/favs32.png"))); // NOI18N
        buttonClearFavorites.setText("Clear Favorites");
        buttonClearFavorites.setToolTipText("Clears the favorites list");
        buttonClearFavorites.setBorderPainted(false);
        buttonClearFavorites.setFocusable(false);
        buttonClearFavorites.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonClearFavorites.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonClearFavorites.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed (java.awt.event.ActionEvent evt)
            {
                if (OK_OPTION == JOptionPane.showConfirmDialog(MainFrame.this, "Really delete all favorites?", "JGPG Delete Favorites", OK_CANCEL_OPTION))
                {
                    favorites.clear();
                    JgpgPreferences.get().put(PREF_FAVORITES, favoritesAsJson());
                    refreshFavorites();
                }
            }
        });
        jToolBar1.add(buttonClearFavorites);

        // ---------------------------------


        jSeparator1.setRequestFocusEnabled(false);
        jSeparator1.setSeparatorSize(new java.awt.Dimension(30, 4));
        jToolBar1.add(jSeparator1);

        buttonExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/1346509462_exit.png"))); // NOI18N
        buttonExit.setText("EXIT");
        buttonExit.setBorderPainted(false);
        buttonExit.setFocusable(false);
        buttonExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonExit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonExitActionPerformed(evt);
            }
        });

        buttonOptions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/toolbar.png"))); // NOI18N
        buttonOptions.setText("Text Options");
        buttonOptions.setBorderPainted(false);
        buttonOptions.setFocusable(false);
        buttonOptions.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonOptions.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonOptions.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonOptionsActionPerformed(evt);
            }
        });
        jPanelTextArea.setButtonToolbarVisible(buttonOptions.isSelected());

        jToolBar1.add(jButtonSettings);

        jSeparator2.setRequestFocusEnabled(false);
        jSeparator2.setSeparatorSize(new java.awt.Dimension(30, 4));
        jToolBar1.add(jSeparator2);
        jToolBar1.add(buttonOptions);
        jToolBar1.add(jSeparator2);
        jToolBar1.add(buttonExit);

        jButtonSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/Settings-icon.png"))); // NOI18N
        jButtonSettings.setText("JGPG Settings");
        jButtonSettings.setFocusable(false);
        jButtonSettings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSettings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSettings.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSettingsActionPerformed(evt);
            }
        });




        jSeparator3.setRequestFocusEnabled(false);
        jSeparator3.setSeparatorSize(new java.awt.Dimension(30, 4));
        jToolBar1.add(jSeparator3);

        buttonLAF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/1346515642_gnome-settings-theme.png"))); // NOI18N
        buttonLAF.setText("Change theme");
        buttonLAF.setFocusable(false);
        buttonLAF.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        buttonLAF.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonLAFActionPerformed(evt);
            }
        });
        jToolBar1.add(buttonLAF);


        buttonAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/icons8-info-30.png"))); // NOI18N
        buttonAbout.setText("");
        buttonAbout.setBorderPainted(false);
        buttonAbout.setFocusable(false);
        buttonAbout.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonAbout.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonAbout.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonAboutActionPerformed(evt);
            }
        });
        jToolBarPanel.add(buttonAbout, BorderLayout.EAST);

        jToolBarPanel.add(jToolBar1, BorderLayout.CENTER);
        getContentPane().add(jToolBarPanel, java.awt.BorderLayout.PAGE_START);

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
        focusComponentVector.add(textFilter);
        focusComponentVector.add(jListSecrets);
        focusComponentVector.add(jPanelTextArea.getFocusComponent());
        JgpgFocusTraversalPolicy jgpgFocusTraversalPolicy = new JgpgFocusTraversalPolicy(focusComponentVector);
        this.setFocusTraversalPolicy(jgpgFocusTraversalPolicy);

        pack();
    }

//    JTextArea debugTextArea = new JTextArea();
//    private void initDebugWindow ()
//    {
//        JFrame debugFrame = new JFrame();
//        debugFrame.setLayout(new BorderLayout());
//        debugTextArea.setFont(new Font("monospaced", Font.PLAIN, 12));
//        JScrollPane debugScrollPane = new JScrollPane(debugTextArea);
//        debugScrollPane.setPreferredSize(new Dimension(600, 600));
//        debugFrame.getContentPane().add(debugScrollPane, BorderLayout.CENTER);
//        JToolBar debugToolbar;
//        debugFrame.getContentPane().add(debugToolbar = new JToolBar(), BorderLayout.NORTH);
//        JButton debugClear = new JButton("Clear");
//        debugClear.addActionListener(e -> debugTextArea.setText(""));
//        debugToolbar.add(debugClear);
//        debugFrame.pack();
//        debugFrame.setVisible(true);
//    }

    private void buttonClearTextareaActionPerformed(java.awt.event.ActionEvent evt) {
        jPanelTextArea.clear("Cleared");
        stopClearTimer();
    }

    private void buttonExitActionPerformed(java.awt.event.ActionEvent evt) {
        JGPGProcess.clearClipboardIfNotChanged();
        this.clipboard = false;
        System.exit(1);
    }

    private void buttonOptionsActionPerformed (java.awt.event.ActionEvent evt)
    {
        if (evt.getSource() instanceof JToggleButton)
        {
            jPanelTextArea.setButtonToolbarVisible(((JToggleButton)evt.getSource()).isSelected());
        }
    }

    private void buttonClearPassActionPerformed(java.awt.event.ActionEvent evt) {
        passDlg.setPassPhrase("");
        jPanelTextArea.clear("Cleared");
        JGPGProcess.clearClipboardIfNotChanged();
        this.clipboard = false;
        stopClearTimer();
        stopPassTimer();
        if (gpgProcess != null)
        {
            gpgProcess.gpgAgendCommand("--reload");
        }
    }

    private void buttonAboutActionPerformed (java.awt.event.ActionEvent evt)
    {

        URL is = getClass().getResource("/org/mockenhaupt/jgpg/about.txt");

        StringBuilder sb = new StringBuilder();
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader((is.openStream())));
            while (br.ready())
            {
                sb.append(br.readLine());
                sb.append(JGPGProcess.LINE_SEP);
            }
        }
        catch (IOException ex)
        {
            sb.append("Internal Error: ").append(ex.getMessage());
        }


        JOptionPane.showMessageDialog(this,
                sb.toString(),
                "About JGPG " + getVersionFromManifest().computeIfAbsent(VERSION_PROJECT, k -> "UNKNOWN"),
                JOptionPane.INFORMATION_MESSAGE,
                new javax.swing.ImageIcon(getClass().getResource("/org/mockenhaupt/jgpg/icons8-info-30.png")));
    }



    private void buttonLAFActionPerformed(java.awt.event.ActionEvent evt) {
        try
        {
            currLafIX++;
            if (currLafIX >= lafs.length)
            {
                currLafIX = 0;
            }
            UIManager.setLookAndFeel(lafs[currLafIX].getClassName());
            SwingUtilities.updateComponentTreeUI(this);
            buttonLAF.setText(
                    "Change Look and Feel, current: " + lafs[currLafIX].getName());

        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex)
        {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null,
                                                            ex);
        }
    }

    private void jButtonSettingsActionPerformed(java.awt.event.ActionEvent evt)
    {

        optionsPanel.setVisible(true);
    }


    private void cleanButtonActionPerformed (ActionEvent evt)
    {
        textFilter.setText("");
        refreshSecretList();
        textFilter.requestFocus();
    }

    private void jButtonClipboardActionPerformed(java.awt.event.ActionEvent evt)
    {

        decrypt(true);
    }



    // Variables declaration - do not modify
    private javax.swing.JButton buttonClearPass;
    private javax.swing.JButton buttonClearFavorites;
    private javax.swing.JButton buttonLAF;
    private JList jListSecrets;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JProgressBar progressClearTimer;
    private javax.swing.JProgressBar progressPassTimer;
    private javax.swing.JTextField textFilter;
    private JPanelTextArea jPanelTextArea;
    private JLabel labelSecretInfo;

    private boolean prefUseFavoriteList = true;
    private boolean prefFilterFavoriteList = true;

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
            if (passSeconds >= PASSWORD_SECONDS)
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
                setCLIP_SECONDS((Integer) propertyChangeEvent.getNewValue());
                break;
            case PREF_USE_FAVORITES:
                setPrefUseFavorites((Boolean)propertyChangeEvent.getNewValue());
                break;
            case PREF_FILTER_FAVORITES:
                prefFilterFavoriteList = (boolean) propertyChangeEvent.getNewValue();
                refreshFavorites();
                break;
            case PREF_CLEAR_SECONDS:
                setCLEAR_SECONDS((Integer) propertyChangeEvent.getNewValue());
                break;
            case PREF_PASSWORD_SECONDS:
                setPASSWORD_SECONDS((Integer) propertyChangeEvent.getNewValue());
                break;
            case PREF_NUMBER_FAVORITES:
                NUMBER_FAVORITES = (Integer) propertyChangeEvent.getNewValue();
                SwingUtilities.invokeLater(() -> refreshFavorites());
                break;
            case PREF_SECRETDIRS:
                SwingUtilities.invokeLater(() -> refreshFavorites());
                break;
            case PREF_FAVORITES_SHOW_COUNT:
                prefShowFavoritesCount = (Boolean) propertyChangeEvent.getNewValue();
                refreshFavorites();
                break;
        }
        SwingUtilities.invokeLater(() -> updateClearPassVisibility());
    }
}
