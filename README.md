# webMethods ART adapter connection update

The job of the JahnTech ART connection update (JTACU) 
is to change settings of adapter connections, like a the hostname
of the database server, credentials, etc.

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


## Installation

Unzip the release ZIP file into a directory of your choice.
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

On systems where a more elaborate configuration exists, those
specifics are taken into account. Therefore the following environment
variables are used:

- `WEBMETHODS_HOME`: Installation location of the webMethods Suite.
  Must be specified, if the installation was performed to a non-default
  location.
- `JAVA_HOME`:
  - On Linux only and if it is not set, the script uses the contents of
    `/etc/profile.d/jdk.sh` if it exists.
  - If not defined, the JVM that comes with the webMethods Suite will
    be used (depends on name of JVM folder and may therefore not work on all
    versions of the webMethods Suite).

## Parameters

The behavior is controlled by command line parameters. The syntax is

```bash
./webm-is-art-connection-update.{sh|bat} <DIRECTORY_WITH_CONNECTION_NODE_NDF> \
                                         <CONNECTION_NAMESPACE> \
                                         <PROPERTY_FILE_WITH_CHANGES>
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
