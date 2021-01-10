
# fortGnox
![Logo](https://raw.githubusercontent.com/KneeDeepInMud/fortGnox/master/src/main/resources/org/mockenhaupt/fortgnox/fortGnox48.png "fortGnox Logo") Java based password manager front-end on top of GPG




## What is **fortGnox**

__fortGnox__ is a GUI password manager written in Java. It is based on [GnuPG](https://gnupg.org/) which is used as backend for all encryption related tasks.

Motivation was the long year usage of command line password manager [pass](https://www.passwordstore.org/) which can be used in friendly coexistence together with __fortGnox__ on the same password store.

Although __fortKnox__ was mainly developed under Linux, it is already used by people around the globe also under Windows (well ... maybe not the globe ... at least I know of a handful of people using it).

## Features ##
- Password management (creation / random generation).
- Keybordless usage of passwords through the clipboard. The passwords are flushed from clipboard after a timeout.
- Passwords are masked in the GUI. This allows usage of __fortGnox__ even if other people are around.
- Integrates smoothly with [pass](https://www.passwordstore.org/)

## Requirements ##

- Java 8 (or later)
- [GnuPG](https://gnupg.org/)
- Some courage to face the manual configuration (for the time being).


## Basic Installation ##

### Prerequisites
- Install Java (e.g. https://jdk.java.net/archive/)
- Install GPG
    - Windows - https://www.gpg4win.de/thanks-for-download.html 
    - *ux - use distribution dependent installer (yum, apt ...)
    - If not already done, create a GPG key pair:

          gpg --generate-key

      and follow the instructions.


- Download __fortGnox__
  
    - Either download the latest JAR file from https://github.com/KneeDeepInMud/fortGnox/releases or download and compile the code.

    - To start  __fortGnox__ basically issue the command

          java -jar fortgnox-0.0.1.jar

    - If JAR files are not directly started on Windows when double clicked in the Windows-explorer, create a shortcut to `java.exe`, edit the shortcut target in the shortcut properies and append the argument `-jar fortgnox-0.0.1.jar`

    - Edit configuration in __fortGnox__
        - After first start open the `Settings` dialog and select `GPG` tab.
        - Enter the `GPG HOme` directory. If unknown enter command `gpg -k`, the path it is shown in the first line of the output.
        - Enter the path to the folder where you want to store the encrypted passwords files in field `Data directories`
        - In case you have multiple public GPG keys, which is usually the case enter **your own** public key in field `Default recipient`. The public keys can be listed via command:
               
              gpg -k --keyid-format short


