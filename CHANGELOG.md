26.3 Support, Major changes & fixes, early Ataraxia support, prepare for full release

* Added early support for AlixAtaraxia
* Added 26.3 support
* Added ClockSkew fingerprinting (when using Ataraxia)
* Fixed 2FA setup and recovery security issues
* Fixed token persistence, caching, and database synchronization
* Added email verification support (to Spigot)
* Added panic mode commands and persistent enable/disable controls
* Fixed panic mode activation and deactivation timing issues
* Fixed firewall enforcement and connection rate limiting
* Improved anti-bot detection using SYN fingerprints and MTU analysis
* Updated PacketEvents
* Fixed startup crashes on newer Paper versions
* Fixed Geyser/Floodgate compatibility with newer Geyser versions
* Fixed email verification vulnerabilities and added request timeouts
* Fixed multiple authentication bypasses and brute-force protection issues
* Fixed account recovery, email verification, and 2FA attempt limits
* Fixed database persistence issues with login settings
* Improved GDPR account deletion and data cleanup
* Fixed several configuration-related crashes and validation issues
* Fixed various concurrency issues and potential memory leaks
* Improved configuration security and file permissions
* Fixed message localization and translation handling
* Removed unused code and improved error handling
* Fixed various command permission and validation issues
* Added email verification commands
* Added panic mode and player help commands
* Fixed unauthorized access vulnerabilities in admin commands
* Fixed server shutdown crashes caused by stale connections
* Fixed account recovery commands being blocked during login GUIs
* Fixed excessive resource usage in captcha and connection handling