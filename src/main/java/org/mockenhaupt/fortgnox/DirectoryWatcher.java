package org.mockenhaupt.fortgnox;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.mockenhaupt.fortgnox.DebugWindow.Category.DIR;

public class DirectoryWatcher
{
    public DirectoryWatcher (IDirectoryWatcherHandler handler)
    {
        this.handler = handler;
    }

    IDirectoryWatcherHandler handler;
    AtomicBoolean active = new AtomicBoolean(true);
    private Thread thread;
    private String directory;

    public void init (String directory)
    {
        this.directory = directory;
        try
        {
            Path dir = new File(directory).toPath();

            WatchService watcher = FileSystems.getDefault().newWatchService();
            dir.register(watcher,
                    ENTRY_CREATE,
                    ENTRY_DELETE);
            startWatcherThread(watcher);
        }
        catch (Exception e)
        {
            dbg("Error in init: " + e.toString());
            Arrays.stream(e.getStackTrace()).sequential().forEach(stackTraceElement -> dbg(stackTraceElement.toString()));
            e.printStackTrace();
        }
    }

    public void stop ()
    {
        dbg("Stopping watcher thread " + directory);
        if (thread != null)
        {
            thread.interrupt();
            active.set(false);
        }
    }

    private void dbg (String text)
    {
        DebugWindow.get().debug(DIR, text + " (" + directory + ")");
    }


    private void startWatcherThread (WatchService watcher)
    {

        thread = new Thread(() ->
        {
            dbg("watcher thread: Starting thread loop");
            while (active.get())
            {
                try
                {
                    WatchKey key = watcher.take();
                    for (WatchEvent<?> event : key.pollEvents())
                    {
                        WatchEvent.Kind<?> kind = event.kind();
                        dbg("watcher thread: " + kind.name() + " " + event.context());
                        if (kind == OVERFLOW)
                        {
                            continue;
                        }

                        if (handler != null)
                        {
                            handler.handleDirContentChanged(directory, event.context().toString(), kind);
                        }
                        boolean valid = key.reset();
                        if (!valid)
                        {
                            break;
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    dbg("watcher thread interrupted, stopping");
                    active.set(false);
                }
            }
            dbg("regularly terminated watcher ");

        }, "DIR-" + directory);

        dbg("Starting watcher thread " + thread);
        thread.start();
    }

}
