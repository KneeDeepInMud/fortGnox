/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mockenhaupt.jgpg;


import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockenhaupt.jgpg.JgpgPreferences.*;

/**
 *
 * @author fmoc
 */
public class JGPGProcess implements PropertyChangeListener
{
    private final static Logger LOGGER = Logger.getLogger(JGPGProcess.class.getName());

    public static final String LINE_SEP = System.lineSeparator();
    private String prefCharset = "ISO-8859-15";
    private Charset charset = Charset.forName(prefCharset);

    @Override
    public void propertyChange (PropertyChangeEvent propertyChangeEvent)
    {
        LOGGER.fine("propertyChangeEvent = " + propertyChangeEvent);
        switch (propertyChangeEvent.getPropertyName())
        {
            case PREF_GPG_HOMEDIR:

                prefGpgHome = (String)propertyChangeEvent.getNewValue();
                break;
            case PREF_GPGCONF_COMMAND:
                prefGpgConfCommand = (String)propertyChangeEvent.getNewValue();
                break;

            case PREF_GPG_COMMAND:
                prefGpgExeLocation = (String)propertyChangeEvent.getNewValue();
                break;

            case PREF_CHARSET:
                prefCharset = (String)propertyChangeEvent.getNewValue();
                charset = Charset.forName(prefCharset);
                break;

            case PREF_SECRETDIRS:
                prefSecretDirsString = (String)propertyChangeEvent.getNewValue();
                handleSecretPreferenceChanged();
                break;

            case PREF_USE_PASS_DIALOG:
                prefUsePasswordDialog = (Boolean)propertyChangeEvent.getNewValue();
                break;

            case PREF_USE_GPG_AGENT:
                prefConnectToGpgAgent = (Boolean)propertyChangeEvent.getNewValue();
                break;

        }
    }

    interface SecretListListener
    {
        void handleSecretList (String[] list);
    }

    interface ResultListListener
    {
        void setUserTextareaText (String out, String err);
    }


    abstract class GpgRunnable implements Runnable
    {
        private boolean toClipboard;
        private String password;
        private String filename;
        private String command;

        public GpgRunnable (boolean toClipboard, String password, String filename)
        {
            this.toClipboard = toClipboard;
            this.password = password;
            this.filename = filename;
        }

        public GpgRunnable (String command)
        {
            this.command = command;
        }

        public String getCommand ()
        {
            return command;
        }

        public boolean isToClipboard ()
        {
            return toClipboard;
        }

        public String getPassword ()
        {
            return password;
        }

        public String getFilename ()
        {
            return filename;
        }
    }

    private String prefGpgConfCommand = "gpgconf";
    private String prefGpgExeLocation = "gpg";
    private String prefGpgHome = "";
    private String prefSecretDirsString = "";
    protected List<String> secretdirs = new ArrayList<String>();
    boolean isWindows;
    HashSet<SecretListListener> secretListeners = new HashSet<SecretListListener>();
    HashSet<ResultListListener> resultListeners = new HashSet<ResultListListener>();

    private static String lastClipText;

    private boolean prefUsePasswordDialog = true;
    private boolean prefConnectToGpgAgent = true;

    public boolean isPrefUsePasswordDialog ()
    {
        return prefUsePasswordDialog;
    }

    public void setPrefUsePasswordDialog (boolean prefUsePasswordDialog)
    {
        this.prefUsePasswordDialog = prefUsePasswordDialog;
    }

    public boolean isPrefConnectToGpgAgent ()
    {
        return prefConnectToGpgAgent;
    }

    public void setPrefConnectToGpgAgent (boolean prefConnectToGpgAgent)
    {
        this.prefConnectToGpgAgent = prefConnectToGpgAgent;
    }

    void addSecretListListener (SecretListListener listener)
    {
        secretListeners.add(listener);
        listener.handleSecretList(secretList);
    }

    void addResultListener (ResultListListener listener)
    {
        resultListeners.add(listener);
        listener.setUserTextareaText("", "");
    }

    private void notifySecretListeners ()
    {
        Iterator<SecretListListener> iter = secretListeners.iterator();
        while (iter.hasNext())
        {
            iter.next().handleSecretList(secretList);
        }
    }

    private void notifyResultListeners (String out)
    {
        notifyResultListeners(out, "");
    }
    private void notifyResultListeners (String out, String err)
    {
        Iterator<ResultListListener> iter = resultListeners.iterator();
        while (iter.hasNext())
        {
            iter.next().setUserTextareaText(out, err);
        }
    }

