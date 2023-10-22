/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mockenhaupt.fortgnox;


import org.mockenhaupt.fortgnox.misc.DirectoryWatcher;
import org.mockenhaupt.fortgnox.misc.FileUtils;
import org.mockenhaupt.fortgnox.misc.IDirectoryWatcherHandler;
import org.mockenhaupt.fortgnox.tags.TagsFile;
import org.mockenhaupt.fortgnox.tags.TagsStore;

import javax.swing.SwingUtilities;
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
import java.nio.file.WatchEvent;
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
import java.util.stream.Collectors;

import static org.mockenhaupt.fortgnox.FgPreferences.PREF_CHARSET;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPGCONF_COMMAND;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_COMMAND;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_HOMEDIR;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_GPG_USE_ASCII;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_SECRETDIRS;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_SECRETDIR_SORTING;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_USE_GPG_AGENT;
import static org.mockenhaupt.fortgnox.FgPreferences.PREF_USE_PASS_DIALOG;

/**
 * @author fmoc
 */
public class FgGPGProcess implements PropertyChangeListener, IDirectoryWatcherHandler
{
    private final static Logger LOGGER = Logger.getLogger(FgGPGProcess.class.getName());

    public static final String LINE_SEP = System.lineSeparator();
    private String prefCharset = "ISO-8859-15";
    private Charset charset;
    private String[] secretList;
    private String prefGpgConfCommand = "gpgconf";
    private String prefGpgExeLocation = "gpg";
    private String prefGpgHome = "";
    private String prefSecretDirsString = "";
    protected List<String> secretdirs = new ArrayList<>();
    protected Map<String, DirectoryWatcher> directoryWatchers = new HashMap<>();
    private final HashSet<SecretListListener> secretListeners = new HashSet<>();
    private final HashSet<ResultListListener> resultListeners = new HashSet<>();
    private final HashSet<EncrypionListener> encryptListeners = new HashSet<>();
    private final HashSet<CommandListener> commandListeners = new HashSet<>();

    private static String lastClipText;

    private boolean prefUsePasswordDialog = false;
    private boolean prefConnectToGpgAgent = true;
    private final Set<String> skipExtensions = new HashSet<>(Arrays.asList("tgz",
            "jpg",
            "gif",
            "doc",
            "docx",
            "p12",
            "xls",
            "xlsx",
            "zip"));


    @Override
    public void propertyChange (PropertyChangeEvent propertyChangeEvent)
    {
        LOGGER.fine("propertyChangeEvent = " + propertyChangeEvent);
        switch (propertyChangeEvent.getPropertyName())
        {
            case PREF_GPG_HOMEDIR:

                prefGpgHome = (String) propertyChangeEvent.getNewValue();
                break;
            case PREF_GPGCONF_COMMAND:
                prefGpgConfCommand = (String) propertyChangeEvent.getNewValue();
                break;

            case PREF_GPG_COMMAND:
                prefGpgExeLocation = (String) propertyChangeEvent.getNewValue();
                break;

            case PREF_CHARSET:
                prefCharset = (String) propertyChangeEvent.getNewValue();
                charset = Charset.forName(prefCharset);
                break;

            case PREF_SECRETDIRS:
                prefSecretDirsString = (String) propertyChangeEvent.getNewValue();
                handleSecretPreferenceChanged();
                break;

            case PREF_USE_PASS_DIALOG:
                prefUsePasswordDialog = (Boolean) propertyChangeEvent.getNewValue();
                break;

            case PREF_USE_GPG_AGENT:
                prefConnectToGpgAgent = (Boolean) propertyChangeEvent.getNewValue();
                break;
            case PREF_SECRETDIR_SORTING:
                rebuildSecretList();
                break;
        }
    }

    public interface SecretListListener
    {
        void handleSecretList (String[] list);
    }

    public interface ResultListListener
    {
        void handleGpgResult (String out, String err);

        void handleGpgResult (String out, String err, String fileName);

        void handleGpgResult (String out, String err, String filename, Object clientData);
    }

    public interface EncrypionListener
    {
        void handleGpgEncryptResult (String out, String err, String filename, Object clientData);
    }

    public interface CommandListener
    {
        void handleGpgCommandResult (String out, String err, String filename, Object clientData, int exitCode);
    }

