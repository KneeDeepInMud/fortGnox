package org.mockenhaupt.fortgnox.swing;

import org.mockenhaupt.fortgnox.FgPreferences;
import org.mockenhaupt.fortgnox.MainFrame;
import org.mockenhaupt.fortgnox.misc.FileUtils;
import org.mockenhaupt.fortgnox.misc.StringUtils;
import org.mockenhaupt.fortgnox.otp.OTP;
import org.mockenhaupt.fortgnox.otp.OtpQRUtil;
import org.mockenhaupt.fortgnox.tags.TagsStore;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.awt.FlowLayout.CENTER;
import static java.awt.FlowLayout.LEFT;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_RESET_MASK_BUTTON_SECONDS;

public class FgPanelTextArea extends JPanel implements PropertyChangeListener, FgTextFilter.TextFilterHandler
{
    private JTextPane textPane;

    private String plainText = "";

    private JToggleButton maskToggleButton;
    private JToggleButton detectUrlsToggleButton;
    private JToggleButton maskFirstLineToggleButton;
    private JToggleButton compressBlankLinesButton;
    private JToggleButton openUrlButton;
    private JCheckBox checkSelectAll;
    private JScrollPane scrollPaneTextArea;
    private JToolBar clipToolbar;
    private JScrollPane scrollPaneTextAreaError;
    private JTextArea textAreaError;
    private FgTextFilter fgTextFilter;
    private MainFrame mainFrame;
    private JPanel buttonToolbar;
    private Timer resetMaskButtonTimer;
    private JLabel labelFileName = new JLabel();
    private JPanel panelHeaderLine;
    private CurrentTimeLabel remainingOtpTimeLabel = new CurrentTimeLabel(CurrentTimeLabel.Mode.COUNTDOWN, 30, "OTP: ");
    private final AtomicReference<Integer> remainingOtpTime = new AtomicReference<>();


    private static final List<String> DEFAULT_MASK_PATTERNS = new ArrayList(Arrays.asList(
            ".*pass", "puk", "tan", "secret", "kennwort", "root", ".*code", ".*pin"
    ));

    private static final List<String> DEFAULT_USERNAME_PATTERNS = new ArrayList(Arrays.asList(
            "account", "user", ".*id[:=]", "login", "Auftrag", "benutzer", "Kunde", "telefon", ".*nummer", ".*kennung"
    ));

    private static final String TOC_HEADER_PREFIX_FILE = "file://";
    private static final String TOC_HEADER_SUFFIX =  "tocheader_";
    private static final String TOC_HEADER_PREFIX = TOC_HEADER_PREFIX_FILE + TOC_HEADER_SUFFIX;
    private static final String TOC_START = "";
    private static final String TOC_END = "-- ";


    enum LineMaskingOrder
    {
        OTP_CODES,
        PASSWORDS_FILES,
        PASSWORDS_TEXT,
        USER_NAMES,
        HTTP_LINKS,
        EMAIL_ADRESSES,
        PASSWORD_FIRST_LINE
    }

    private List<String> maskPasswordPatterns = new ArrayList<>();
    private List<String> maskUsernamePatterns = new ArrayList<>();
    private boolean prefCompressBlankLines = true;
    private String prefOpenUrlCommand = "";
    private boolean prefOpenUrls = true;
    private boolean prefClipboardToolbarVisible = true;
    private boolean prefMaskFirstLine = true;
    private int prefTextAreaFontSize = 14;
    private boolean prefTocGeneration = false;
    private String prefTocPrefix = "";
    private final AtomicReference<String> oldStatusText = new AtomicReference<>("");
    // stores the text position of search hits
    final private List<Integer> hitList = new ArrayList<>();
    SortedMap<String, String> tocMap = new TreeMap<>();
    private int caretPointer = -1;

    public static Color BACKGROUND = new java.awt.Color(62, 62, 62);
    public static final String PATTERN_DELIMITER = "|";
    public static final String PASSWORD_PREFIX = "oghogoo3eaTheephe7:";
    public static final String GPG_FILE_PASSWORD_PREFIX = "oghogoo3eaTheephe8:";
    public static final String COLOR_LINK = "#9fbfff";
    private static final String COLOR_END_OF_TOC = "#999999";

    public static final String COLOR_JUMP_TOP = "#8DDE7EFF";
    public static final String COLOR_GPG_FILE = COLOR_JUMP_TOP;
    public static final String COLOR_CLIPBOARD = "#fff8ac";
    public static final String COLOR_EMAIL = COLOR_CLIPBOARD;
    public static final String COLOR_PASSWORD = "#ed8cc5";
    private static final String SPAN_COL = "<span style='color:" + COLOR_PASSWORD + ";'>";
    private static final String  PASSWORD_MASK = "xxxxxxxxxxxxxxx-";

    private static final String TXT_MASK_FIRST = "Mask 1st";
    private static final String TXT_MASK_FIRST_TT = "First line in password file will be shown as 'xxxxxx-1' instead of clear text";
    private static final String TXT_MASK_ALL_PASSWORDS =  "Mask passwords";
    private static final String TXT_MASK_ALL_PASSWORDS_TT =  "Show 'xxxxxx-1' instead of clear passwords";
    private static final String TXT_COMPRESS_BLANK_LINES =  "Squeeze text";
    private static final String TXT_COMPRESS_BLANK_LINES_TT =  "Multiple empty lines in password file will be shown as singe line";
    private static final String TXT_OPEN_URL =  "Open URLs";
    private static final String TXT_DETECT_URLS =  "URLs";
    private static final String TXT_DETECT_URLS_TT =  "Display URLs and email addresses as selectable hyperlinks";

    private Timer otpTimer;


    class SearchKeyAdapter extends KeyAdapter
    {
        MainFrame mainFrame;

        public SearchKeyAdapter(MainFrame mainFrame)
        {
            this.mainFrame = mainFrame;
        }