    private void handleSecretPreferenceChanged ()
    {
        PreferencesAccess preferences = JgpgPreferences.get();
        File f = new File(prefSecretDirsString);
        secretdirs.clear();

        if (prefSecretDirsString.isEmpty())
        {
            preferences.put(PREF_SECRETDIRS, f.getAbsolutePath());
            secretdirs.add(f.getAbsolutePath());
        }
        else
        {
            // found a stored property
            if (prefSecretDirsString.contains(";"))
            {
                String revisedProp = "";
                // prop is an array
                for (String token : prefSecretDirsString.split(";"))
                {
                    if (!token.isEmpty())
                    {
                        f = new File(token);
                        if (f.exists())
                        {
                            secretdirs.add(f.getAbsolutePath());
                            revisedProp += token;
                            revisedProp += ";";
                        }
                    }
                }
                revisedProp = revisedProp.replaceAll(";$", "");
                preferences.put(PREF_SECRETDIRS, revisedProp);
            }
            else
            {
                // prop is single directrory
                f = new File(prefSecretDirsString);
                preferences.put(PREF_SECRETDIRS, f.getAbsolutePath());
                secretdirs.add(f.getAbsolutePath());
            }
        }

        rebuildSecretList();
    }

    //    protected Preferences preferences;
    private final Component mainWindow;
    public JGPGProcess (Component mainWindow)
    {
        this.mainWindow = mainWindow;

        PreferencesAccess preferences = JgpgPreferences.get();
        preferences.addPropertyChangeListener(this);

        File defaultFileLocation = new File("gpg");
        prefGpgExeLocation = preferences.get(JgpgPreferences.PREF_GPG_COMMAND, defaultFileLocation.getAbsolutePath());

        prefCharset = preferences.get(PREF_CHARSET, prefCharset);
        charset = Charset.forName(prefCharset);

        defaultFileLocation = new File("gnupg");
        prefGpgHome = preferences.get(PREF_GPG_HOMEDIR, defaultFileLocation.getAbsolutePath());

        prefGpgConfCommand = preferences.get(JgpgPreferences.PREF_GPGCONF_COMMAND, "gpgconf");

        defaultFileLocation = new File("/secret/password");
        prefSecretDirsString = preferences.get(JgpgPreferences.PREF_SECRETDIRS, defaultFileLocation.getAbsolutePath());
        handleSecretPreferenceChanged();

        isWindows = preferences.get(JgpgPreferences.PREF_IS_WINDOWS, true);

        this.prefUsePasswordDialog = preferences.get(JgpgPreferences.PREF_USE_PASS_DIALOG, prefUsePasswordDialog);
        this.prefConnectToGpgAgent = preferences.get(JgpgPreferences.PREF_USE_GPG_AGENT, prefConnectToGpgAgent);
    }

    String[] secretList;
    private String errorState = "";


    public String getShortFileName (String filename, boolean abbrev)
    {
        return getShortFileName(filename, null, abbrev);
    }

    public String getShortFileName (String filename, String info, boolean abbrev)
    {
        String ret;
        if (abbrev)
        {
            ret = abbrevCompleteFileMap.get(filename);
            if (info != null && !info.isEmpty())
            {
                ret = ret.replace(")", " - " + info + ")");
            }
        }
        else
        {
            ret = completeFileMap.get(filename);
            if (info != null && !info.isEmpty())
            {
                ret = ret + " - " + info;
            }
        }


        if (ret == null || ret.isEmpty())
        {
            ret = filename;
        }
        return ret;
    }

    private HashMap<String, String> fileMap = new HashMap<String, String>();
    private HashMap<String, String> completeFileMap = new HashMap<String, String>();
    private HashMap<String, String> abbrevCompleteFileMap = new HashMap<String, String>();

    Set<String> skipExtensions = new HashSet<>(Arrays.asList("tgz",
            "jpg",
            "gif",
            "doc",
            "docx",
            "p12",
            "xls",
            "xlsx",
            "zip"));

