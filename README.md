# vetsource-autoprovision
A portable program designed to install preset applications, back up user data, and clean up afterwards.

## Current features:
### Application management

* Maintain a list of configured applications
* Automatically detect new installers for configuration
* Install all/selected configured applications asynchronously and without human interaction

## Currently planned features:

### Application management

* Automatic detection of quiet arguments for silent installs
* Replace already-configured application installers with newer/updated versions without reconfiguring the entire application
* For applications with dependencies, only install after the dependencies are installed

### File backup

* configure backup to a local/networked/external drive
* Possible compression of backed-up data?
* Ability to exclude certain file types/folders from backup
* Ability to exclude files above a certain size

### OS Configuration?

* Join a domain
* Add networked drives
* Keep a list of networked printers/add those printers