    abstract static class GpgRunnable implements Runnable
    {
        private boolean toClipboard;
        private String password;
        private String filename;
        private String command;
        private String content;
        private Object clientData;

        public GpgRunnable (boolean toClipboard, String password, String filename, Object clientData)
        {
            this.toClipboard = toClipboard;
            this.password = password;
            this.filename = filename;
            this.clientData = clientData;
        }

        public GpgRunnable (String command)
        {
            this.command = command;
        }

        public GpgRunnable (String command, File file, Object clientData)
        {
            if (file != null)
            {
                this.filename = file.getAbsolutePath();
            }
            this.command = command;
            this.clientData = clientData;
        }

        public GpgRunnable (String command, Object clientData)
        {
            this.command = command;
            this.clientData = clientData;
        }

        public GpgRunnable (String filename, String content, Object clientData)
        {
            this.filename = filename;
            this.content = content;
            this.clientData = clientData;
        }

        public String getCommand ()
        {
            return command;
        }

        public List<String> getCommandList ()
        {
            return Arrays.asList(command.split("\\s"));
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

        public Object getClientData ()
        {
            return clientData;
        }

        public void setClientData (Object clientData)
        {
            this.clientData = clientData;
        }

        public String getContent ()
        {
            return content;
        }

        public void setContent (String content)
        {
            this.content = content;
        }
    }

    public static void clearClipboardIfNotChanged ()
    {
        String currentClip = null;
        try
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            currentClip = (String) clipboard.getData(DataFlavor.stringFlavor);
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
            clip("#-= FLUSHED PASSWORD FROM fortGnox =-#");
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


    public void decrypt (String fname, String passphrase, boolean _toClipboard, int clipSeconds, Object clientData)
    {
        Thread t = new Thread(new GpgRunnable(_toClipboard, passphrase, fname, clientData)
        {
            public void run ()
            {
                String output = "";
                String error = "";
                String clipboardText = null;
                int lines = 0;

                List<String> cmdArgList = new ArrayList<>();
                cmdArgList.addAll(Arrays.asList(prefGpgExeLocation.split("\\s")));
                cmdArgList.add("--batch");
                if (passphrase != null && !passphrase.isEmpty())
                {
                    cmdArgList.add("--passphrase-fd");
                    cmdArgList.add("0");
                }
                cmdArgList.add("--yes");
                if (prefGpgHome != null && !prefGpgHome.isEmpty())
                {
                    cmdArgList.add("--homedir");
                    cmdArgList.add(prefGpgHome);
                }
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
                        handleGpgResult(this.getFilename() + LINE_SEP + error, "Error occurred copying text to clipboard", 1);
                    }
                    else
                    {
                        clip(clipboardText);
                        handleGpgResult("Copied first line of " + this.getFilename() +
                                "password to clipboard for " + clipSeconds + " seconds", "", 0, getClientData());
                    }
                }
                else
                {
                    String linesText = LINE_SEP + lines + " lines";
                    linesText = ""; // TODO: make this configurable

                    handleGpgResult(this.getFilename() + LINE_SEP + error + linesText, output, this.getFilename(), exitValue, getClientData());
                }
            }
        });

        try
        {
            t.start();
        }
        catch (Exception ex)
        {
            handleGpgResult(ex.getMessage(), "Internal error occurred starting decryption thread", 333, null);
        }
    }


