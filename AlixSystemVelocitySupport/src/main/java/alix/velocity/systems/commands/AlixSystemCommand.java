package alix.velocity.systems.commands;

import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.file.AllowListFileManager;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.velocity.Main;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.connection.captcha.CaptchaState;

import java.util.Arrays;
import java.util.List;

import static alix.velocity.utils.AlixUtils.sendMessage;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class AlixSystemCommand {

    private static final String passwordResetMessage = Messages.get("password-reset-forcefully");
    private static final AlixMessage playerDataNotFound = Messages.getAsObject("player-data-not-found");

    // Suggestion provider for player names from UserFileManager
    private static final SuggestionProvider<CommandSource> USERNAME_SUGGESTIONS = (context, builder) -> {
        UserFileManager.getAllData().stream()
                .map(PersistentUserData::getName)
                .forEach(builder::suggest);
        return builder.buildFuture();
    };

    // Suggestion provider for login types
    private static final SuggestionProvider<CommandSource> LOGIN_TYPE_SUGGESTIONS = (context, builder) -> {
        builder.suggest("COMMAND");
        builder.suggest("PIN");
        builder.suggest("ANVIL");
        return builder.buildFuture();
    };

    public static void register(CommandManager commandManager) {
        // Root command literal (assumed command name: "as")
        LiteralArgumentBuilder<CommandSource> root = LiteralArgumentBuilder.literal("as");
        root.requires(source -> source.hasPermission("alixsystem.admin"));

        // Subcommand: Add to allow list ("bl", "bypasslist", "bypasslimit")
        addSubcommand(root, Arrays.asList("bl", "bypasslist", "bypasslimit"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "name");
                            CommandSource sender = context.getSource();
                            if (AllowListFileManager.has(target)) {
                                sendMessage(sender, "&cName '" + target + " is already on the account limit bypass list!");
                                return SINGLE_SUCCESS;
                            }
                            AllowListFileManager.add(target);
                            sendMessage(sender, "Added name '" + target + " to the account limit bypass list!");
                            return SINGLE_SUCCESS;
                        })
        );

        // Subcommand: Remove from allow list ("bl-r", "bypasslist-remove", "bypasslimit-remove")
        addSubcommand(root, Arrays.asList("bl-r", "bypasslist-remove", "bypasslimit-remove"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "name");
                            CommandSource sender = context.getSource();
                            if (AllowListFileManager.remove(target)) {
                                sendMessage(sender, "Removed name '" + target + " from the account limit bypass list!");
                                return SINGLE_SUCCESS;
                            }
                            sendMessage(sender, "&cName '" + target + " is not on the account limit bypass list!");
                            return SINGLE_SUCCESS;
                        })
        );

        // Subcommand: Fully remove data ("frd", "fullyremovedata")
        addSubcommand(root, Arrays.asList("frd", "fullyremovedata"),
                argument("name", StringArgumentType.word())
                        .suggests(USERNAME_SUGGESTIONS)
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "name");
                            CommandSource sender = context.getSource();
                            PersistentUserData data = UserFileManager.remove(target);
                            if (AllowListFileManager.remove(target)) {
                                sendMessage(sender, "Removed the name " + target + " from the allow-list!");
                            }
                            if (data == null) {
                                sendMessage(sender, playerDataNotFound.format(target));
                                return SINGLE_SUCCESS;
                            }
                            GeoIPTracker.removeIP(data.getSavedIP());
                            sendMessage(sender, "Fully removed the data of the account " + target + "!");
                            return SINGLE_SUCCESS;
                        })
        );

        // Subcommand: Reset premium status ("rs", "resetstatus")
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
                            sendMessage(sender, "The premium status of the player " + target + " has been set to UNKNOWN.");
                            return SINGLE_SUCCESS;
                        })
        );

        // Subcommand: Reset password ("rp", "resetpassword")
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
                                                    p.disconnect(Component.text(passwordResetMessage))
                                            );
                                            LoginType type;
                                            try {
                                                type = LoginType.valueOf(loginTypeArg.toUpperCase());
                                            } catch (Exception e) {
                                                sendMessage(sender, "Available login types: COMMAND, PIN & ANVIL, but instead got: " + loginTypeArg);
                                                return SINGLE_SUCCESS;
                                            }
                                            data.setLoginType(type);
                                            sendMessage(sender, "Successfully reset the password of the player " + target
                                                    + " and set his password type to " + type + "!");
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
                                    p.disconnect(Component.text(passwordResetMessage))
                            );
                            sendMessage(sender, "Successfully reset the password of the player " + target + ".");
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
                                    sendMessage(sender, "&cPlayer " + target + " is not in AlixSystem's User DataBase!");
                                    return;
                                }
                                boolean dVer = data.getLoginParams().isDoubleVerificationEnabled();
                                sendMessage(sender, "");
                                sendMessage(sender, "User " + target + " has the following data:");
                                sendMessage(sender, "IP: &c" + data.getSavedIP().getHostAddress());
                                sendMessage(sender, "IP AutoLogin: &c" + (data.getLoginParams().getIpAutoLogin() ? "Enabled" : "Disabled"));
                                sendMessage(sender, "Login Type: &c" + data.getLoginType());
                                sendMessage(sender, "Premium Status: &c" + data.getPremiumData().getStatus().readableName());
                                if (dVer)
                                    sendMessage(sender, "Second Login Type: &c" + data.getLoginParams().getExtraLoginType());
                                sendMessage(sender, "Double password verification: " + (dVer ? "&aEnabled" : "&cDisabled"));
                                String authApp;
                                switch (data.getLoginParams().getAuthSettings()) {
                                    case PASSWORD:
                                        authApp = "&cDisabled";
                                        break;
                                    case AUTH_APP:
                                        authApp = "&aEnabled - Just Auth App";
                                        break;
                                    case PASSWORD_AND_AUTH_APP:
                                        authApp = "&aEnabled - Auth App and " + (dVer ? "two " : "") + "in-game password" + (dVer ? "s" : "");
                                        break;
                                    default:
                                        throw new RuntimeException("Invalid - " + data.getLoginParams().getAuthSettings());
                                }
                                sendMessage(sender, "TOTP Auth app: " + authApp);
                                sendMessage(sender, "Has TOTP app linked: " + (data.getLoginParams().hasProvenAuthAccess() ? "&aYep" : "&cNope"));
                                sendMessage(sender, "");
                            });
                            return SINGLE_SUCCESS;
                        })
        );

        // Fallback execution (when no subcommand is provided)
        root.executes(context -> {
            CommandSource sender = context.getSource();
            sendMessage(sender, "");
            sendMessage(sender, "&c/as user <player> &7- Returns information about the given player.");
            sendMessage(sender, "&c/as bl/bypasslimit <name> &7- Adds the specified name to the account limit bypass list. " +
                    "Such accounts are not restricted by the account limiter, no matter the config 'max-total-accounts' parameter.");
            sendMessage(sender, "&c/as bl-r/bypasslimit-remove <name> &7- Removes the specified name from the account limit bypass list.");
            sendMessage(sender, "&c/as rp/resetpassword <player> &7- Resets the player's password.");
            sendMessage(sender, "&c/as rp/resetpassword <player> <login type> &7- Resets the player's password and changes their login type. Available login types: COMMAND, PIN & ANVIL.");
            sendMessage(sender, "&c/as frd/fullyremovedata <player> &7- Fully removes all account data of the specified player. The data cannot be restored after this operation.");
            sendMessage(sender, "&c/as rs/resetstatus <player> &7- Resets the player's premium status. Mainly aimed to forgive cracked players who used /premium");
            sendMessage(sender, "");
            return SINGLE_SUCCESS;
        });

        // Finally, register the command
        commandManager.register(new BrigadierCommand(root));
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