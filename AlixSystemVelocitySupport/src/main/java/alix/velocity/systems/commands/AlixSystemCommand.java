package alix.velocity.systems.commands;

import alix.common.antibot.algorithms.any.PanicModeManager;
import alix.common.antibot.epoll.Telemetry;
import alix.common.antibot.epoll.TelemetryProfiler;
import alix.common.antibot.epoll.TelemetryProfilerImpl;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.commands.file.CommandsFileManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.file.AllowListFileManager;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumDataCache;
import alix.common.data.premium.PremiumStatus;
import alix.common.data.security.email.EmailConfig;
import alix.common.data.security.email.EmailHandler;
import alix.common.database.DatabaseUpdater;
import alix.common.login.premium.PremiumUtils;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.packets.message.MessageWrapper;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.velocity.Main;
import alix.velocity.utils.AlixUtils;
import alix.velocity.utils.file.FileManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import io.netty.channel.Channel;
import ua.nanit.limbo.connection.login.LoginState;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import static alix.velocity.utils.AlixUtils.sendMessage;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class AlixSystemCommand {

    private static final boolean devMode = false;
    private static final String passwordResetMessage = Messages.get("password-reset-forcefully");
    private static final AlixMessage playerDataNotFound = Messages.getAsObject("player-data-not-found");

    private static final SuggestionProvider<CommandSource> USERNAME_SUGGESTIONS = (context, builder) -> {
        UserFileManager.getAllData().stream()
                .map(PersistentUserData::getName)
                .forEach(builder::suggest);
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSource> LOGIN_TYPE_SUGGESTIONS = (context, builder) -> {
        Arrays.stream(LoginType.values()).map(LoginType::name).forEach(builder::suggest);
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSource> FIREWALLED_IPS_SUGGESTIONS = (context, builder) -> {
        FireWallManager.dynamicBlockedSet().forEach(ip -> builder.suggest(ip.getHostAddress()));
        return builder.buildFuture();
    };

    public static void register(CommandManager commandManager) {
        LiteralArgumentBuilder<CommandSource> root = LiteralArgumentBuilder.literal("as");
        root.requires(source -> source.hasPermission("alixsystem.admin"));

        root.then(BrigadierCommand.literalArgumentBuilder("save_all_local_to_db")
                .executes(context -> {
                    CommandSource sender = context.getSource();
                    sendMessage(sender, Messages.get("as-save-all-local-to-db"));
                    UserFileManager.saveLocalToDb();
                    return SINGLE_SUCCESS;
                })
        );

        // v1.5.2: re-reads config.yml/database.yml/gui-menus/*.yml/langs/commands.txt/allow-list.txt
        // from disk without restarting the whole proxy - see FileManager#reloadFiles() for exactly what
        // this does and does not cover (some settings, e.g. email-config.yml or command aliases
        // themselves, genuinely still need a real restart - that's stated to the sender below too, so
        // it's never a surprise).
        root.then(BrigadierCommand.literalArgumentBuilder("reload")
                .executes(context -> {
                    CommandSource sender = context.getSource();
                    long start = System.currentTimeMillis();

                    try {
                        FileManager.reloadFiles();
                    } catch (Throwable e) {
                        sendMessage(sender, Messages.get("as-reload-failed", e.getMessage()));
                        e.printStackTrace();
                        return 0;
                    }

                    long tookMs = System.currentTimeMillis() - start;
                    sendMessage(sender, Messages.get("as-reload-success", tookMs));
                    return SINGLE_SUCCESS;
                })
        );

        root.then(LiteralArgumentBuilder.<CommandSource>literal("ufw")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("ip", StringArgumentType.word())
                        .suggests(FIREWALLED_IPS_SUGGESTIONS)
                        .executes(context -> {
                            CommandSource sender = context.getSource();
                            String arg2 = StringArgumentType.getString(context, "ip");

                            InetAddress ip;
                            try {
                                // assumes the user does not input a resolvable domain (cuz that would block, not great)
                                ip = InetAddress.getByName(arg2);
                            } catch (Exception e) {
                                sendMessage(sender, Messages.get("as-ufw-invalid-ip", arg2));
                                return 0; // Return 0 to indicate command failure
                            }

                            if (FireWallManager.removeDynamic(ip)) {
                                sendMessage(sender, Messages.get("as-ufw-removed", arg2));
                            } else {
                                if (PanicModeManager.isBlocked(ip)) {
                                    sendMessage(sender, Messages.get("as-ufw-not-firewalled-panicmode", arg2));
                                } else if (FireWallManager.isBlocked0(ip)) {
                                    sendMessage(sender, Messages.get("as-ufw-blocked-static", arg2));
                                } else {
                                    sendMessage(sender, Messages.get("as-ufw-not-firewalled", arg2));
                                }
                            }

                            return SINGLE_SUCCESS;
                        })
                )
        );

        root.then(BrigadierCommand.literalArgumentBuilder("sendverifyemail")
                .executes(context -> {
                    /*if (Boolean.TRUE.equals(ServerSettingsManager.get(Setting.VERIFIED_EMAIL))) {
                        return SINGLE_SUCCESS;
                    }*/
                    CommandSource sender = context.getSource();
                    EmailHandler.sendVerifyMail(sender, EmailConfig.INSTANCE.email, true, AlixUtils::sendMessage);
                    return SINGLE_SUCCESS;
                })
        );

        addSubcommand(root, Arrays.asList("verifyemail"),
                argument("code", StringArgumentType.greedyString()).executes(context -> {
                    CommandSource sender = context.getSource();
                    String code = StringArgumentType.getString(context, "code");
                    EmailHandler.verifyMail(sender, null, code, true, AlixUtils::sendMessage);
                    return SINGLE_SUCCESS;
                })
        );

        root.then(BrigadierCommand.literalArgumentBuilder("profilejoins")
                .executes(context -> {
                    CommandSource sender = context.getSource();
                    LimboJoinProfiler.PROFILE_JOINS = !LimboJoinProfiler.PROFILE_JOINS;

                    if (LimboJoinProfiler.PROFILE_JOINS)
                        sendMessage(sender, Messages.get("as-profilejoins-enabled"));
                    else
                        sendMessage(sender, Messages.get("as-profilejoins-disabled"));
                    return SINGLE_SUCCESS;
                })
        );


        root.then(BrigadierCommand.literalArgumentBuilder("panicmode")
                .executes(context -> {
                    CommandSource sender = context.getSource();
                    if (PanicModeManager.activate("Manual trigger.")) {
                        sendMessage(sender, Messages.get("as-panicmode-enabled"));
                    } else {
                        sendMessage(sender, Messages.get("as-panicmode-already-enabled"));
                    }
                    return SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.literalArgumentBuilder("on")
                        .executes(context -> {
                            CommandSource sender = context.getSource();
                            if (PanicModeManager.activate("Manual trigger.")) {
                                sendMessage(sender, Messages.get("as-panicmode-enabled"));
                            } else {
                                sendMessage(sender, Messages.get("as-panicmode-already-enabled"));
                            }
                            return SINGLE_SUCCESS;
                        })
                )
                .then(BrigadierCommand.literalArgumentBuilder("off")
                        .executes(context -> {
                            CommandSource sender = context.getSource();
                            if (PanicModeManager.deactivate("Manual trigger.")) {
                                sendMessage(sender, Messages.get("as-panicmode-disabled"));
                            } else {
                                sendMessage(sender, Messages.get("as-panicmode-already-disabled"));
                            }
                            return SINGLE_SUCCESS;
                        })
                )
        );

        if (devMode) {
            root.then(BrigadierCommand.literalArgumentBuilder("testdb")
                    .executes(context -> {
                        CommandSource sender = context.getSource();
                        DatabaseUpdater.INSTANCE.testDatabase();
                        sendMessage(sender, "sex tested, check this shit out");
                        return SINGLE_SUCCESS;
                    })
            );
        }

        addSubcommand(root, Arrays.asList("bl", "bypasslist", "bypasslimit"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "name");
                            CommandSource sender = context.getSource();
                            if (AllowListFileManager.has(target)) {
                                sendMessage(sender, Messages.get("as-bypasslimit-already-added", target));
                                return SINGLE_SUCCESS;
                            }
                            AllowListFileManager.add(target);
                            sendMessage(sender, Messages.get("as-bypasslimit-added", target));
                            return SINGLE_SUCCESS;
                        })
        );

        addSubcommand(root, Arrays.asList("bl-r", "bypasslist-remove", "bypasslimit-remove"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "name");
                            CommandSource sender = context.getSource();
                            if (AllowListFileManager.remove(target)) {
                                sendMessage(sender, Messages.get("as-bypasslimit-removed", target));
                                return SINGLE_SUCCESS;
                            }
                            sendMessage(sender, Messages.get("as-bypasslimit-not-added", target));
                            return SINGLE_SUCCESS;
                        })
        );

        addSubcommand(root, Arrays.asList("frd", "fullyremovedata"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "name");
                            CommandSource sender = context.getSource();
                            PersistentUserData data = UserFileManager.remove(target);
                            if (AllowListFileManager.remove(target)) {
                                sendMessage(sender, Messages.get("as-frd-removed-from-allowlist", target));
                            }
                            if (data == null) {
                                sendMessage(sender, playerDataNotFound.format(target));
                                return SINGLE_SUCCESS;
                            }
                            GeoIPTracker.removeIP(data.getSavedIP());
                            sendMessage(sender, Messages.get("as-frd-success", target));
                            return SINGLE_SUCCESS;
                        })
        );

        addSubcommand(root, Arrays.asList("rs", "resetstatus"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "name");
                            CommandSource sender = context.getSource();
                            PersistentUserData data = UserFileManager.get(target);
                            if (data == null) {
                                sendMessage(sender, playerDataNotFound.format(target));
                                return SINGLE_SUCCESS;
                            }
                            data.setPremiumData(PremiumData.UNKNOWN);
                            sendMessage(sender, Messages.get("as-resetstatus-success", target));
                            return SINGLE_SUCCESS;
                        })
        );

        addSubcommand(root, Arrays.asList("rp", "resetpassword"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .then(
                                // Optional login type argument
                                RequiredArgumentBuilder.<CommandSource, String>argument("logintype", StringArgumentType.word())
                                        .suggests(LOGIN_TYPE_SUGGESTIONS)
                                        .executes(context -> {
                                            String target = StringArgumentType.getString(context, "name");
                                            String loginTypeArg = StringArgumentType.getString(context, "logintype");
                                            CommandSource sender = context.getSource();
                                            PersistentUserData data = UserFileManager.get(target);
                                            if (data == null) {
                                                sendMessage(sender, playerDataNotFound.format(target));
                                                return SINGLE_SUCCESS;
                                            }
                                            data.resetPasswords();
                                            Main.PLUGIN.getServer().getPlayer(data.getName()).ifPresent(p ->
                                                    p.disconnect(MessageWrapper.parseLegacy(passwordResetMessage))
                                            );
                                            LoginType type;
                                            try {
                                                type = LoginType.valueOf(loginTypeArg.toUpperCase());
                                            } catch (Exception e) {
                                                sendMessage(sender, Messages.get("as-invalid-login-type", loginTypeArg));
                                                return SINGLE_SUCCESS;
                                            }
                                            data.setLoginType(type);
                                            sendMessage(sender, Messages.get("as-resetpassword-success-with-type", target, type));
                                            return SINGLE_SUCCESS;
                                        })
                        )
                        // Execute without login type provided
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "name");
                            CommandSource sender = context.getSource();
                            PersistentUserData data = UserFileManager.get(target);
                            if (data == null) {
                                sendMessage(sender, playerDataNotFound.format(target));
                                return SINGLE_SUCCESS;
                            }
                            data.resetPasswords();
                            Main.PLUGIN.getServer().getPlayer(data.getName()).ifPresent(p ->
                                    p.disconnect(MessageWrapper.parseLegacy(passwordResetMessage))
                            );
                            sendMessage(sender, Messages.get("as-resetpassword-success", target));
                            return SINGLE_SUCCESS;
                        })
        );

        // Subcommand: User data ("user")
        addSubcommand(root, Arrays.asList("user"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "name");
                            CommandSource sender = context.getSource();
                            AlixScheduler.async(() -> {
                                PersistentUserData data = UserFileManager.get(target);
                                if (data == null) {
                                    sendMessage(sender, playerDataNotFound.format(target));
                                    return;
                                }

                                Channel channel = AlixCommonUtils.channel(data);

                                boolean isPremium = data.getPremiumData().getStatus().isPremium();

                                sendMessage(sender, "");
                                sendMessage(sender, Messages.get("as-user-header", target));
                                sendMessage(sender, Messages.get("as-user-ip", data.getSavedIP().getHostAddress()));
                                sendMessage(sender, Messages.get("as-user-premium-status", data.getPremiumData().getStatus().readableName()));

                                if (Telemetry.ENABLED && TelemetryProfilerImpl.SAVE_SYN_ENABLED && channel != null) {
                                    var sig = TelemetryProfiler.synSignature(channel);
                                    if (sig != null) {
                                        sendMessage(sender, Messages.get("as-user-os", sig.os.getReadableName()));
                                        sendMessage(sender, Messages.get("as-user-connection-env", sig.mtuEnv.getReadableName()));
                                    } else
                                        sendMessage(sender, Messages.get("as-user-syn-signature-error"));
                                }

                                if (!data.getSavedIP().equals(PersistentUserData.UNKNOWN_IP)) {//I guess possibly incorrect info when testing on localhost
                                    var accounts = UserFileManager.getAllData().stream().filter(d -> d.getSavedIP().equals(data.getSavedIP()))
                                            .map(PersistentUserData::getName).toList();

                                    var extraInfo = accounts.size() > 1 ? " &7(" + String.join(", ", accounts) + ")" : "";
                                    sendMessage(sender, Messages.get("as-user-accounts", accounts.size(), extraInfo));
                                }

                                var email = data.getEmail();
                                if (email != null)
                                    sendMessage(sender, Messages.get("as-user-email", email.email()));

                                var isEncrypted = AlixUtils.isOnlineEncryptionEnabled(channel);
                                if (isEncrypted != null)
                                    sendMessage(sender, Messages.get("as-user-encryption", stateOf(isEncrypted)));

                                if (!isPremium) {
                                    sendMessage(sender, Messages.get("as-user-ip-autologin", stateOf(data.getLoginParams().getIpAutoLogin())));
                                    sendMessage(sender, Messages.get("as-user-login-type", data.getLoginType()));

                                    boolean dVer = data.getLoginParams().isDoubleVerificationEnabled();
                                    if (dVer)
                                        sendMessage(sender, Messages.get("as-user-second-login-type", data.getLoginParams().getExtraLoginType()));
                                    sendMessage(sender, Messages.get("as-user-double-verification", stateOf(dVer)));
                                    String authApp;
                                    switch (data.getLoginParams().getAuthSettings()) {
                                        case PASSWORD:
                                            authApp = Messages.get("state-disabled");
                                            break;
                                        case AUTH_APP:
                                            authApp = Messages.get("as-user-auth-app-only");
                                            break;
                                        case PASSWORD_AND_AUTH_APP:
                                            authApp = dVer ? Messages.get("as-user-auth-app-and-passwords") : Messages.get("as-user-auth-app-and-password");
                                            break;
                                        default:
                                            throw new RuntimeException("Invalid - " + data.getLoginParams().getAuthSettings());
                                    }
                                    sendMessage(sender, Messages.get("as-user-totp", authApp));
                                    sendMessage(sender, Messages.get("as-user-totp-linked", stateOf(data.getLoginParams().hasProvenAuthAccess(), true)));
                                }
                                sendMessage(sender, "");
                            });
                            return SINGLE_SUCCESS;
                        })
        );

        // Subcommand: Change / set password ("cp", "changepassword", "setpassword")
        addSubcommand(root, Arrays.asList("cp", "changepassword", "setpassword"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("password", StringArgumentType.word())
                                .executes(context -> {
                                    String target = StringArgumentType.getString(context, "name");
                                    String password = StringArgumentType.getString(context, "password");
                                    CommandSource sender = context.getSource();

                                    PersistentUserData data = UserFileManager.get(target);
                                    if (data == null) {
                                        sendMessage(sender, playerDataNotFound.format(target));
                                        return SINGLE_SUCCESS;
                                    }

                                    LoginType type = data.getLoginType();
                                    AlixCommonUtils.getPasswordInvalidityReasonAsync(password, type, invalidityReason -> {
                                        if (invalidityReason != null) {
                                            sendMessage(sender, invalidityReason);
                                            return;
                                        }

                                        data.setPassword(password);
                                        data.setLoginType(type);
                                        String passFormatted = "*".repeat(Math.max(0, password.length() - 3)) + password.substring(Math.max(0, password.length() - 3));
                                        sendMessage(sender, Messages.get("as-changepassword-success", data.getName(), passFormatted, type));

                                        if (data.getLoginParams().getExtraLoginType() != null) {
                                            sendMessage(sender, Messages.get("as-changepassword-extra-login-cleared"));
                                            data.getLoginParams().setExtraLoginType(null);
                                        }
                                    });

                                    return SINGLE_SUCCESS;
                                })
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("logintype", StringArgumentType.word())
                                        .suggests(LOGIN_TYPE_SUGGESTIONS)
                                        .executes(context -> {
                                            String target = StringArgumentType.getString(context, "name");
                                            String password = StringArgumentType.getString(context, "password");
                                            String loginTypeArg = StringArgumentType.getString(context, "logintype");
                                            CommandSource sender = context.getSource();

                                            PersistentUserData data = UserFileManager.get(target);
                                            if (data == null) {
                                                sendMessage(sender, playerDataNotFound.format(target));
                                                return SINGLE_SUCCESS;
                                            }

                                            LoginType type;
                                            try {
                                                type = LoginType.from(loginTypeArg.toUpperCase(), false, false);
                                            } catch (Exception e) {
                                                sendMessage(sender, Messages.get("as-invalid-login-type", loginTypeArg));
                                                return SINGLE_SUCCESS;
                                            }

                                            AlixCommonUtils.getPasswordInvalidityReasonAsync(password, type, invalidityReason -> {
                                                if (invalidityReason != null) {
                                                    sendMessage(sender, invalidityReason);
                                                    return;
                                                }

                                                data.setPassword(password);
                                                data.setLoginType(type);
                                                String passFormatted = "*".repeat(Math.max(0, password.length() - 3)) + password.substring(Math.max(0, password.length() - 3));
                                                sendMessage(sender, Messages.get("as-changepassword-success", data.getName(), passFormatted, type));

                                                if (data.getLoginParams().getExtraLoginType() != null) {
                                                    sendMessage(sender, Messages.get("as-changepassword-extra-login-cleared"));
                                                    data.getLoginParams().setExtraLoginType(null);
                                                }
                                            });

                                            return SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
        // Subcommand: Force status ("fs", "forcestatus")
        addSubcommand(root, Arrays.asList("fs", "forcestatus"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("status", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    Arrays.stream(PremiumStatus.values()).map(Enum::name).forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String target = StringArgumentType.getString(context, "name");
                                    String statusArg = StringArgumentType.getString(context, "status");
                                    CommandSource sender = context.getSource();

                                    PersistentUserData data = UserFileManager.get(target);
                                    if (data == null) {
                                        sendMessage(sender, playerDataNotFound.format(target));
                                        return SINGLE_SUCCESS;
                                    }

                                    PremiumStatus status;
                                    try {
                                        status = PremiumStatus.valueOf(statusArg.toUpperCase());
                                    } catch (Exception e) {
                                        sendMessage(sender, Messages.get("as-forcestatus-invalid-status"));
                                        return SINGLE_SUCCESS;
                                    }

                                    if (data.getPremiumData().getStatus() == status) {
                                        sendMessage(sender, Messages.get("as-forcestatus-already-has-status", target, status));
                                        return SINGLE_SUCCESS;
                                    }

                                    if (!status.isPremium()) {
                                        data.setPremiumData(status.isNonPremium() ? PremiumData.NON_PREMIUM : PremiumData.UNKNOWN);
                                        sendMessage(sender, Messages.get("as-forcestatus-success", target, status));
                                        return SINGLE_SUCCESS;
                                    }

                                    var cached = PremiumDataCache.getOrUnknown(target);
                                    if (cached.getStatus().isKnown()) {
                                        if (cached.getStatus().isPremium()) {
                                            data.setPremiumData(cached);
                                            sendMessage(sender, Messages.get("as-forcestatus-success-cached", target, cached.premiumUUID()));
                                            return SINGLE_SUCCESS;
                                        }
                                        sendMessage(sender, Messages.get("as-forcestatus-cached-non-premium", target));
                                        return SINGLE_SUCCESS;
                                    }

                                    /*ConnectedPlayer player = (ConnectedPlayer) Main.PLUGIN.getServer().getPlayer(target).orElse(null);
                                    if (player != null && !UserManager.hasVerified(player.getUniqueId())) {
                                        var packetUUID = VelocityLimboIntegration.getLoginAssignedUUID(player.getConnection().getChannel());
                                        if (packetUUID != null && packetUUID.version() != 4) {
                                            sendMessage(sender, "&ePlayer " + target + " declared himself as NON_PREMIUM, and thus his status cannot be set to PREMIUM.");
                                            return SINGLE_SUCCESS;
                                        }
                                    }*/

                                    PremiumUtils.getOrRequestAndCacheData(null, target, newPremiumData -> {
                                        if (newPremiumData.getStatus().isUnknown()) {
                                            sendMessage(sender, Messages.get("as-forcestatus-unknown", target));
                                            return;
                                        }
                                        if (newPremiumData.getStatus().isNonPremium()) {
                                            sendMessage(sender, Messages.get("as-forcestatus-api-non-premium", target));
                                            return;
                                        }
                                        data.setPremiumData(newPremiumData);
                                        sendMessage(sender, Messages.get("as-forcestatus-success-api", target, newPremiumData.premiumUUID()));
                                    });

                                    return SINGLE_SUCCESS;
                                })
                        )
        );


        // Fallback execution (when no subcommand is provided)
        root.executes(context -> {
            sendAdminCommandsList(context.getSource());
            return SINGLE_SUCCESS;
        });

        // Subcommand: lists every command (both admin and player-facing) along with a short description of what it does.
        // Deliberately left gated by the root's "alixsystem.admin" requirement, same as every other "/as ..."
        // subcommand - "/as" is an admin command tree, and admin subcommand names/usage shouldn't be exposed
        // to (or runnable by) non-admins just because this particular one happens to also list player-facing
        // commands for the admin's own reference. A previous revision exempted this subcommand from the
        // permission check so regular players could use it too, which was the wrong fix: it let any player
        // run an "/as ..." subcommand and see the full admin command list. Players who just want to see their
        // own available commands should use the separate, genuinely non-admin "/alixhelp" command instead
        // (see CommandManager#register_Help()), which only lists sendPlayerCommandsList()'s content.
        root.then(BrigadierCommand.literalArgumentBuilder("commands")
                .executes(context -> {
                    CommandSource sender = context.getSource();
                    sendAdminCommandsList(sender);
                    sendPlayerCommandsList(sender);
                    return SINGLE_SUCCESS;
                })
        );

        // Finally, register the command (using the aliases configured in commands.txt, e.g. "alix")
        commandManager.register(
                commandManager.metaBuilder("as")
                        .aliases(CommandsFileManager.getAliases("alixsystem"))
                        .plugin(Main.PLUGIN)
                        .build(),
                new BrigadierCommand(root)
        );
    }

    // Lists every admin ("/as ...") subcommand along with a short description of what it does, grouped into
    // sections rather than one flat, undifferentiated wall of red text - the previous layout had no header
    // or grouping at all, making a 16-command list hard to scan for anything specific.
    private static void sendAdminCommandsList(CommandSource sender) {
        sendMessage(sender, "");
        sendMessage(sender, Messages.get("admin-commands-header"));

        sendMessage(sender, Messages.get("admin-commands-section-accounts"));
        sendMessage(sender, Messages.get("admin-commands-user"));
        sendMessage(sender, Messages.get("admin-commands-resetpassword"));
        sendMessage(sender, Messages.get("admin-commands-resetpassword-type"));
        sendMessage(sender, Messages.get("admin-commands-changepassword"));
        sendMessage(sender, Messages.get("admin-commands-fullyremovedata"));
        sendMessage(sender, Messages.get("admin-commands-resetstatus"));
        sendMessage(sender, Messages.get("admin-commands-forcestatus"));
        sendMessage(sender, Messages.get("admin-commands-bypasslimit"));
        sendMessage(sender, Messages.get("admin-commands-bypasslimit-remove"));
        sendMessage(sender, "");

        sendMessage(sender, Messages.get("admin-commands-section-security"));
        sendMessage(sender, Messages.get("admin-commands-panicmode"));
        sendMessage(sender, Messages.get("admin-commands-ufw"));
        sendMessage(sender, "");

        sendMessage(sender, Messages.get("admin-commands-section-email"));
        sendMessage(sender, Messages.get("admin-commands-sendverifyemail"));
        sendMessage(sender, Messages.get("admin-commands-verifyemail"));
        sendMessage(sender, "");

        sendMessage(sender, Messages.get("admin-commands-section-server"));
        sendMessage(sender, Messages.get("admin-commands-save-all-local-to-db"));
        sendMessage(sender, Messages.get("admin-commands-reload"));
        sendMessage(sender, Messages.get("admin-commands-commands"));
        sendMessage(sender, "");
    }

    // "Enabled"/"Disabled" (or "Yep"/"Nope" when yesNo is true), localized - shared across every state
    // shown by "/as user <player>".
    private static String stateOf(boolean b) {
        return stateOf(b, false);
    }

    private static String stateOf(boolean b, boolean yesNo) {
        if (yesNo) return Messages.get(b ? "state-yes" : "state-no");
        return Messages.get(b ? "state-enabled" : "state-disabled");
    }

    // Lists every player-facing command along with a short description of what it does. Package-private (not
    // private) so CommandManager#register_Help() can reuse it for the separate, non-admin-gated "/alixhelp"
    // command - see the comment on the "commands" subcommand above for why that had to be a separate command
    // rather than just opening up this "/as ..." subcommand to everyone.
    static void sendPlayerCommandsList(CommandSource sender) {
        sendMessage(sender, Messages.get("player-commands-header"));

        sendMessage(sender, Messages.get("player-commands-section-login"));
        // /register and /login are deliberately not listed here: both are already explained to the
        // player at the point they're actually needed (a detailed prompt on first join for /register,
        // the respective login GUI/prompt for /login), so repeating them in a general command list adds
        // nothing at runtime.
        sendMessage(sender, Messages.get("player-commands-recovery"));
        // /terms is only ever relevant while 'require-terms-acceptance' is on (it's not a real command
        // otherwise), and even then it's already explained via the in-your-face prompt shown during
        // registration - only listed here as a reminder for that same reason, gated behind the setting
        // that makes it exist at all.
        if (LoginState.requireTermsAcceptance)
            sendMessage(sender, Messages.get("player-commands-terms"));
        sendMessage(sender, "");

        sendMessage(sender, Messages.get("player-commands-section-account"));
        sendMessage(sender, Messages.get("player-commands-account"));
        sendMessage(sender, Messages.get("player-commands-account-sendverifyemail"));
        sendMessage(sender, Messages.get("player-commands-account-verifyemail"));
        sendMessage(sender, Messages.get("player-commands-changepassword"));
        sendMessage(sender, Messages.get("player-commands-premium"));
        sendMessage(sender, "");
    }

    // Helper method to add the same subcommand under multiple aliases
    private static void addSubcommand(LiteralArgumentBuilder<CommandSource> root, List<String> aliases,
                                      com.mojang.brigadier.builder.ArgumentBuilder<CommandSource, ?> subcommand) {
        for (String alias : aliases) {
            LiteralArgumentBuilder<CommandSource> l = LiteralArgumentBuilder.literal(alias);
            root.then(l.then(subcommand));
        }
    }

    // Helper method to create a required argument
    private static RequiredArgumentBuilder<CommandSource, String> argument(String name, com.mojang.brigadier.arguments.ArgumentType<String> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}