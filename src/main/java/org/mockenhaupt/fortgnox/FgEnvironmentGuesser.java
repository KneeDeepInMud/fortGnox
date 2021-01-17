package org.mockenhaupt.fortgnox;

import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class FgEnvironmentGuesser
{
    // used for command execution only
    FgGPGProcess fgGPGProcess;

    public FgEnvironmentGuesser ()
    {
        fgGPGProcess = new FgGPGProcess();
    }

    public FgEnvironmentGuesser (FgGPGProcess fgGPGProcess)
    {
        this.fgGPGProcess = fgGPGProcess;
    }

    AtomicReference<String> atomicReferenceGpgHome = new AtomicReference<>(null);

    public String getGpgHome (String gpgExeLocationlong, long timeout)
    {
        Thread thread = new Thread(() ->
        {
            AtomicBoolean terminated = new AtomicBoolean(false);
            Thread commandThread = fgGPGProcess.command(gpgExeLocationlong + " -k", null, this, (out, err, filename, clientData, exitCode) ->
            {
                Scanner scanner = new Scanner(out);
                String lookFor = "pubring.gpg";
                while (scanner.hasNext())
                {
                    String line = scanner.nextLine();
                    if (line.toLowerCase().endsWith(lookFor))
                    {
                        int begin = line.length() - lookFor.length();
                        atomicReferenceGpgHome.set(line.substring(0, begin - 1));
                        break;
                    }
                }
                terminated.set(true);
            });

            try
            {
                while (!terminated.get() && commandThread.isAlive())
                {
                    Thread.sleep(10);
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

        });

        thread.start();
        try
        {
            thread.join(timeout);
        }
        catch (InterruptedException e)
        {
            // ignore any error here
        }
        return atomicReferenceGpgHome.get();
    }
}
