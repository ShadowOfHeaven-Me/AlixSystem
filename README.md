# AlixSystem

This is the source code of the spigot/velocity antibot & authentication plugin called AlixSystem, the link to which can be found on:
* [spigot](https://www.spigotmc.org/resources/alixsystem.109144/)
* [modrinth](https://modrinth.com/plugin/alixsystem)
* [builtbybit](https://builtbybit.com/resources/alixvelocity.61304/)

This documentation refers to 4.0.0 spigot & 2.0.0 velocity

## How it works

(This explanation assumes the default config, as of 4.0.0 Spigot or 2.0.0 Velocity)

Before the connection begins, Alix analyzes the general state of the server as a whole, the connecting ip's recent behaviour and it's subnet's behaviour. Based on those rules, as well as TCP connection hints, such as the inferred MTU & OS it applies penalties, which can result in either rate limits or full firewalls. Alix also analyzes the suspicion of the given ip based on Open Source Cyber Threat Intelligence data, be it of the given ip or some of it's parameters. OS inference is done via modernized [p0f](https://github.com/p0f/p0f) checks, while MTU guesses are hand-rolled (so they might not be fully correct, but it's marginally acceptable).
When a player connects, Alix will hijack their connection and perform many pre-login checks, including bot attack checks, packet sanity checks, pre-register checks defined in the config file & . If an AntiBot check fails, the connecting ip will be automatically firewalled (for FireWall impl details, see the section below). If the player is unverified, he'll be sent to a simulated virtual limbo server (he'll be under the illusion of connecting to an actual server).  
It will then use mainly [Sonar](https://github.com/jonesdevelopment/sonar/tree/main) checks on a fake [NanoLimbo](https://github.com/BoomEaro/NanoLimbo) server (altough rewritten into an abstract packet lib, and thoroughly optimized) to determine whether the connected user possesses a valid (i.e. non-botted) minecraft instance (will kick on fail, without firewalling due to possible conflicts with some clients and occassional fails). The entire process usually lasts ~0.6s, and is fully automatic.  
If all is well, the player will be either automatically transferred onto the actual server (1.20.5+) or will be kicked and asked to rejoin (since retransmitting join packets could result in conflicts).  
Alix uses [PacketEvents](https://github.com/retrooper/packetevents) for it's packet decoding & encoding, for the most part. It does not necessarily always use the API the "correct" way, and may instead just use the impl as an abstract packet library (so not tied to any server impl, and not reliant on the server de/encoding pipeline).

## FireWall
Due to different environments, Alix cannot always use the very best firewall methods there are. For that reason, currently 3 different firewall types exist within Alix.
Starting from the least optimized:

### Netty
This type uses netty's API methods to close a connection as soon as it reaches the event loop. Due to API methods being for generalized use and the delayed nature of the event loop, this firewall type is only used whenever the faster alternative for NIO or Epoll (more info below) could not have been used. If you ever get the message that the faster implementation cannot be used, it's best to contact the developer.

### Fast Unsafe Epoll

Currently the most optimized non-OS solution Alix can offer. Used on Linux machines whenever Epoll is used for netty transport. This firewall type transforms the bytecode of the server socket object. Right after accepting the connection, it can be efficiently closed without much overhead. This implementation is (or at least should be, depending on whatever the C netty code impl does) zero-garbage. This method also possesses a fast ipv4 look-up table, which further optimizes it's performance. For ipv6 a fast threadlocal cache is used instead.
The "Unsafe" in the name refers to the operations used in order to achieve this performance:  
Dynamic agent loading - used for the bytecode transformation, will have greater restrictions in later java releases  
Unsafe - a low-level (at least in java) memory manipulation class, scheduled for removal in later java versions  

### Ataraxia

The only "true" firewall option here, and the absolutely most optimized one. All of the other options need to first accept a connection and can only then close it, due to the given restrictions, which is, to say the least, unideal.
[Alix Ataraxia](https://github.com/ShadowOfHeaven-Me/AlixAtaraxia) is an eBPF (Layer 4, kernel component) available on modern linux versions. It was written in Rust, using Aya. It hasn't yet reached full safety for general use, so it isn't officially available yet. Requires root privileges to run.


#### Special thanks to:

- xDark - for writing the vast majority of the injection code used in Fast Unsafe Epoll FireWall
- geolykt - for providing the incredible [ConcurrentInt62Set](https://github.com/stianloader/stianloader-concurrent/blob/main/src/main/java/org/stianloader/concurrent/ConcurrentInt62Set.java) data structure currently used for fast IPv4 look-ups in the Fast Unsafe Epoll FireWall
- mE-Shuggah, Jan & jumanji144 - for additional help in creating the Fast Unsafe Epoll FireWall