    private void rebuildSecretList ()
    {
        fileMap.clear();

        Map<String, List<String>> folderToPathKeysMap = new HashMap<>();

        for (String secDir : secretdirs)
        {
            File f = new File(secDir);
            File[] fList = f.listFiles(new FilenameFilter()
            {

                public boolean accept (File dir, String name)
                {
                    if (!(name.toLowerCase().endsWith(".asc") || name.toLowerCase().endsWith(".gpg")))
                    {
                        return false;
                    }

                    return skipExtensions.stream().noneMatch(ext ->
                    {
                        String name2 = name.toLowerCase().replace(".asc", "").replace(".gpg", "");
                        return name2.endsWith("." + ext);
                    });
                }
            });

            if (fList == null)
            {
                errorState = "Invalid secret directory in preferences " + secretdirs;
                handleGpgResult(errorState, "", 1);
                break;
            }

            errorState = "";

            for (int i = 0; i < fList.length; ++i)
            {
                String shortName = fList[i].getName().replace(".asc", "").replace(".gpg", "");
                String dir = "";
                if (fList[i].getParentFile() != null)
                {
                    dir = fList[i].getParentFile().getName();
                    dir += "/";
                }
//                String ddir = dir.isEmpty() ? shortName : shortName + " (" + dir + ")";
                String ddir = dir + shortName;
                fileMap.put(fList[i].getAbsolutePath(), ddir);
                folderToPathKeysMap.computeIfAbsent(dir, d ->  new ArrayList<>()).add(fList[i].getAbsolutePath());
            }
        }

        int secretsSize = folderToPathKeysMap.entrySet().stream().mapToInt(stringListEntry -> stringListEntry.getValue().size()).sum();
        secretList = new String[secretsSize];

        AtomicInteger ix = new AtomicInteger();
        folderToPathKeysMap.keySet().stream().sorted(
                (s, t1) -> s.toLowerCase().compareTo(t1.toLowerCase()) * -1)
                .forEach(
                dir -> {
                    folderToPathKeysMap.get(dir).stream().sorted((a,b) -> {
                        return a.toLowerCase().compareTo(b.toLowerCase());
                    }).forEach(s -> {
                        secretList[ix.getAndIncrement()] = s;
                    });
                }
        );

        completeFileMap.putAll(fileMap);

        final String sepChar = Pattern.quote(File.separator);
        final Pattern bnPattern = Pattern.compile("([^"+ sepChar + "]+)" + sepChar +  "([^" + sepChar + "]+)$");
        completeFileMap.entrySet().stream().forEach(stringStringEntry -> {
            Matcher m = bnPattern.matcher(stringStringEntry.getKey());

            while (m.find() && m.groupCount() == 2)
            {
                String shortName = m.group(2);
                shortName = shortName.replace(".asc", "").replace(".gpg", "");

                String longName = shortName + " (" + m.group(1) + ")";

//                if (!abbrevCompleteFileMap.containsValue(shortName))
//                {
//                    abbrevCompleteFileMap.put(stringStringEntry.getKey(), shortName);
//                }
//                else
//                {
                    abbrevCompleteFileMap.put(stringStringEntry.getKey(), longName);
//                }
            }
        });

        notifySecretListeners();
    }
///home/fmoc/work/password;/home/fmoc/work/password/orthogon;/home/fmoc/work/password/secret
    private synchronized void handleGpgResult (String err, String out,
                                               int exCode)
    {
        if (exCode == 0)
        {
            notifyResultListeners(out, err);
        }
        else
        {
            err = "Exitcode: " + exCode + LINE_SEP + err;
            notifyResultListeners("", err);
        }
    }


