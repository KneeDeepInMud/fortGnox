package org.mockenhaupt.fortgnox.misc;

import java.awt.event.KeyEvent;
import java.awt.event.WindowStateListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PasswordCharacterPool
{
    private final List<Character> digits = new ArrayList<>();
    private final List<Character> uppercase = new ArrayList<>();
    private final List<Character> lowercase = new ArrayList<>();
    private final List<Character> special = new ArrayList<>();

    static PasswordCharacterPool instance;

    public static PasswordCharacterPool get ()
    {
        if (instance == null)
        {
            instance = new PasswordCharacterPool();
        }
        return instance;
    }

    public PasswordCharacterPool ()
    {
        initCharacterPools();
    }

    public static String getDigits ()
    {
        return charlistToString(get().digits);
    }

    public static String getUppercase ()
    {
        return charlistToString(get().uppercase);
    }

    public static String getLowercase ()
    {
        return charlistToString(get().lowercase);
    }

    public static String getSpecial ()
    {
        return charlistToString(get().special);
    }

    private static String charlistToString (List<Character> charList)
    {
        return String.join("",
                charList.stream().map(character -> new String(String.valueOf(character))).collect(Collectors.toList()));
    }


    private void initCharacterPools ()
    {
        Set<Character> skip = new HashSet<Character>(Arrays.asList(new Character[]{'"', '\'', '\\'}));
        for (char i = 33; i <= 126; ++i)
        {
            if (skip.contains(i)) continue;
            if (isPrintableChar(i))
            {
                if (Character.isDigit(i))
                {
                    digits.add(i);
                }
                else if (Character.isAlphabetic(i))
                {
                    if (Character.isUpperCase(i))
                        uppercase.add(i);
                    if (Character.isLowerCase(i))
                        lowercase.add(i);
                }
                else
                {
                    special.add(i);
                }
            }
        }
    }

    public boolean isPrintableChar (char c)
    {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                c != KeyEvent.CHAR_UNDEFINED &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

}
