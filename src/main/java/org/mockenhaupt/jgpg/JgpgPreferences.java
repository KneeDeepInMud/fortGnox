package org.mockenhaupt.jgpg;

public class JgpgPreferences
{
    public static final String PREFERENCE_NODE = "org.fmoc.jgpg";

    public static final String PREF_GPG_HOMEDIR = "homedir";
    public static final String PREF_GPG_COMMAND = "gpg";
    public static final String PREF_GPGCONF_COMMAND = "gpgconf";
    public static final String PREF_URL_OPEN_COMMAND = "browser_open_command";
    public static final String PREF_CHARSET = "charset";
    public static final String PREF_SECRETDIRS = "secretdir";
    public static final String PREF_IS_WINDOWS = "iswindows";
    public static final String PREF_USE_PASS_DIALOG = "passDialog";
    public static final String PREF_SHOW_PASSWORD_SHORTCUT_BAR = "passwordShortcutToolbar";
    public static final String PREF_USE_GPG_AGENT = "connectToGpgAgent";
    public static final String PREF_MASK_FIRST_LINE = "mask_first_line";
    public static final String PREF_TEXTAREA_FONT_SIZE = "textarea_font_size";

    public static final String PREF_CLIP_SECONDS = "clip_seconds";
    public static final String PREF_USE_FAVORITES = "use_favorites";
    public static final String PREF_FAVORITES = "favorites";
    public static final String PREF_RESET_MASK_BUTTON_SECONDS = "reset_mask_button_seconds";
    public static final String PREF_CLEAR_SECONDS = "clear_seconds";
    public static final String PREF_PASSWORD_SECONDS = "password_seconds";
    public static final String PREF_PASSWORD_MASK_PATTERNS = "password_mask_patterns";
    public static final String PREF_USERNAME_MASK_PATTERNS = "username_mask_patterns";
    public static final String PREF_SQUEEZE_LINES = "squeeze_lines";
    public static final String PREF_OPEN_URLS = "open_urls";


    public static PreferencesAccess get ()
    {
        return PreferencesAccess.getInstance(PREFERENCE_NODE);
    }
}
