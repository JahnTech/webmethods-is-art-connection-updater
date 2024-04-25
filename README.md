# webMethods ART adapter connection update

The job of the JahnTech ART connection update (JTACU) 
is to change settings of adapter connections, like a the hostname
of the database server, credentials, etc. This also includes
the connection pool settings. It is also possible to disable
a connection before bringing up Integration Server.

The change of the settings happens purely on disk and does not
require a running Integration Server (IS). In fact, if such a change is
performed with IS running, the affected package
must be reloaded immediately. In case of a password update with
IS running, the latter must be restarted.

Because of those effects, it is stongly discouraged to execute
JTACU with IS running at the same time. In particular it must be
avoided to change settings from the IS side after JTACU was run.
You should restart IS in that case immediately.

The primary use-case is deployment into a new environment type,
creation of a container image, or start-up of a new container
instance.

[![Watch the video](https://img.youtube.com/vi/841fooatLkI/hqdefault.jpg)](https://youtu.be/841fooatLkI)

## How It Works

The program reads the entire adapter connection from the `node.ndf`
file on disk, performs the specified changes, and writes back
the now updated connection settings. No backup is performed.

Passwords are handled in a similar fashion. They are not
stored in the `node.ndf` but the built-in password manager
of IS, which is often referred to as PassMan.
Therefore a separate instance of PassMan is initiated and the
password updated. Hence the need to restart IS after such
change.

If you dont' want to store an unencrypted password in the changes
file, you can encrypted it. Either use the

- service `wm.server.configurationvariables:encryptData` from the
  `WmRoot` package, or
- the Java class `com.webmethods.deployer.common.cipher.CipherUtil` .

Please note that the tool makes use of non-public APIs. You should
therefore test it carefully. It is also recommended to create backups
of the files that are changed. In addition to the `node.ndf` that
contains the connection details, this also means the data store files
for PassMan. You can find them at `$IS_HOME/config/txnPassStore.dat`
and `$IS_HOME/config/WORK-txnPassStore.dat` .

## Installation

Download the latest release ZIP
[from here](https://github.com/JahnTech/webmethods-is-art-connection-updater/releases)
and unzip file into a directory of your choice.
There is no requirement to use a particular directory.

## Usage

The program comes with batch/script files
to invoke the actual Java program. 

The script files' (`webm-is-art-connection-update.sh` and 
`webm-is-art-connection-update.bat`) logic is such that on a
system where the webMethods installation was performed
into the default location, no further setup work is required.

If webMethods was installed into a custom location, the environment
variable `WEBMETHODS_HOME` must be specified for the script to
work properly.

For Java the following applies, if the `JAVA_HOME` environment
variable is not defined:

- On Linux only, the script uses the contents of
  `/etc/profile.d/jdk.sh` if that exists. In that case the next
  point is skipped.
- On Windows and Linux, the JVM that comes with the webMethods Suite will
  be used.

  _Note_: In the past the name of the JVM folder has been changed. The scripts
  may therefore not work on all future versions of the webMethods Suite.
  If this situation occurs, please search for "checked for Java" in the scripts.
  The line below that comment contains all places that are checked for a
  Java installation within webMethods. You can then simply add the new one here.

## Parameters

The behavior is controlled by command line parameters. The syntax is

```bash
webm-is-art-connection-update.{sh|bat} <DIRECTORY_WITH_CONNECTION_NODE_NDF> \
                                       <CONNECTION_ALIAS> \
                                       <PROPERTY_FILE_WITH_CHANGES>
```

On Linux you may need to enable the execution bit for the script via
```bash
chmod 755 webm-is-art-connection-update.sh
```

The ZIP release archives contain a sample file for the connection settings
(`sample-change.conf`). Please make a copy and use this as a starting point.

______________________
This tool is provided as-is and without warranty or support. Users are free
to use, fork and modify it, subject to the license agreement.
While JahnTech, Inh. Christoph Jahn welcomes contributions, we cannot guarantee
to include every contribution in the master project.

Contact us at [JahnTech](mailto:info@jahntech.com?subject=Github/JTACU)
if you have any questions.