    public void encrypt (String fname, String content, String recipient, Object clientData)
    {
        Thread t = new Thread(new GpgRunnable(fname, content, clientData)
        {
            public void run ()
            {
                String output = "";
                String error = "";
                String clipboardText = null;
                int lines = 0;

                List<String> cmdArgList = new ArrayList<>();
                cmdArgList.addAll(Arrays.asList(prefGpgExeLocation.split("\\s")));
                cmdArgList.add("--batch");
                cmdArgList.add("--yes");
                if (FgPreferences.get().getBoolean(PREF_GPG_USE_ASCII))
                {
                    cmdArgList.add("--armor");
                }
                if (prefGpgHome != null && !prefGpgHome.isEmpty())
                {
                    cmdArgList.add("--homedir");
                    cmdArgList.add(prefGpgHome);
                }
                cmdArgList.add("--encrypt");
                cmdArgList.add("--recipient");
                cmdArgList.add(recipient);
                cmdArgList.add("--output");

                File file = new File(this.getFilename());
                cmdArgList.add(file.getAbsolutePath());

                DebugWindow.get().debug(DebugWindow.Category.GPG, cmdArgList.toString());
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
                    notifyEncryptionListeners("", e.getMessage() + " ERROR staring encryption process", fname, clientData);
                    return;
                }

                final BufferedReader or = new BufferedReader(
                        new InputStreamReader(gpgProcess.getInputStream(), charset));
                final BufferedReader er = new BufferedReader(
                        new InputStreamReader(gpgProcess.getErrorStream(), charset));

                String content = this.getContent();
                if (content != null && !content.isEmpty())
                {
                    final BufferedWriter os = new BufferedWriter(
                            new OutputStreamWriter(gpgProcess.getOutputStream()));

                    content += LINE_SEP;

                    try
                    {
                        os.write(content);
                        os.flush();
                        os.close();
                        Thread.sleep(200);
                    }
                    catch (IOException e)
                    {
                        notifyEncryptionListeners("", e.getMessage() + " ERROR writing content for encryption", fname, clientData);
                        return;
                    }
                    catch (InterruptedException e)
                    {
                        notifyEncryptionListeners("", e.getMessage() + " ERROR writing password", fname, clientData);
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
                            notifyEncryptionListeners(output, error, fname, clientData);
                            break;
                        }
                    }

                    exitValue = gpgProcess.exitValue();
                }
                catch (IOException ex)
                {
                    notifyEncryptionListeners("", ex.toString(), fname, clientData);
                }
                catch (IllegalThreadStateException ex)
                {
                    // nope
                }
                finally
                {
                    if (stillActive(gpgProcess))
                    {
                        gpgProcess.destroy();
                    }
                }

                if (exitValue != 0)
                {
                    error = "failure during encryption command, " + error;
                }
                notifyEncryptionListeners("Encrypted" + output, error, getFilename(), getClientData());
            }
        });

        try
        {
            t.start();
        }
        catch (Exception ex)
        {
            notifyEncryptionListeners("", ex.toString(), fname, clientData);
        }
    }


    public void command (String command, String fname, Object clientData)
    {
        command(command, fname, clientData, (out, err, filename, clientData1, exitCode) -> {
             notifyCommandListeners(out, err, filename, clientData1, exitCode);
        });
    }

    public void command (String command, String fname, Object clientData, CommandListener commandListener)
    {
        File fileArgument;
        if (fname == null || fname.isEmpty())
        {
            fileArgument = null;
        }
        else
        {
            fileArgument = new File(fname);
        }
        Thread t = new Thread(new GpgRunnable(command, fileArgument, clientData)
        {
            public void run ()
            {
                String output = "";
                String error = "";
                String clipboardText = null;
                int lines = 0;

                List<String> cmdArgList = new ArrayList<>();
                cmdArgList.addAll(getCommandList());
                if (getFilename() != null && !getFilename().isEmpty())
                {
                    cmdArgList.add(getFilename());
                }

                DebugWindow.get().debug(DebugWindow.Category.GPG, cmdArgList.toString());
                String[] cmds = cmdArgList.toArray(new String[]{});

                int exitValue = 1;


                Process gpgProcess;
                try
                {
                    gpgProcess = Runtime.getRuntime().exec(cmds);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    commandListener.handleGpgCommandResult("", e.toString(), getFilename(), getClientData(), 1);
                    return;
                }

                final BufferedReader or = new BufferedReader(
                        new InputStreamReader(gpgProcess.getInputStream(), charset));
                final BufferedReader er = new BufferedReader(
                        new InputStreamReader(gpgProcess.getErrorStream(), charset));

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
                            commandListener.handleGpgCommandResult(output, error, fname, clientData, 1);
                            break;
                        }
                    }

                    exitValue = gpgProcess.exitValue();
                }
                catch (IOException ex)
                {
                    commandListener.handleGpgCommandResult("", ex.toString(), fname, clientData, exitValue);
                }
                catch (IllegalThreadStateException ex)
                {
                    // nope
                }
                finally
                {
                    if (stillActive(gpgProcess))
                    {
                        gpgProcess.destroy();
                    }
                }

                if (exitValue != 0)
                {
                    error = "failure in post command, " + error;
                }
                commandListener.handleGpgCommandResult(output, error, getFilename(), getClientData(), exitValue);
            }
        });

        try
        {
            t.start();
        }
        catch (Exception ex)
        {
            commandListener.handleGpgCommandResult("", ex.toString(), fname, clientData, 1);
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
                    handleGpgResult(error + output + "Successfully executed \"" + prefGpgConfCommand + " " + command + "\"", "",
                            gpgConnectAgentProcess.exitValue());

                }
                catch (IOException ex)
                {
                    handleGpgResult(ex.toString() + LINE_SEP + error, "", 1);
                }
                catch (InterruptedException ex)
                {
                    handleGpgResult(ex.toString() + LINE_SEP + error, "", 2);
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

    void addEncryptionListener (EncrypionListener listener)
    {
        encryptListeners.add(listener);
    }

    void addCommandListener (CommandListener listener)
    {
        commandListeners.add(listener);
    }

    void addResultListener (ResultListListener listener)
    {
        resultListeners.add(listener);
        listener.handleGpgResult("", "");
    }

    private void notifySecretListeners ()
    {
        Iterator<SecretListListener> iter = secretListeners.iterator();
        while (iter.hasNext())
        {
            iter.next().handleSecretList(secretList);
        }
    }

    private void notifyResultListeners (String out, String err, String filename, Object clientData)
    {
        Iterator<ResultListListener> iter = resultListeners.iterator();
        while (iter.hasNext())
        {
            iter.next().handleGpgResult(out, err, filename, clientData);
        }
    }

    private void notifyEncryptionListeners (String out, String err, String filename, Object clientData)
    {
        Iterator<EncrypionListener> iter = encryptListeners.iterator();
        while (iter.hasNext())
        {
            iter.next().handleGpgEncryptResult(out, err, filename, clientData);
        }
    }

    private void notifyCommandListeners (String out, String err, String filename, Object clientData, int exitCode)
    {
        Iterator<CommandListener> iter = commandListeners.iterator();
        while (iter.hasNext())
        {
            iter.next().handleGpgCommandResult(out, err, filename, clientData, exitCode);
        }
    }

    private void handleSecretPreferenceChanged ()
    {
        PreferencesAccess preferences = FgPreferences.get();
        File f = new File(prefSecretDirsString);
        secretdirs.clear();
        directoryWatchers.values().stream().forEach(s -> s.stop());
        directoryWatchers.clear();

        FileUtils.ParsedDirectories parsedDirectories = FileUtils.splitDirectoryString(prefSecretDirsString);

        if (!parsedDirectories.directoryList.isEmpty())
        {
            secretdirs.addAll(parsedDirectories.directoryList.stream().sorted().collect(Collectors.toList()));
            preferences.put(PREF_SECRETDIRS, parsedDirectories.revisedList);
        }
        else
        {
            // prop is single directrory
            f = new File(prefSecretDirsString);
            preferences.put(PREF_SECRETDIRS, f.getAbsolutePath());
            secretdirs.add(f.getAbsolutePath());
        }


        secretdirs.stream().forEach(sd ->
        {
            directoryWatchers.computeIfAbsent(sd, s ->
            {
                DirectoryWatcher dw = new DirectoryWatcher(FgGPGProcess.this);
                dw.init(s);
                return dw;
            });
        });

        rebuildSecretList();
    }

    //  protected Preferences preferences;
    public FgGPGProcess ()
    {

        PreferencesAccess preferences = FgPreferences.get();
        preferences.addPropertyChangeListener(this);

        String defaultFileLocation = "gpg";
        prefGpgExeLocation = preferences.get(FgPreferences.PREF_GPG_COMMAND, defaultFileLocation);

        prefCharset = preferences.get(PREF_CHARSET, prefCharset);
        charset = Charset.forName(prefCharset);

        defaultFileLocation = "";
        prefGpgHome = preferences.get(PREF_GPG_HOMEDIR, defaultFileLocation);

        prefGpgConfCommand = preferences.get(FgPreferences.PREF_GPGCONF_COMMAND, "gpgconf");

        defaultFileLocation = "/";
        prefSecretDirsString = preferences.get(FgPreferences.PREF_SECRETDIRS, defaultFileLocation);
        handleSecretPreferenceChanged();

        this.prefUsePasswordDialog = preferences.get(FgPreferences.PREF_USE_PASS_DIALOG, prefUsePasswordDialog);
        this.prefConnectToGpgAgent = preferences.get(FgPreferences.PREF_USE_GPG_AGENT, prefConnectToGpgAgent);
    }


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
            if (info != null && !info.isEmpty() && ret != null)
            {
                ret = ret.replace(")", " - " + info + ")");
            }
        }
        else
        {
            ret = completeFileMap.get(filename);
            if (info != null && !info.isEmpty() && ret != null)
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

    private HashMap<String, String> fileMap = new HashMap<>();
    private HashMap<String, String> completeFileMap = new HashMap<>();
    private HashMap<String, String> abbrevCompleteFileMap = new HashMap<>();


    public void rebuildSecretList ()
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

            String errorState = "";
            if (fList == null)
            {
                errorState = "Invalid secret directory in preferences " + secretdirs;
                handleGpgResult(errorState, "", 1, null);
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
                String displayDir = dir + shortName;
                fileMap.put(fList[i].getAbsolutePath(), displayDir);
                folderToPathKeysMap.computeIfAbsent(dir, d -> new ArrayList<>()).add(fList[i].getAbsolutePath());
            }


            String tagsFileName = secDir + File.separator + "fgtags.yml";
            File tagsFile = new File(tagsFileName);
            if (tagsFile.exists() && !tagsFile.isDirectory()) {
                try
                {
                    TagsStore.registerTags(new TagsFile(tagsFileName));
                }
                catch (Exception ex)
                {
                    notifyResultListeners("", ex.toString(), tagsFileName, null);
                }
            }

        }

        int secretsSize = folderToPathKeysMap.entrySet().stream().mapToInt(stringListEntry -> stringListEntry.getValue().size()).sum();
        secretList = new String[secretsSize];

        AtomicInteger ix = new AtomicInteger();
        int sortReverse = FgPreferences.get().getBoolean(PREF_SECRETDIR_SORTING) ? 1 : -1;
        folderToPathKeysMap.keySet().stream().sorted(
                (s, t1) -> s.toLowerCase().compareTo(t1.toLowerCase()) * sortReverse)
                .forEach(
                        dir ->
                        {
                            folderToPathKeysMap.get(dir).stream().sorted((a, b) ->
                            {
                                return a.toLowerCase().compareTo(b.toLowerCase());
                            }).forEach(s ->
                            {
                                secretList[ix.getAndIncrement()] = s;
                            });
                        }
                );

        completeFileMap.putAll(fileMap);

        final String sepChar = Pattern.quote(File.separator);
        final Pattern bnPattern = Pattern.compile("([^" + sepChar + "]+)" + sepChar + "([^" + sepChar + "]+)$");
        completeFileMap.entrySet().forEach(stringStringEntry ->
        {
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

    private synchronized void handleGpgResult (String err,
                                               String out,
                                               int exCode)
    {
        handleGpgResult(err, out, null, exCode, null);
    }

    private synchronized void handleGpgResult (String err,
                                               String out,
                                               int exCode,
                                               Object clientData)
    {
        handleGpgResult(err, out, null, exCode, clientData);
    }


    private synchronized void handleGpgResult (String err,
                                               String out,
                                               String filename,
                                               int exCode,
                                               Object clientData)
    {
        if (exCode == 0)
        {
            notifyResultListeners(out, err, filename, clientData);
        }
        else
        {
            err = "Exitcode: " + exCode + LINE_SEP + err;
            notifyResultListeners("", err, filename, clientData);
        }
    }


    protected boolean stillActive (Process p)
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

    public List<String> getSecretdirs ()
    {
        return secretdirs;
    }

    @Override
    public void handleDirContentChanged (String directory, String entry, ChangeEvent kind)
    {
        if (entry.toLowerCase().endsWith("gpg") || entry.toLowerCase().endsWith("asc") || entry.toLowerCase().endsWith("yml"))
        {
            SwingUtilities.invokeLater(this::rebuildSecretList);
        }
    }


}