        @Override
        public void keyPressed(KeyEvent e)
        {
            mainFrame.startTimer();
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_F3:
                    if (e.isShiftDown())
                    {
                        searchPrevious();
                    }
                    else
                    {
                        searchNext();
                    }
                    break;

                case KeyEvent.VK_ESCAPE:
                    resetSearch();
                    break;

                case KeyEvent.VK_DOWN:
                    scrollPaneTextArea.getVerticalScrollBar()
                            .setValue(scrollPaneTextArea.getVerticalScrollBar().getValue()
                            + scrollPaneTextArea.getVerticalScrollBar().getUnitIncrement());
                    break;

                case KeyEvent.VK_UP:
                    scrollPaneTextArea.getVerticalScrollBar()
                            .setValue(scrollPaneTextArea.getVerticalScrollBar().getValue()
                            - scrollPaneTextArea.getVerticalScrollBar().getUnitIncrement());
                    break;

                default:
                    super.keyPressed(e);
                    break;
            }
        }
    }

    private List<String> getMaskPasswordPatterns ()
    {
        return maskPasswordPatterns;
    }
    private void setMaskPasswordPatterns (String maskPasswordPatterns)
    {
        this.maskPasswordPatterns.clear();
        this.maskPasswordPatterns.addAll( Arrays.asList(maskPasswordPatterns.split("\\" + PATTERN_DELIMITER))
                .stream()
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList()));
        SwingUtilities.invokeLater(() -> updateText());
    }
    public static String getDefaultMaskPatterns ()
    {
        return DEFAULT_MASK_PATTERNS.stream().collect(Collectors.joining(PATTERN_DELIMITER));
    }

    private List<String> getMaskUsernamePatterns ()
    {
        return maskUsernamePatterns;
    }

    public String getDefaultMaskUsernamePatterns ()
    {
        return DEFAULT_USERNAME_PATTERNS.stream().collect(Collectors.joining(PATTERN_DELIMITER));
    }

    public void setMaskUsernamePatterns (String maskUsernamePatterns)
    {
        this.maskUsernamePatterns.clear();
        this.maskUsernamePatterns.addAll( Arrays.asList(maskUsernamePatterns.split("\\" + PATTERN_DELIMITER))
                .stream()
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList()));
        SwingUtilities.invokeLater(() -> updateText());
    }


    public static void scrollToLineOfCaretPosition(JTextComponent component, int pos) throws BadLocationException
    {
        component.setCaretPosition(pos);
        Container container = SwingUtilities.getAncestorOfClass(JViewport.class, component);

        if (container == null) return;

        try
        {
            Rectangle r = component.modelToView(component.getCaretPosition());
            JViewport viewport = (JViewport)container;
            //            int extentHeight = viewport.getExtentSize().height;
            //            int viewHeight = viewport.getViewSize().height;
            //            int y = Math.max(0, r.y - ((extentHeight - r.height) / 2));
            //            y = Math.min(y, viewHeight - extentHeight);

            viewport.setViewPosition(new Point(0, r.y));
        }
        catch(BadLocationException ble)
        {
            throw ble;
        }
    }

    private void initTextArea (MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
        this.textPane = new JTextPane();

//        StyleSheet styleSheet = new StyleSheet();
////        styleSheet.addRule("ol {padding: 0px;}");
//        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
//        htmlEditorKit.setStyleSheet(styleSheet);
//
//        HTMLDocument htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
//        textPane.setDocument(htmlDocument);


        textPane.setBorder(BorderFactory.createLineBorder(BACKGROUND, 1));
        textPane.setEditable(false);
        textPane.setContentType("text/html");


        textPane.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mousePressed (MouseEvent e)
            {
                super.mousePressed(e);
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased (MouseEvent e)
            {
                super.mouseReleased(e);
                maybeShowPopup(e);
            }

            private void maybeShowPopup (MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    JPopupMenu popupMenu = mainFrame.getSecretsPopupMenu(true);
                    if (popupMenu.getComponentCount() > 0)
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        textPane.addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved (MouseEvent e)
            {
                super.mouseMoved(e);
                mainFrame.startTimer();
            }
        });

        textPane.addKeyListener(new SearchKeyAdapter(mainFrame)
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                super.keyTyped(e);
                if (((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
                        || ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0))
                {
                    return;
                }
                fgTextFilter.setText(fgTextFilter.getText() + e.getKeyChar());
                fgTextFilter.requestFocus();
                mainFrame.startTimer();
            }
        });

        setupHyperLinkEvents(mainFrame);
    }




    private void setupHyperLinkEvents(MainFrame mainFrame)
    {
        textPane.addHyperlinkListener(e ->
        {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType()))
            {
                try
                {
                    if (e.getURL() != null)
                    {
                        String needle = e.getURL().getHost();

                        if (needle.startsWith(TOC_HEADER_SUFFIX))
                        {
                            int offset = getDocumentText().indexOf(TOC_END);
                            needle = needle.replaceAll(TOC_HEADER_SUFFIX, "");

                            int pos = Integer.parseInt(needle);
                            scrollToLineOfCaretPosition(textPane, pos == 0 ? 0 : pos + offset);
                        }
                        else if (isOpenUrls())
                        {
                            openUrlLink(e);
                        }
                        else
                        {
                            copyToClipboard(e);
                        }
                    }
                    else
                    {
                        if (e.getDescription() != null)
                        {
                            if (e.getDescription().startsWith(PASSWORD_PREFIX))
                            {
                                String pass = e.getDescription().replaceFirst(PASSWORD_PREFIX, "");
                                // password text comes encoded, decode before adding to clipboard
                                pass = URLDecoder.decode(pass);
                                MainFrame.toClipboard(pass, "selected password", true);
                            }
                            else if (e.getDescription().startsWith(GPG_FILE_PASSWORD_PREFIX))
                            {
                                String gpgFile = e.getDescription().replaceFirst(GPG_FILE_PASSWORD_PREFIX, "");
                                mainFrame.decrypt(new File(gpgFile));
                            }
                            else
                            {
                                copyToClipboard(e);
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    setStatusText(ex.getMessage());
                }
            }
            else if (HyperlinkEvent.EventType.ENTERED.equals(e.getEventType()))
            {
                String desc = e.getDescription();
                if (desc != null)
                {
                    if (oldStatusText.get().isEmpty())
                    {
                        oldStatusText.set(getStatusText());
                    }

                    if (e.getURL() != null)
                    {
                        String needle = e.getURL().getHost();
                        if (needle.startsWith(TOC_HEADER_SUFFIX))
                        {
                            if (needle.endsWith("_0")) setStatusText("Jump to top");
                            else  setStatusText("Jump to section '" + tocMap.get(e.getURL().toString()) + "'");
                        }
                        else if (isOpenUrls()) setStatusText("Open \"" + desc + "\" in browser");
                        else setStatusText("Copy \"" + desc + "\" to clipboard");
                    }
                    else if (desc.startsWith(PASSWORD_PREFIX))
                    {
                        setStatusText("Copy password to clipboard");
                    }
                    else if (desc.startsWith(GPG_FILE_PASSWORD_PREFIX))
                    {
                        String gpgFile = e.getDescription().replaceFirst(GPG_FILE_PASSWORD_PREFIX, "");

                        setStatusText("Decrypt file: " + gpgFile);
                    }
                    else
                    {
                        setStatusText("Copy \"" + desc + "\" to clipboard");
                    }
                }
            }
            else if (HyperlinkEvent.EventType.EXITED.equals(e.getEventType()))
            {
                String oldStat = oldStatusText.getAndSet("");
                if (!oldStat.isEmpty())
                {
                    setStatusText(oldStat);
                }
            }
        });
    }

    private void copyToClipboard (HyperlinkEvent e)
    {
        String text = e.getDescription().trim();
        MainFrame.toClipboard(text, "\"" + text + "\"", false);
    }

    private void openUrlLink (HyperlinkEvent e) throws URISyntaxException, IOException
    {
        Desktop desktop = Desktop.getDesktop();
        setStatusText("Opening " + e.getURL().toURI() + " in browser");
        if (prefOpenUrlCommand == null || prefOpenUrlCommand.isEmpty())
        {
            desktop.browse(e.getURL().toURI());
        }
        else
        {
            Runtime.getRuntime().exec(prefOpenUrlCommand + " " + e.getURL().toString());
        }
    }

    public FgPanelTextArea (MainFrame mainFrame)
    {
        super(new BorderLayout());
        initTextArea(mainFrame);
        init();
    }

    private void setLabelFileName (String fileName)
    {
        if (fileName == null || fileName.isEmpty())
        {
            panelHeaderLine.setVisible(false);
        }
        else
        {
            labelFileName.setText(fileName);
            panelHeaderLine.setVisible(true);
        }

    }
    private void init ()
    {
        FgPreferences.get().addPropertyChangeListener(this);
        loadPreferences();

        JPanel textAndHeaderPanel = new JPanel(new BorderLayout());

        panelHeaderLine = new JPanel(new BorderLayout());
        panelHeaderLine.add(labelFileName, BorderLayout.CENTER);
        panelHeaderLine.add(remainingOtpTimeLabel, BorderLayout.EAST);

        textAndHeaderPanel.add(panelHeaderLine, BorderLayout.NORTH);

        scrollPaneTextArea = new JScrollPane();
        scrollPaneTextArea.setViewportView(textPane);
        scrollPaneTextArea.getVerticalScrollBar().setUnitIncrement(14);

        textAndHeaderPanel.add(scrollPaneTextArea, BorderLayout.CENTER);

        JPanel searchAndTextPanel = new JPanel(new BorderLayout());
        searchAndTextPanel.add(textAndHeaderPanel, BorderLayout.CENTER);
        fgTextFilter = new FgTextFilter(this);
        searchAndTextPanel.add(fgTextFilter, BorderLayout.NORTH);
        fgTextFilter.addKeyListener(new SearchKeyAdapter(mainFrame) {});

        add(searchAndTextPanel, BorderLayout.CENTER);

        buttonToolbar = new JPanel(new FlowLayout(LEFT, 1, 0));
//        buttonToolbar.setRollover(true);

        // ---------------------------------------------------------------------
        maskToggleButton = new FgToggleButton(TXT_MASK_ALL_PASSWORDS);
        maskToggleButton.setToolTipText(TXT_MASK_ALL_PASSWORDS_TT);
        maskToggleButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent actionEvent)
            {
                if (!isMask()) {
                    restoreMaskButtonStates();
                }
                updateCheckboxSelectAll();
                updateText();
            }
        });
        maskToggleButton.setSelected(true);


        detectUrlsToggleButton = new FgToggleButton(TXT_DETECT_URLS);
        detectUrlsToggleButton.setToolTipText(TXT_DETECT_URLS_TT);
        detectUrlsToggleButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent actionEvent)
            {
                updateCheckboxSelectAll();
                updateText();
            }
        });
        detectUrlsToggleButton.setSelected(true);


        checkSelectAll = new JCheckBox();
        checkSelectAll.setPreferredSize(new Dimension(25, 25));
        checkSelectAll.setToolTipText("Toggle all toolbar buttons");
        checkSelectAll.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent actionEvent)
            {
                boolean selected = ((JCheckBox)actionEvent.getSource()).isSelected();
                setMask(selected);
                setCompressBlankLines(selected);
                setMaskFirstLine(selected);
                setDetectUrls(selected);
                openUrlButton.setVisible(isDetectUrls());
                updateText();
            }
        });
        checkSelectAll.setSelected(true);


        // ---------------------------------------------------------------------
        compressBlankLinesButton = new FgToggleButton(TXT_COMPRESS_BLANK_LINES);
        compressBlankLinesButton.setToolTipText(TXT_COMPRESS_BLANK_LINES_TT);
        compressBlankLinesButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent actionEvent)
            {
                setCompressBlankLines(compressBlankLinesButton.isSelected(), true);
            }
        });
        compressBlankLinesButton.setSelected(prefCompressBlankLines);

        // ---------------------------------------------------------------------
        openUrlButton = new FgToggleButton();
        openUrlButton.setPreferredSize(new Dimension(30, 30));
        openUrlButton.setBorderPainted(false);
        openUrlButton.setIcon(FileUtils.getScaledIcon(this.getClass(), "/org/mockenhaupt/fortgnox/icons8-link-64.png", 24));
        openUrlButton.setToolTipText("Directly open URLs in browser");
        openUrlButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent actionEvent)
            {
               setOpenUrls(openUrlButton.isSelected(), true);
            }
        });
        openUrlButton.setSelected(prefOpenUrls);

        // ---------------------------------------------------------------------
        maskFirstLineToggleButton = new FgToggleButton(TXT_MASK_FIRST);
        maskFirstLineToggleButton.setToolTipText(TXT_MASK_FIRST_TT);
        maskFirstLineToggleButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent actionEvent)
            {
                if (!isMaskFirstLine()) {
                    restoreMaskButtonStates();
                }
                updateCheckboxSelectAll();
                updateText();
            }
        });
        maskFirstLineToggleButton.setSelected(prefMaskFirstLine);


        // ---------------------------------------------------------------------
        clipToolbar = new JToolBar(JToolBar.VERTICAL);
        clipToolbar.setFloatable(false);
        clipToolbar.setVisible(false);

        // ---------------------------------------------------------------------


        textAreaError = new JTextArea();
        scrollPaneTextAreaError = new JScrollPane(textAreaError);
        int h = textAreaError.getFontMetrics(textAreaError.getFont()).getHeight()
                + textAreaError.getFontMetrics(textAreaError.getFont()).getDescent();

        scrollPaneTextAreaError.setMaximumSize(new Dimension(Integer.MAX_VALUE, h * 4));
        scrollPaneTextAreaError.setPreferredSize(scrollPaneTextAreaError.getMaximumSize());
