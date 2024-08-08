# AlixSystem

This is the source code of the spigot antibot & authentication plugin called AlixSystem, to which link can be found here: https://www.spigotmc.org/resources/alixsystem.109144/

This documentation refers to the latest 3.2.0 (DEV-3 Hotfix) build

## FireWall
Due to different environments, Alix cannot always use the very best firewall methods there are. For that reason, currently 4 different firewall types exist within Alix.
Starting from the least optimized:

### Netty
This type uses netty's API methods to close a connection as soon as it reaches the event loop. Due to API methods being for generalized use and the delayed nature of the event loop, this firewall type is only used whenever the faster alternative for NIO or Epoll (more info below) could not have been used. If you ever get the message that the faster implementation cannot be used, it's best to contact the developer.

### Internal NIO Interceptor
This type injects itself into the internals of the accepting socket. Used if the server uses the default Java NIO for netty transport.

### Fast Unsafe Epoll

Currently the most optimized non-OS solution Alix can offer. Used on Linux machines whenever Epoll is used for netty transport. This firewall type transforms the bytecode of the server socket object. Right after accepting the connection, it can be efficiently closed without much overhead. This method also possesses a fast ipv4 look-up algorithm, which further optimizes it's performance.
The "Unsafe" in the name refers to the operations used in order to achieve this performance:
Dynamic agent loading - used for the bytecode transformation, will have greater restrictions in later java releases
Unsafe - a low-level (at least in java) memory manipulation class, scheduled for removal in later java versions

### OS IpSet

The only "true" firewall option here, and the absolutely most optimized one. All of the other options need to first accept a connection and can only then close it, due to the given restrictions, which is, to say the least, unideal.
This firewall option works by having a seperate java process with root perms access the iptables on a Linux machine. This way the connection can be truly rejected, without ever being accepted in the first place.
However, this firewall type has not yet been completed, and isn't currently available.


#### Special thanks to:

- xDark - for writing the vast majority of the injection code used in Fast Unsafe Epoll FireWall
- geolykt - for providing the incredible [ConcurrentInt62Set](https://github.com/stianloader/stianloader-concurrent/blob/main/src/main/java/org/stianloader/concurrent/ConcurrentInt62Set.java) data structure currently used for fast IPv4 look-ups in the Fast Unsafe Epoll FireWall
- mE-Shuggah, Jan & jumanji144 - for additional help in creating the Fast Unsafe Epoll FireWall
