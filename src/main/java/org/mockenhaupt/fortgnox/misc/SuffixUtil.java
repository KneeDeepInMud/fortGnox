package org.mockenhaupt.fortgnox.misc;

import org.mockenhaupt.fortgnox.FgPreferences;

public final class SuffixUtil {
    private SuffixUtil() {}

    public static String getGpgSuffix() {
        return FgPreferences.get().getBoolean(FgPreferences.PREF_GPG_USE_ASCII) ? ".asc" : ".gpg";
    }
}
