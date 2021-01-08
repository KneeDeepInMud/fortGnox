package org.mockenhaupt.fortgnox;

import java.nio.file.WatchEvent;

public interface IDirectoryWatcherHandler
{
    void handleDirContentChanged (String directory, String entry, WatchEvent.Kind<?> kind);
}