//        scrollPaneTextAreaError.setVisible(false);
        textAreaError.setBackground(BACKGROUND);
        textAreaError.setForeground(new Color(232, 228, 160));

        // ---------------------------------------------------------------------
        buttonToolbar.add(maskFirstLineToggleButton);
        buttonToolbar.add(maskToggleButton);
        buttonToolbar.add(compressBlankLinesButton);
        buttonToolbar.add(detectUrlsToggleButton);
//        buttonToolbar.add(openUrlButton);
        buttonToolbar.add(checkSelectAll);

        Set<Component> jToggleButtonSet = new HashSet<>();
        int maxWidth = Integer.MIN_VALUE;
        for (int i = 0; i < buttonToolbar.getComponentCount(); ++i)
        {
            if (buttonToolbar.getComponent(i) instanceof JToggleButton && !(buttonToolbar.getComponent(i) instanceof JCheckBox))
            {
                JToggleButton b = (JToggleButton) buttonToolbar.getComponent(i);
                jToggleButtonSet.add(b);
                int w = b.getFontMetrics(b.getFont()).stringWidth(b.getText());

                maxWidth = Math.max(w + 5, maxWidth);
            }
        }

        JButton buttonClearTextarea = new JButton();
        buttonClearTextarea.setIcon(FileUtils.getScaledIcon(this.getClass(), "/org/mockenhaupt/fortgnox/wipe48.png", 24));
        buttonClearTextarea.setMnemonic(KeyEvent.VK_I);
        buttonClearTextarea.setPreferredSize(new Dimension(30, 30));
        buttonClearTextarea.setToolTipText("Clears the textarea and the clipboard in case a password has been stored there");
        buttonClearTextarea.setBorderPainted(false);
        buttonClearTextarea.setFocusable(false);
        buttonClearTextarea.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonClearTextarea.setVerticalTextPosition(SwingConstants.BOTTOM);
        buttonClearTextarea.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed (java.awt.event.ActionEvent evt)
            {
                mainFrame.clearTextArea(true);
            }
        });


        this.add(scrollPaneTextAreaError, BorderLayout.SOUTH);
        JPanel toolBarPanel = new JPanel(new BorderLayout());
        JToggleButton buttonTbVisible = new JToggleButton();
        buttonTbVisible.setIcon(new ImageIcon(getClass().getResource("/org/mockenhaupt/fortgnox/settings24.png")));
        buttonTbVisible.setToolTipText("Show extended options for text area");
        buttonTbVisible.addActionListener(a -> buttonToolbar.setVisible(buttonTbVisible.isSelected()));
        buttonTbVisible.setPreferredSize(new Dimension(30, 30));

        JPanel miniButtonPanel = new JPanel(new FlowLayout(CENTER, 0, 0));
        miniButtonPanel.setBorder(BorderFactory.createEmptyBorder());
        miniButtonPanel.add(buttonClearTextarea);
        miniButtonPanel.add(openUrlButton);
        miniButtonPanel.add(buttonTbVisible);
        toolBarPanel.add(miniButtonPanel, BorderLayout.WEST);
        toolBarPanel.add(buttonToolbar, BorderLayout.CENTER);
