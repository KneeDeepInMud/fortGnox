package org.mockenhaupt.fortgnox;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

class PasswordGeneratorTest
{

    enum CharClass
    {
        digit(1 << 0),
        upper(1 << 1),
        lower(1 << 2),
        special(1 << 3);

        private final int value;

        CharClass (int i)
        {
            this.value = i;
        }

        public int getValue ()
        {
            return value;
        }
    }

    PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp ()
    {
        passwordGenerator = new PasswordGenerator(password ->
        {
        });

    }

    @Test
    void generatePassword ()
    {
        int charClass = 0;
        for (int len = 20; len > 3; --len)
            for (charClass = 1; charClass <= 0xf; ++charClass)
            {
                boolean digit = (charClass & CharClass.digit.getValue()) != 0;
                boolean upper = (charClass & CharClass.upper.getValue()) != 0;
                boolean lower = (charClass & CharClass.lower.getValue()) != 0;
                boolean special = (charClass & CharClass.special.getValue()) != 0;

                AtomicReference<String> pass = new AtomicReference<>("");
                passwordGenerator.generatePassword(len, digit, upper, lower, special, s ->
                {
                }, p -> pass.set(p));

                System.out.print("charClass = " + charClass);
                System.out.print(", digit: " + (digit ? "T" : "F"));
                System.out.print(", lower: " + (upper ? "T" : "F"));
                System.out.print(", upper: " + (lower ? "T" : "F"));
                System.out.println(", special: " + (special ? "T" : "F") + " password: " + pass);

                checkPassWordComplete(pass.get(), len, digit, upper, lower, special);

            }

    }


    void checkPassWordComplete (String pass,
                                long len,
                                boolean digit,
                                boolean upper,
                                boolean lower,
                                boolean useSpecial)
    {
        Assert.assertTrue("Password " + pass + " has not length " + len, pass.length() == len);
        if (digit) checkPool(pass, passwordGenerator.getDigits());
        if (upper) checkPool(pass, passwordGenerator.getUppercase());
        if (lower) checkPool(pass, passwordGenerator.getLowercase());
        if (useSpecial) checkPool(pass, passwordGenerator.getSpecial());

    }

    private void checkPool (String pass, List<Character> characterList)
    {
        boolean found = false;
        for (Character poolChar : characterList)
        {
            Optional<Character> charFound = pass.chars().mapToObj(value -> (char) value).filter(character -> character == poolChar).findAny();
            if (charFound.isPresent())
            {
                found = true;
                break;
            }
        }
        Assert.assertTrue("Password " + pass + " has none of " + characterList.toString(), found);
    }

}
