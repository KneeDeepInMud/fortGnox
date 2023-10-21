package org.mockenhaupt.fortgnox.misc;

import java.nio.file.WatchEvent;

public interface IDirectoryWatcherHandler
{
    public enum ChangeEvent {
        FILE_NEW,
        FILE_DELETE,
        FILE_CHANGE
    }
    void handleDirContentChanged (String directory, String entry, ChangeEvent kind);
}