    public static void clearClipboardIfNotChanged ()
    {
        String currentClip = null;
        try
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            currentClip = (String)clipboard.getData(DataFlavor.stringFlavor);
        }
        catch (UnsupportedFlavorException e)
        {
            // ignore
        }
        catch (IOException e)
        {
            // ignore
        }
        if (currentClip != null && currentClip.equals(lastClipText))
        {
            clip("#-= FLUSHED PASSWORD FROM JGPG =-#");
        }
        lastClipText = "";
    }

    public static void clip (String text)
    {
        if (text == null || text.isEmpty())
        {
            return;
        }
        text = text.trim();
        lastClipText = text;
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }


    public void decrypt (String fname, String passphrase, boolean _toClipboard, int clipSeconds)
    {
        Thread t = new Thread(new GpgRunnable(_toClipboard, passphrase, fname)
        {
            public void run ()
            {
                String output = "";
                String error = "";
                String clipboardText = null;
                int lines = 0;

                List<String> cmdArgList = new ArrayList<>();
                cmdArgList.add(prefGpgExeLocation);
                cmdArgList.add("--batch");
                if (passphrase != null && !passphrase.isEmpty())
                {
                    cmdArgList.add("--passphrase-fd");
                    cmdArgList.add("0");
                }
                cmdArgList.add("--yes");
                cmdArgList.add("--homedir");
                cmdArgList.add(prefGpgHome);
                cmdArgList.add("--decrypt");

                File file = new File(this.getFilename());
                cmdArgList.add(file.getAbsolutePath());

                String[] cmds = cmdArgList.toArray(new String[]{});

                int exitValue = 0;


                Process gpgProcess;
                try
                {
                    gpgProcess = Runtime.getRuntime().exec(cmds);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handleGpgResult(e.getMessage(), "ERROR Runtime.getRuntime()", 555);
                    return;
                }

                final BufferedReader or = new BufferedReader(
                        new InputStreamReader(gpgProcess.getInputStream(), charset));
                final BufferedReader er = new BufferedReader(
                        new InputStreamReader(gpgProcess.getErrorStream(), charset));

                String pass = this.getPassword();
                if (pass != null && !pass.isEmpty())
                {
                    final BufferedWriter os = new BufferedWriter(
                            new OutputStreamWriter(gpgProcess.getOutputStream()));

                    pass += LINE_SEP;

                    try
                    {
                        os.write(pass);
                        os.flush();
                        Thread.sleep(200);
                    }
                    catch (IOException e)
                    {
                        handleGpgResult(e.getMessage(), "ERROR writing password", 666);
                        return;
                    }
                    catch (InterruptedException e)
                    {
                        handleGpgResult(e.getMessage(), "ERROR writing password", 664);
                        return;
                    }
                }


                CharsetEncoder encoder = charset.newEncoder();

                try
                {
                    while (stillActive(gpgProcess) || er.ready() || or.ready())
                    {
                        while (er.ready())
                        {
                            String s = er.readLine();
                            error += s + LINE_SEP;
                        }
                        while (or.ready())
                        {
                            String s = or.readLine();
                            lines++;
                            output += s + LINE_SEP;
                            if (!encoder.canEncode(s))
                            {
                                error = "Binary content in file detected";
                            }
                            if (this.isToClipboard() && clipboardText == null)
                            {
                                clipboardText = s;
                            }
                        }
                        try
                        {
                            Thread.sleep(10, 10);
                        }
                        catch (InterruptedException ex)
                        {
                            handleGpgResult(ex.toString(), "ERROR: Thread sleep", 1);
                            break;
                        }
                    }

                    exitValue = gpgProcess.exitValue();
                }
                catch (IOException ex)
                {
                    handleGpgResult(ex.toString(), "ERROR: reading, writing from streams", 1);
                }
                finally
                {
                    if (stillActive(gpgProcess))
                    {
                        gpgProcess.destroy();
                    }
                }

                // Call handler
                if (isToClipboard())
                {
                    if (clipboardText == null)
                    {
                        handleGpgResult(this.getFilename()  + LINE_SEP + error, "Error occurred copying text to clipboard", 1);
                    }
                    else
                    {
                        clip(clipboardText);
                        handleGpgResult("Copied first line of " + this.getFilename() +
                                "password to clipboard for " + clipSeconds + " seconds", "", 0);
                    }
                }
                else
                {                  
                    String linesText = LINE_SEP + lines + " lines";
                    linesText = ""; // TODO: make this configurable

                    handleGpgResult(this.getFilename() + LINE_SEP + error + linesText, output, exitValue);
                }
            }
        });

        try
        {
            t.start();
        }
        catch (Exception ex)
        {
            handleGpgResult(ex.getMessage(), "Internal error occurred starting decrytion thread", 333);
        }
    }

    protected synchronized boolean stillActive (Process p)
    {
        try
        {
            p.exitValue();
            return false;
        }
        catch (IllegalThreadStateException ex)
        {
            return true;    // still active!
        }
    }


    public void gpgAgendCommand (String command)
    {
        if (!isPrefConnectToGpgAgent())
        {
            return;
        }

        Thread t = new Thread(new GpgRunnable(command)
        {

            public void run ()
            {
                String output = "";
                String error = "";
                String[] cmds =
                        {
                                prefGpgConfCommand,
                                this.getCommand()
                        };

                Process gpgConnectAgentProcess = null;
                try
                {
                    gpgConnectAgentProcess = Runtime.getRuntime().exec(cmds);
                    final BufferedReader or = new BufferedReader(
                            new InputStreamReader(gpgConnectAgentProcess.getInputStream(), charset));
                    final BufferedReader er = new BufferedReader(
                            new InputStreamReader(gpgConnectAgentProcess.getErrorStream(), charset));

                    while (stillActive(gpgConnectAgentProcess) || er.ready() || or.ready())
                    {
                        while (er.ready())
                        {
                            String s = er.readLine();
                            error += s + LINE_SEP;
                        }
                        while (or.ready())
                        {
                            String s = or.readLine();
                            output += s + LINE_SEP;
                        }
                        Thread.sleep(10, 10);
                    }
                    handleGpgResult(error+output + "Successfully executed \"" + prefGpgConfCommand + " " + command + "\"", "",
                            gpgConnectAgentProcess.exitValue());

                }
                catch (IOException ex)
                {
                    handleGpgResult(ex.toString() + LINE_SEP + error, "",1);
                }
                catch (InterruptedException ex)
                {
                    handleGpgResult(ex.toString()+ LINE_SEP + error, "", 2);
                }
                finally
                {
                    if (stillActive(gpgConnectAgentProcess))
                    {
                        gpgConnectAgentProcess.destroy();
                    }
                }
            }
        });

        try
        {
            t.start();
        }
        catch (Exception ex)
        {
            handleGpgResult("Internal error: " + ex.toString(), "", 5);
        }
    }



}
