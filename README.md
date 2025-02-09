
# fortGnox  ![Logo](https://raw.githubusercontent.com/KneeDeepInMud/fortGnox/master/src/main/resources/org/mockenhaupt/fortgnox/fortGnox48.png "fortGnox Logo")

Java based password manager front-end on top of GPG




## What is **fortGnox**

__fortGnox__ is a GUI password manager written in Java. It uses [GnuPG](https://gnupg.org/) as backend for all encryption related tasks.

![Screenshot](https://raw.githubusercontent.com/KneeDeepInMud/fortGnox/master/resources/fortGnox_Screenshot.png "fortGnox screenshot")

Motivation was the long year usage of command line password manager [pass](https://www.passwordstore.org/) which can be used in friendly coexistence together with __fortGnox__ on the same password store.

Although __fortGnox__ was mainly developed under Linux, it is already used by people around the globe also under Windows (well ... maybe not the globe ... at least I know of a handful of people using it).

## Features ##
- Password management (creation / random generation).
- Strong security. Encryption is done exclusively by a standard [GnuPG](https://gnupg.org/) installation. All passwords are typically encrypted with the own public GPG (PGP) key.
- Keybordless usage of passwords through the clipboard. The passwords are flushed from clipboard after a timeout.
- Passwords are masked in the GUI. This allows usage of __fortGnox__ even if other people are around and can see your screen while using __fortGnox__.
- Integrates smoothly with [pass](https://www.passwordstore.org/)
- One Time Password Generation

## Requirements ##

- Java 8 (or later)
- [GnuPG](https://gnupg.org/)
- Some courage to face the manual configuration (for the time being).


## Basic Installation ##

### Prerequisites
- Install Java (e.g. https://jdk.java.net/archive/, https://learn.microsoft.com/de-de/java/openjdk/download)
- Install GPG
    - Windows - https://www.gpg4win.de/thanks-for-download.html
    - *ux - use distribution dependent installer (yum, apt ...)
    - If not already done, create a GPG key pair:

          gpg --generate-key

      and follow the instructions.

- Download __fortGnox__

    - Either download the latest JAR file from https://github.com/KneeDeepInMud/fortGnox/releases or download and compile the code.

    - To start  __fortGnox__ basically issue the command

          java -jar fortgnox-v1.0.5.jar

    - If JAR files are not directly started on Windows when double clicked in the Windows-explorer, create a shortcut to `java.exe`, edit the shortcut target in the shortcut properies and append the argument `-jar fortgnox-v1.0.5.jar`

    - Edit configuration in __fortGnox__
        - After first start open the `Settings` dialog and select `GPG` tab.
        - Enter the path to the folder where you want to store the encrypted passwords files in field `Data directories`
        - Enter **your own** public GPG key in field `Default recipient`. The public keys can be listed via command:

              gpg -k --keyid-format short


# One Time Passwords

Password files can contain secrets for one time password in the form:
    
     otpauth://totp/Fort%20Gnox%20Test%20Key?issuer=FortGnoxTest&\
     algorithm=SHA1&digits=6&period=30&secret=ABCDABCDABCDABCD

These will be parsed and an OTP token displayed. The secret will be masked.
To add keys from Google Authenticator, the exported keys as QR code have to be
converted/extracted because these are stored in a non published Google format.
This can be done using:

 - https://github.com/dim13/otpauth

To convert OTP secrets from exported QR Image from Google Authenticator, following
command can be used (Linux):

     otpauth -link $(java -jar fortgnox-v1.1.0.jar -q QR_GoogleAuthenticatorAllTokens.jpg)
