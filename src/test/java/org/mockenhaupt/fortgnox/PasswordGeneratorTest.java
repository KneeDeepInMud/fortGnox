package org.mockenhaupt.fortgnox;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

class PasswordGeneratorTest
{

    @Test
    void generatePassword ()
    {
        PasswordGenerator pg = new PasswordGenerator(password ->
        {
        });

        AtomicReference<String> pass = new AtomicReference<>("");
        for (int i =0; i < 20; ++i)
        {
            pg.generatePassword(40, true, true, true, true, s ->
            {
            }, p -> pass.set(p));
            System.err.println(pass.get());
        }
    }
}
