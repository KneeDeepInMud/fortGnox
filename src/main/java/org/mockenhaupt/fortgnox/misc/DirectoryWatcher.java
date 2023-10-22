package org.mockenhaupt.fortgnox.misc;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.mockenhaupt.fortgnox.DebugWindow;
import org.mockenhaupt.fortgnox.FgPreferences;

import java.io.File;
import java.util.Arrays;

import static org.mockenhaupt.fortgnox.DebugWindow.Category.DIR;

public class DirectoryWatcher
{
    IDirectoryWatcherHandler handler;
    private String directory;
    private FileAlterationMonitor monitor = null;

    public DirectoryWatcher(IDirectoryWatcherHandler handler)
    {
        this.handler = handler;
    }

    public void init(String directory_)
    {
        int interval = FgPreferences.get().get(FgPreferences.PREF_DIRECTORY_OBSERVER_INTERVAL, -1);
        if (interval <= 0) {
            return;
        }
        interval = Math.max(5000, interval);

        this.directory = directory_;
        File monitoredDirectory = new File(directory_);
        FileAlterationObserver observer = new FileAlterationObserver(monitoredDirectory);
        observer.addListener(getFileAlterationListener());
        try
        {
            monitor = new FileAlterationMonitor(interval, observer);
            monitor.start();
        }
        catch (Exception e)
        {
            handleListenerException(e);
        }
    }

    private void handleListenerException(Exception e) throws RuntimeException
    {
        dbg("Error in init: " + e.toString());
        Arrays.stream(e.getStackTrace()).sequential().forEach(stackTraceElement -> dbg(stackTraceElement.toString()));
        e.printStackTrace();
        throw new RuntimeException(e);
    }

    private FileAlterationListenerAdaptor getFileAlterationListener()
    {
        return new FileAlterationListenerAdaptor()
        {
            @Override
            public void onFileChange(File file)
            {
                handler.handleDirContentChanged(directory, file.getName(), IDirectoryWatcherHandler.ChangeEvent.FILE_CHANGE);
            }

            @Override
            public void onFileCreate(File file)
            {
                handler.handleDirContentChanged(directory, file.getName(), IDirectoryWatcherHandler.ChangeEvent.FILE_NEW);
            }

            @Override
            public void onFileDelete(File file)
            {
                handler.handleDirContentChanged(directory, file.getName(), IDirectoryWatcherHandler.ChangeEvent.FILE_DELETE);
            }
        };
    }


    public void stop()
    {
        dbg("Stopping watcher thread " + directory);
        if (monitor != null)
        {
            try
            {
                monitor.stop();
            }
            catch (Exception e)
            {
                handleListenerException(e);
            }
        }
    }

    private void dbg(String text)
    {
        DebugWindow.get().debug(DIR, text + " (" + directory + ")");
    }
}
