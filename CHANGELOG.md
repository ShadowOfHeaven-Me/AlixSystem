

* Changed default password-hash-type: 3 -> 4
* Fixed alix_user_tokens still not being saved to the database
* Added back packet length validation
* Fixed one vpn check provider not working correctly
* Fixed HAProxy support not working properly
* Added the option to support MC port http traffic
* Fixed errors on newer Geyser versions
* Invalid packets now firewall for up to 30 minutes, 15s for mapped ips
* Tuned PanicModeManager

Spigot:
* Try fix excessive chunk loading in the alix world
* Fixed a possible unauthorized access vulnerability