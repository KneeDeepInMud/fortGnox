package org.mockenhaupt.jgpg;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.mockenhaupt.jgpg.DebugWindow.Category.DIR;

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
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stop ()
    {
        DebugWindow.get().debug(DIR, "Stopping watcher thread " + directory);
        if (thread != null)
        {
            thread.interrupt();
            active.set(false);
        }
    }


    private void startWatcherThread (WatchService watcher)
    {
        thread = new Thread(() ->
        {
            while (active.get())
            {
                try
                {
                    WatchKey key = watcher.take();
                    for (WatchEvent<?> event : key.pollEvents())
                    {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW)
                        {
                            continue;
                        }

                        DebugWindow.get().debug(DIR, kind.name() + " " + event.context() + " in " + directory);
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
                    active.set(false);
                }
            }
            DebugWindow.get().debug(DIR, "terminated watcher = " + watcher);

        }, "DIR-" + directory);

        DebugWindow.get().debug(DIR, "Starting watcher thread " + thread);
        thread.start();
    }

}