//        toolBarPanel.add(new CurrentTimeLabel(), BorderLayout.LINE_END);
//        this.add(buttonToolbar, BorderLayout.NORTH);
        this.add(toolBarPanel, BorderLayout.NORTH);
        this.add(clipToolbar, BorderLayout.EAST);
        updateCheckboxSelectAll();

        Dimension finalButtonSize = new Dimension(maxWidth, 30);
        int finalMaxWidth = maxWidth;
        jToggleButtonSet.stream().forEach(jToggleButton -> jToggleButton.setPreferredSize(new Dimension(jToggleButton.getPreferredSize().width, 30)));

    }



    private void restoreMaskButtonStates ()
    {
        if (resetMaskButtonTimer != null && resetMaskButtonTimer.isRunning()) {
            resetMaskButtonTimer.stop();
        }

        int timeout =  FgPreferences.get().get(PREF_RESET_MASK_BUTTON_SECONDS, 5) * 1000;
        if (timeout > 0)
        {
            resetMaskButtonTimer = new Timer(timeout, new ActionListener()
            {
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    maskToggleButton.setSelected(true);
                    maskFirstLineToggleButton.setSelected(true && prefMaskFirstLine);
                    updateCheckboxSelectAll();
                    updateText();
                }
            });
            resetMaskButtonTimer.setRepeats(false);
            resetMaskButtonTimer.start();
        }
    }



    public void setButtonToolbarVisible (boolean mode)
    {
        buttonToolbar.setVisible(mode);
    }


    private void searchDirection(int dir)
    {
        if (hitList.isEmpty())
        {
            return;
        }

        caretPointer += dir;
        if (caretPointer < 0)
        {
            caretPointer = hitList.size() - 1;
        }
        else if (caretPointer >=  hitList.size())
        {
            caretPointer = 0;
        }

        if (caretPointer >= 0)
        {
            setCaretPosition(hitList.get(caretPointer));
        }
    }

    private void searchPrevious()
    {
        searchDirection(-1);
    }
    private void searchNext ()
    {
        searchDirection(1);
    }
    private void resetSearch ()
    {
        caretPointer = -1;
        fgTextFilter.setText("");
        hitList.clear();
        textPane.getHighlighter().removeAllHighlights();
    }

    private void setCaretPosition (int position)
    {
        textPane.setCaretPosition(position);
        textPane.getHighlighter().removeAllHighlights();
        try
        {
            int ex = position + fgTextFilter.getText().length();
            textPane.getHighlighter().addHighlight(position,
                    ex,
                    DefaultHighlighter.DefaultPainter);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }

    }

    private void loadPreferences ()
    {
        setMaskPasswordPatterns(FgPreferences.get().get(FgPreferences.PREF_PASSWORD_MASK_PATTERNS, getDefaultMaskPatterns()));
        setMaskUsernamePatterns(FgPreferences.get().get(FgPreferences.PREF_USERNAME_MASK_PATTERNS, getDefaultMaskUsernamePatterns()));
        prefCompressBlankLines = FgPreferences.get().get(FgPreferences.PREF_SQUEEZE_LINES, prefCompressBlankLines);
        prefOpenUrlCommand = FgPreferences.get().get(FgPreferences.PREF_URL_OPEN_COMMAND, prefOpenUrlCommand);
        prefOpenUrls = FgPreferences.get().get(FgPreferences.PREF_OPEN_URLS, prefOpenUrls);
        prefClipboardToolbarVisible = FgPreferences.get().get(FgPreferences.PREF_SHOW_PASSWORD_SHORTCUT_BAR, prefClipboardToolbarVisible);
        prefMaskFirstLine = FgPreferences.get().get(FgPreferences.PREF_MASK_FIRST_LINE, prefMaskFirstLine);
        prefTextAreaFontSize = FgPreferences.get().get(FgPreferences.PREF_TEXTAREA_FONT_SIZE, prefTextAreaFontSize);
        prefTocGeneration = FgPreferences.get().get(FgPreferences.PREF_TOC_GENERATION, prefTocGeneration);
        prefTocPrefix = FgPreferences.get().get(FgPreferences.PREF_TOC_PREFIX, prefTocPrefix);
        FgPreferences.get().get(PREF_RESET_MASK_BUTTON_SECONDS, 5);

    }


    private void updateCheckboxSelectAll ()
    {
        this.openUrlButton.setVisible(isDetectUrls());
        checkSelectAll.setSelected(isMask()
                && isMaskFirstLine()
                && isCompressBlankLines()
                && isDetectUrls());
    }


    public boolean isCompressBlankLines ()
    {
        return compressBlankLinesButton.isSelected();
    }

    public void setCompressBlankLines (boolean compressBlankLines)
    {
        FgPreferences.get().put(FgPreferences.PREF_SQUEEZE_LINES, compressBlankLines);
        if (compressBlankLinesButton != null)
        {
            this.compressBlankLinesButton.setSelected(compressBlankLines);
        }
    }
    public void setCompressBlankLines (boolean compressBlankLines, boolean update)
    {
        setCompressBlankLines(compressBlankLines);
        if (update)
        {
            updateLaterTextAndStati();
        }
    }

    public void setOpenUrls (boolean openUrls, boolean update)
    {
        FgPreferences.get().put(FgPreferences.PREF_OPEN_URLS, openUrls);
        if (this.openUrlButton != null)
        {
            this.openUrlButton.setSelected(openUrls);
        }
        if (update)
        {
            updateLaterTextAndStati();
        }
    }

    private void updateLaterTextAndStati ()
    {
        SwingUtilities.invokeLater(() ->
        {
            updateText();
            updateCheckboxSelectAll();
            textPane.requestFocus();
        });
    }


    public boolean isMaskFirstLine ()
    {
        return maskFirstLineToggleButton.isSelected();
    }

    public void setMaskFirstLine (boolean maskFirstLine)
    {
        maskFirstLineToggleButton.setSelected(maskFirstLine);
        if (!isMaskFirstLine()) {
            restoreMaskButtonStates();
        }
    }

    public boolean isMask ()
    {
        return maskToggleButton.isSelected();
    }

    public void setMask (boolean mask)
    {
        maskToggleButton.setSelected(mask);
        if (!isMask()) {
            restoreMaskButtonStates();
        }
    }

    public boolean isDetectUrls ()
    {
        return detectUrlsToggleButton.isSelected();
    }

    public void setDetectUrls (boolean detectUrls)
    {
        this.detectUrlsToggleButton.setSelected(detectUrls);
    }


    public boolean isOpenUrls ()
    {
        return openUrlButton.isSelected();
    }

    public void setOpenUrls (boolean copyUrls)
    {
        this.openUrlButton.setSelected(copyUrls);
    }

    private String getPasswordLink (String password, String mask)
    {
        if (isDetectUrls())
        {
            String newMask = mask;
            // #10: backslashes not correcly handled
            // encode password text before adding to clipboard
            password = URLEncoder.encode(password);
            return getLink(PASSWORD_PREFIX + password, newMask, COLOR_PASSWORD);
        }
        else
        {
            return colored(mask);
        }
    }

    private String getClipboardLink (String url)
    {
        return getLink(url, url, COLOR_CLIPBOARD);
    }

    private String getEmailLink (String url)
    {
        return getLink(url, url, COLOR_EMAIL);
    }

    private String getLink (String url)
    {
        return getLink(url, url, COLOR_LINK);
    }

    private String getLink (String url, String text)
    {
        return getLink(url, text, COLOR_LINK);
    }

    private String getLink (String href, String text, String color)
    {
        if (isDetectUrls())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<a style='color:" +
                    color +
                    ";' href='");
            sb.append(href);
            sb.append("'>");
            sb.append(text);
            sb.append("</a>");
            return sb.toString();
        }
        else
        {
            return text;
        }
    }

    private String getBodyTag (boolean start)
    {
        if (start)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<body ");
            sb.append("style='");
            sb.append("font-family: monospace;");
            sb.append("font-size: " + prefTextAreaFontSize + "pt;");
            sb.append("color:#eeeeee; ");
            sb.append("background-color: #3e3e3e;");
            sb.append("'");
            sb.append(">");
            return sb.toString().trim();
        }
        else
        {
            return "</body>";
        }
    }


    private String getBold (String text)
    {
        return String.format("<b>%s</b>", text);
    }
    private String getBold (String text, int pad)
    {
        return String.format("<b>%-"+ pad + "s</b>", text);
    }

    private void resetClipboardCommands ()
    {
        setClipToolbarVisibility(false);
        clipToolbar.removeAll();
    }

    private void addClipboardCommand (int nr, String password)
    {
        JClipboardButton clipboardButton = new JClipboardButton(nr, password);
        if (nr > 0 && nr < 10)
        {
            clipboardButton.setMnemonic(KeyEvent.VK_0 + nr);
        }
        else if (nr == 10)
        {
            clipboardButton.setMnemonic(KeyEvent.VK_0);
        }
        clipToolbar.add(clipboardButton);
    }





    private String colored (String text)
    {
        return SPAN_COL + text + "</span>";
    }
    private String getMaskedText ()
    {
        Scanner scanner = new Scanner(this.plainText);
        String maskedText = "";
        String plainMaskedText = "";
        int passwordCount = 1;
        int lineNr = 0;
        int blankCount = 0;

        resetClipboardCommands();
        int headerLine = -1;
        int lastHeaderLine = -1;
        boolean otpContained = false;

        while (scanner.hasNextLine())
        {
            String line = StringUtils.trimEnd(scanner.nextLine());
            String plainLine = line;

            // BLANK LINE COMPRESSION ==================================
            boolean isBlankLine = line.matches("^\\s*$");
            if (isBlankLine)
            {
                blankCount++;
                if (blankCount > 1 && isCompressBlankLines())
                {
                    continue;
                }
            }
            else
            {
                blankCount = 0;
            }


            lineNr++;

            boolean lineHandled = false;

            for (LineMaskingOrder order : LineMaskingOrder.values())
            {
                switch (order)
                {
                    case OTP_CODES:
                        if (!lineHandled && isDetectUrls())
                        {
                            String regexp = "^(.*)(otpauth://totp/[^\\s]+)(.*)$";
                            Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find() && matcher.groupCount() >= 3)
                            {
                                String urlString = matcher.group(2).trim();
                                OTP otp = OtpQRUtil.getOtp(urlString);
                                if (otp != null)
                                {
                                    otpContained = true;
                                    String before = matcher.group(1);
                                    line = before + "\n";
                                    int pad = 15;
                                    line += getBold("-------------------------------------------------") + "\n";
                                    line += getBold("- OTP: " + otp.getIssuer() + " / " + otp.getName()) + "\n";
                                    line += getBold("-------------------------------------------------") + "\n";
                                    line += getBold("  Name:", pad) + otp.getName() + "\n";
                                    line += getBold("  OTP:", pad) + getClipboardLink(OtpQRUtil.otpString(otp.getSecret())) + "\n";

                                    String remainining = "\n";
                                    String valUntil = OtpQRUtil.getValidUntil(otp.getPeriod(), remainingOtpTime);
                                    line += getBold("  Valid until:", pad) + valUntil + remainining;

                                    line += getBold("  Issuer:", pad) + otp.getIssuer() + "\n";


                                    String mask = PASSWORD_MASK + passwordCount;
                                    if (isMask())
                                    {
                                        line += getBold("  Secret:", pad) + getPasswordLink(otp.getSecret(), mask) + "\n";
                                        addClipboardCommand(passwordCount, otp.getSecret());
                                        passwordCount++;
                                    }
                                    else
                                    {
                                        line += getBold("  Secret:", pad) + otp.getSecret() + "\n";
                                    }

                                    String after = matcher.group(3);
                                    line += after;
                                    lineHandled = true;
                                }
                            }

                        }
                        break;

                    case HTTP_LINKS:
                        //--------------------------------------------------------------------------------------
                        // HTTP link detection
                        //--------------------------------------------------------------------------------------
                        if (!lineHandled)
                        {
                            // String regexp = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)";
                            //  regexp ="(https?:\\/\\/)(www\\.)?([-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b)([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)";
                            String regexp = "https?://[^\\s]*";
                            Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find())
                            {
                                if (isOpenUrls())
                                {
                                    line = matcher.replaceAll(getLink("$0"));
                                }
                                else
                                {
                                    line = matcher.replaceAll(getClipboardLink("$0"));
                                }
                                lineHandled = true;
                            }
                        }
                        break;
                    case USER_NAMES:
                        //--------------------------------------------------------------------------------------
                        // Username detection
                        //--------------------------------------------------------------------------------------
                        if (!lineHandled)
                        {
                            for (String p : getMaskUsernamePatterns())
                            {
                                String regexp = "^(\\s*" + p + "[-a-z/A-Z0-9\\s]*[:=\\s]\\s*)(.*)\\s*$";
                                Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find() && matcher.groupCount() >= 2)
                                {
                                    try
                                    {
                                        String userName = matcher.group(2).trim();
                                        line = matcher.replaceAll("$1" + getClipboardLink(userName));
                                        lineHandled = true;
                                        break;
                                    }
                                    catch (Exception ex)
                                    {
                                        line += "ERROR : " + ex.getMessage();
                                    }
                                }
                            }
                        }
                        break;

                    case EMAIL_ADRESSES:
                        //--------------------------------------------------------------------------------------
                        // email detection
                        //--------------------------------------------------------------------------------------
                        if (!lineHandled && !isPasswordMatch(line))
                        {
                            String regex = "(^.*?)([A-Za-z0-9+_.-]+@[^\\s]+)(\\s?.*)$";
                            Pattern pattern = Pattern.compile(regex);

                            Matcher matcher = pattern.matcher(line);
                            if (matcher.matches() && matcher.groupCount() > 2)
                            {
                                line = matcher.replaceAll("$1" + getEmailLink(matcher.group(2)) + "$3");
                                lineHandled = true;
                            }
                        }
                        break;

                    case PASSWORDS_FILES:
                        if (!lineHandled)
                        {
                            Iterator<Map.Entry<String, String>> iter;
                            // Sort entries by legth of filename
                            iter = mainFrame.getSecretsList().entrySet()
                                    .stream()
                                    .sorted((o1, o2) -> o2.getValue().length() - o1.getValue().length())
                                    .collect(Collectors.toList()).iterator();
                            Map<String, String> replacementMap = new HashMap<>();
                            int i = 0;
                            while (iter.hasNext())
                            {
                                Map.Entry<String, String> entry = iter.next();
                                String passwordFile = entry.getValue();
                                if (line.contains(passwordFile))
                                {
                                    String link = getLink(GPG_FILE_PASSWORD_PREFIX + entry.getKey(), passwordFile, COLOR_GPG_FILE);
                                    String tmpSymbol = "FG_LINE_SYMBOL_" + i++;
                                    replacementMap.put(tmpSymbol, link);
                                    line = line.replaceAll(passwordFile, tmpSymbol);
                                }
                            }
                            if (!replacementMap.isEmpty())
                            {
                                final String[] finalLine = {line};
                                replacementMap.entrySet().stream().forEach(e -> finalLine[0] = finalLine[0].replaceAll(e.getKey(), e.getValue()));
                                line = finalLine[0];
                            }
                        }
                        break;

                    case PASSWORDS_TEXT:
                        //--------------------------------------------------------------------------------------
                        // Mask passwords in text
                        //--------------------------------------------------------------------------------------
                        if (!lineHandled && isMask() && getMaskPasswordPatterns() != null && !getMaskPasswordPatterns().isEmpty())
                        {
                            for (String p : getMaskPasswordPatterns())
                            {
                                String regexp = "^(\\s*" + p + "[-a-z/A-Z0-9\\s]*[:=\\s]\\s*)(.*)\\s*$";
                                Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.matches() && matcher.groupCount() >= 2)
                                {
                                    try
                                    {
                                        String password = matcher.group(2).trim();
                                        String mask = PASSWORD_MASK + passwordCount;
                                        line = matcher.replaceAll("$1" + getPasswordLink(password, mask));
                                        plainLine = matcher.replaceAll("$1" + mask);
                                        addClipboardCommand(passwordCount, password);
                                        passwordCount++;
                                        lineHandled = true;
                                        // do not apply multiple matchers for one line
                                        break;
                                    }
                                    catch (Exception ex)
                                    {
                                        line += "ERROR : " + ex.getMessage();
                                    }
                                }
                            }
                        }

                        break;
                    case PASSWORD_FIRST_LINE:
                        //--------------------------------------------------------------------------------------
                        // Mask 1st line passwords
                        //--------------------------------------------------------------------------------------
                        if (!lineHandled && lineNr == 1 && isMaskFirstLine() && !isBlankLine)
                        {
                            addClipboardCommand(passwordCount, line.trim());
                            line = getPasswordLink(line.trim(), PASSWORD_MASK + passwordCount);
                            lineHandled = true;
                            passwordCount++;
                        }
                        break;
                }
            }


            // TOC GENERATION ==================================
            if (!lineHandled && prefTocGeneration)
            {
                AtomicReference<String> lineRef = new AtomicReference<>(line);
                AtomicReference<Integer> headerLineRef = new AtomicReference<>(headerLine);
                AtomicReference<Integer> lastHeaderLineRef = new AtomicReference<>(lastHeaderLine);
                handleTocGenerationInText(lineRef, headerLineRef, lastHeaderLineRef, plainMaskedText, lineNr);
                line = lineRef.get();
                headerLine = headerLineRef.get();
                lastHeaderLine = lastHeaderLineRef.get();
            }

            // line feed
            plainMaskedText += plainLine;
            maskedText += "<pre style='margin: 0px;'>" + line + "</pre>";
        }

        // add trailing blank lines to avoid clipping of last line (for Robin)
        for (int i = 0; i < 2; ++i)
        {
            maskedText += "<pre style='margin: 0px;'> " + System.lineSeparator() + "</pre>";
        }

        setClipToolbarVisibility(clipToolbar.getComponentCount() > 0);

        if (otpContained)
        {
            setupOtpTimer(remainingOtpTime.get() * 1000);
            // setupOtpTimer(5000);
        }
        remainingOtpTimeLabel.setVisible(otpContained);
        return getToc() + maskedText;
    }

    private void stopOtpTimer()
    {
        if (otpTimer != null)
        {
            otpTimer.stop();
            otpTimer = null;
        }
    }

    private void setupOtpTimer(int remainingMillis)
    {
        stopOtpTimer();

        otpTimer = new Timer(remainingMillis, e -> {
            otpRefresh();
        });
        otpTimer.setRepeats(false);
        otpTimer.start();
    }

    private void otpRefresh()
    {
        updateText();
    }


    private void handleTocGenerationInText(AtomicReference<String> lineRef,
                                           AtomicReference<Integer> headerLineRef,
                                           AtomicReference<Integer> lastHeaderLineRef,
                                           String plainMaskedText,
                                           int lineNr)
    {
        String line = lineRef.get();
        int headerLine = headerLineRef.get();
        int lastHeaderLine = lastHeaderLineRef.get();

        if (prefTocPrefix == null || prefTocPrefix.isEmpty())
        {
            String regexp = "^=+$";
            Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches())// && matcher.groupCount() >= 2)
            {
                headerLine = lineNr;
            }

            String headerRegexp = "^(=*\\s*)(.+)$";
            Pattern headerPattern = Pattern.compile(headerRegexp, Pattern.CASE_INSENSITIVE);
            Matcher headerMatcher = headerPattern.matcher(line);
            if (headerMatcher.matches() && headerMatcher.groupCount() >= 2)
            {
                String headerText = headerMatcher.group(2);
                boolean neverHit = lastHeaderLine <= 0;
                if (lineNr == headerLine + 1 && (neverHit || lineNr > lastHeaderLine + 2))
                {
                    String key = TOC_HEADER_PREFIX + String.format("%05d", plainMaskedText.length());
                    line = headerMatcher.group(1) + getJumpToHeader(key, headerText);
                    tocMap.put(key, headerText);
                    lastHeaderLine = lineNr;
                }
            }
        }
        else
        {
            String headerRegexp = "^" + prefTocPrefix + "\\s*(.+)$";
            Pattern headerPattern = Pattern.compile(headerRegexp);
            Matcher headerMatcher = headerPattern.matcher(line);
            if (headerMatcher.matches() && headerMatcher.groupCount() >= 1)
            {
                String headerText = headerMatcher.group(1);
                String key = TOC_HEADER_PREFIX + String.format("%05d", plainMaskedText.length());
                line = getJumpToHeader(key, headerText);
                tocMap.put(key, headerText);
            }
        }

        headerLineRef.set(headerLine);
        lineRef.set(line);
        lastHeaderLineRef.set(lastHeaderLine);
    }





    private String getToc ()
    {
        if (!prefTocGeneration || tocMap == null || tocMap.isEmpty())
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        Iterator<Map.Entry<String, String>> tocIter = tocMap.entrySet().iterator();

        sb.append(TOC_START);
        sb.append("<ol>");
        while (tocIter.hasNext())
        {
            Map.Entry<String, String> tocEntry = tocIter.next();
            String text = tocEntry.getValue();
            if (text != null && !text.trim().isEmpty())
            {
                sb.append("<li>");
                sb.append(getLink(tocEntry.getKey(), text.trim(), COLOR_JUMP_TOP));
                sb.append("</li>");
            }
        }
        sb.append("</ol>");
        sb.append("<span style='color: " + COLOR_END_OF_TOC + ";'>");
        sb.append(TOC_END);
        sb.append("</span>");
        return sb.toString();
    }

    private String getJumpToHeader(String id, String text)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getLink(TOC_HEADER_PREFIX + "0", text.trim(), COLOR_JUMP_TOP));
        return sb.toString();
    }



    private boolean isPasswordMatch (String line)
    {
        if (getMaskPasswordPatterns() != null && !getMaskPasswordPatterns().isEmpty())
        {
            for (String p : getMaskPasswordPatterns())
            {
                String regexp = "^(\\s*" + p + "[-a-z/A-Z0-9\\s]*[:=\\s]\\s*)(.*)\\s*$";
                Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);
                return (matcher.matches() && matcher.groupCount() >= 2);
            }
        }
        return false;
    }




    private void setClipToolbarVisibility (boolean visible)
    {
        this.clipToolbar.setVisible(visible && prefClipboardToolbarVisible);
    }



    public void setStatusText (String text)
    {
        setText(null, text);
    }

    public String getStatusText ()
    {
        return this.textAreaError.getText();
    }


    private void updateText ()
    {
        resetClipboardCommands();
        resetSearch();
        tocMap.clear();

        Point p = scrollPaneTextArea.getViewport().getViewPosition();

        StringBuilder textAreaText = new StringBuilder();
        textAreaText.append("<html>");
        textAreaText.append(getBodyTag(true));
        textAreaText.append(getMaskedText());
        textAreaText.append(getBodyTag(false));
        textAreaText.append("</html>");
        this.textPane.setText(textAreaText.toString());

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run ()
            {
                scrollPaneTextArea.getViewport().setViewPosition(p);
                textPane.repaint();

            }
        });
    }


    public void setText (String text, String err)
    {
        setText(text, err, "");
    }


    public void setText (String text, String err, String shortFileName)
    {
        setText(text, err, shortFileName, null);
    }
    public void setText (String text, String err, String shortFileName, String fullFileName)
    {
        if (text != null)
        {
            setLabelFileName(shortFileName == null ? "" : shortFileName  + TagsStore.getTagsOfFile(fullFileName));
            this.plainText = text;
            updateText();
        }


        String errText = err == null ? "" : err;
        errText = errText.replaceAll("\\n$", "");
        errText = errText.replaceAll("\\r\\n$", "");
        textAreaError.setText(errText);
//        scrollPaneTextAreaError.setVisible(err != null && !err.isEmpty());
    }


    public void clear (String info)
    {
        stopOtpTimer();
        setText("", info);
        fgTextFilter.setText("");
    }



    public boolean isClear ()
    {
        return this.plainText == null || this.plainText.isEmpty();
    }

    @Override
    public void propertyChange (PropertyChangeEvent propertyChangeEvent)
    {
        switch (propertyChangeEvent.getPropertyName())
        {
            case PREF_RESET_MASK_BUTTON_SECONDS:
                restoreMaskButtonStates();
                break;
            case FgPreferences.PREF_PASSWORD_MASK_PATTERNS:
                setMaskPasswordPatterns((String)propertyChangeEvent.getNewValue());
                break;
            case FgPreferences.PREF_USERNAME_MASK_PATTERNS:
                setMaskUsernamePatterns((String)propertyChangeEvent.getNewValue());
                break;
            case FgPreferences.PREF_SQUEEZE_LINES:
                setCompressBlankLines((Boolean) propertyChangeEvent.getNewValue(), true);
                break;
            case FgPreferences.PREF_OPEN_URLS:
                setOpenUrls((Boolean) propertyChangeEvent.getNewValue(), true);
                break;
            case FgPreferences.PREF_URL_OPEN_COMMAND:
                prefOpenUrlCommand = FgPreferences.get().get(FgPreferences.PREF_URL_OPEN_COMMAND, prefOpenUrlCommand);
                break;
            case FgPreferences.PREF_MASK_FIRST_LINE:
                prefMaskFirstLine = FgPreferences.get().get(FgPreferences.PREF_MASK_FIRST_LINE, prefMaskFirstLine);
                restoreMaskButtonStates();
                break;
            case FgPreferences.PREF_SHOW_PASSWORD_SHORTCUT_BAR:
                prefClipboardToolbarVisible = FgPreferences.get().get(FgPreferences.PREF_SHOW_PASSWORD_SHORTCUT_BAR, prefClipboardToolbarVisible);
                SwingUtilities.invokeLater(() -> updateText());
                break;
            case FgPreferences.PREF_TEXTAREA_FONT_SIZE:
                prefTextAreaFontSize = FgPreferences.get().get(FgPreferences.PREF_TEXTAREA_FONT_SIZE, prefTextAreaFontSize);
                SwingUtilities.invokeLater(() -> updateText());
                break;
            case FgPreferences.PREF_TOC_GENERATION:
                prefTocGeneration = FgPreferences.get().get(FgPreferences.PREF_TOC_GENERATION, prefTocGeneration);
                SwingUtilities.invokeLater(() -> updateText());
                break;
            case FgPreferences.PREF_TOC_PREFIX:
                prefTocPrefix = FgPreferences.get().get(FgPreferences.PREF_TOC_PREFIX, prefTocPrefix);
                if (prefTocPrefix != null) prefTocPrefix = prefTocPrefix.trim();
                SwingUtilities.invokeLater(() -> updateText());
                break;
        }
    }

    @Override
    public void handleTextFilterChanged (String filter)
    {
        mainFrame.startTimer();

        String documentText = getDocumentText();
        if (documentText == null)
        {
            return;
        }


        textPane.getHighlighter().removeAllHighlights();
        hitList.clear();

        int ix = documentText.toLowerCase().indexOf(fgTextFilter.getText().toLowerCase());
        while (ix >= 0 && !fgTextFilter.getText().isEmpty())
        {
            hitList.add(ix);
            ix++;
            ix = documentText.toLowerCase().toLowerCase().indexOf(fgTextFilter.getText().toLowerCase(), ix);
        }
        if (hitList.size() > 0)
        {
            caretPointer = 0;
            setCaretPosition(hitList.get(caretPointer));
        }
    }

    private String getDocumentText()
    {
        String documentText;
        Document document = textPane.getDocument();
        try
        {
            documentText = document.getText(0, document.getLength());
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
            return null;
        }
        return documentText;
    }

    @Override
    public void requestFocus()
    {
        textPane.requestFocus();
    }

    public Component getFocusComponent ()
    {
        return textPane;
    }
}
