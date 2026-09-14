package alix.velocity.systems.commands;

import alix.common.commands.file.CommandsFileManager;
import alix.common.data.LoginType;
import alix.common.data.file.UserFileManager;
import alix.common.data.security.email.EmailHandler;
import alix.common.login.premium.PremiumUtils;
import alix.common.messages.Messages;
import alix.common.utils.AlixCommonUtils;
import alix.velocity.Main;
import alix.velocity.server.impl.VelocityLimboIntegration;
import alix.velocity.systems.packets.gui.impl.AccountGUI;
import alix.velocity.utils.AlixUtils;
import alix.velocity.utils.user.UserManager;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.connection.login.packets.SoundPackets;

import static alix.velocity.utils.AlixUtils.sendMessage;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public final class CommandManager {

    public static void register(ProxyServer server) {
        register_Account(server);
        register_ChangePassword(server);
        AlixSystemCommand.register(server.getCommandManager());
        //EmailCommand.register_VerifyEmail(server);
        //EmailCommand.register_SendVerifyEmail(server);
        register_Premium(server.getCommandManager());

        registerPlaceholderCommand("confirm");
        registerPlaceholderCommand("cancel");
    }

    private static void registerPlaceholderCommand(String commandName) {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder.<CommandSource>literal(commandName)
                .requires(source -> {
                    return source instanceof Player;//UserManager.getVerified(player.getUniqueId()).getDuplexProcessor().isSettingUp2FA();
                }).executes(context -> Command.SINGLE_SUCCESS).build();

        BrigadierCommand command = new BrigadierCommand(node);
        Main.SERVER.getCommandManager().register(command);
    }

    private static void register_Account(ProxyServer server) {
        var accountCmd = LiteralArgumentBuilder.<CommandSource>literal("account")
                .requires(source -> source instanceof Player)
                .executes(ctx -> {
                    if (isConsole(ctx)) return SINGLE_SUCCESS;
                    Player player = (Player) ctx.getSource();

                    VerifiedUser user = UserManager.getVerified(player.getUniqueId());

                    var data = user.getData();
                    if (data == null) {
                        player.sendRichMessage("<red>Error - Missing persistent data");
                        return SINGLE_SUCCESS;
                    }
                    AccountGUI.add(user);

                    return SINGLE_SUCCESS;
                });

        var verifyEmailCmd = LiteralArgumentBuilder.<CommandSource>literal("verifyemail")
                .executes(ctx -> {
                    if (isConsole(ctx)) return SINGLE_SUCCESS;
                    Player player = (Player) ctx.getSource();
                    AlixUtils.sendMessage(player, "&eSpecify the verify code!");
                    return SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("verify-code", StringArgumentType.word())
                        .executes(ctx -> {
                            if (isConsole(ctx)) return SINGLE_SUCCESS;
                            Player player = (Player) ctx.getSource();
                            String code = StringArgumentType.getString(ctx, "verify-code");

                            EmailHandler.verifyMail(player, UserFileManager.get(player.getUsername()), code, false, AlixUtils::sendMessage);
                            return SINGLE_SUCCESS;
                        })
                );
        var sendVerifyEmailCmd = LiteralArgumentBuilder.<CommandSource>literal("sendverifyemail")
                .executes(ctx -> {
                    if (isConsole(ctx)) return SINGLE_SUCCESS;
                    Player player = (Player) ctx.getSource();
                    AlixUtils.sendMessage(player, "&eUsage: /account sendverifyemail <email>");
                    return SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("email", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            if (isConsole(ctx)) return SINGLE_SUCCESS;
                            Player player = (Player) ctx.getSource();
                            String email = StringArgumentType.getString(ctx, "email");

                            EmailHandler.sendVerifyMail(player, email, false, AlixUtils::sendMessage);
                            return SINGLE_SUCCESS;
                        })
                );

        accountCmd.then(verifyEmailCmd);
        accountCmd.then(sendVerifyEmailCmd);

        var manager = server.getCommandManager();
        manager.register(
                manager.metaBuilder("account")
                        .aliases(CommandsFileManager.getAliases("account"))
                        .plugin(Main.PLUGIN)
                        .build(),
                new BrigadierCommand(accountCmd.build())
        );
    }

    private static void register_ChangePassword(ProxyServer server) {
        var cmd = command("changepassword", ctx -> {
            if (isConsole(ctx)) return SINGLE_SUCCESS;

            Player player = (Player) ctx.getSource();
            sendMessage(player, "&eSpecify your new password!");
            return SINGLE_SUCCESS;
        }).then(BrigadierCommand.requiredArgumentBuilder("password", StringArgumentType.word())
                .executes(ctx -> {
                    if (isConsole(ctx)) return SINGLE_SUCCESS;
                    Player player = (Player) ctx.getSource();
                    VerifiedUser user = UserManager.getVerified(player.getUniqueId());

                    var data = user.getData();
                    if (data == null) {
                        player.sendRichMessage("<red>Error - missing persistent data");
                        return SINGLE_SUCCESS;
                    }
                    String password = StringArgumentType.getString(ctx, "password");

                    String reason = AlixCommonUtils.getPasswordInvalidityReason(password, LoginType.ANVIL);
                    if (reason != null) {
                        user.writePacketSilently(SoundPackets.wrapperOf(Sounds.ENTITY_VILLAGER_NO));
                        player.sendMessage(Component.text(reason));
                        return SINGLE_SUCCESS;
                    }

                    if (data.getLoginType() == LoginType.PIN)
                        data.setLoginType(LoginType.ANVIL);
                    data.setPassword(password);
                    sendMessage(player, Messages.get("password-changed"));
                    return SINGLE_SUCCESS;
                })
        ).build();

        var manager = server.getCommandManager();

        manager.register(manager.metaBuilder("changepassword").aliases(CommandsFileManager.getAliases("changepassword")).plugin(Main.PLUGIN).build(), new BrigadierCommand(cmd));
    }

    private static void register(String cmd, com.velocitypowered.api.command.Command command) {
        var server = Main.PLUGIN.getServer();
        var info = CommandsFileManager.getCommand(cmd);
        var manager = server.getCommandManager();

        manager.register(manager.metaBuilder(info.getCommand()).aliases(info.getAliasesNotNull()).plugin(Main.PLUGIN).build(), command);
    }

    private static final String
            alreadyPremiumMessage = Messages.getWithPrefix("premium-command-already-premium"),
            premiumDataMessage = Messages.getWithPrefix("premium-command-premium"),
            nonPremiumDataMessage = Messages.getWithPrefix("premium-command-non-premium"),
            unknownDataMessage = Messages.getWithPrefix("premium-command-unknown");

    public static void register_Premium(com.velocitypowered.api.command.CommandManager commandManager) {
        LiteralArgumentBuilder<CommandSource> premiumLiteral = command("premium", ctx -> {
            if (isConsole(ctx)) return 0;
            Player player = (Player) ctx.getSource();
            var user = UserManager.getVerified(player.getUniqueId());
            var channel = user.getChannel();
            var uuid = VelocityLimboIntegration.getLoginAssignedUUID(channel);
            boolean canBePremium = uuid == null || uuid.version() == 4;

            if (!canBePremium) {
                user.sendMessage(nonPremiumDataMessage);
                return SINGLE_SUCCESS;
            }

            String name = player.getUsername();
            var data = UserFileManager.get(name);

            if (data.getPremiumData().getStatus().isPremium()) {
                user.sendMessage(alreadyPremiumMessage);
                return SINGLE_SUCCESS;
            }

            PremiumUtils.getOrRequestAndCacheData(channel, name, premiumData -> {
                switch (premiumData.getStatus()) {
                    case PREMIUM:
                        user.sendMessage(premiumDataMessage);
                        data.setPremiumData(premiumData);
                        break;
                    case NON_PREMIUM:
                        user.sendMessage(nonPremiumDataMessage);
                        break;
                    case UNKNOWN:
                        user.sendMessage(unknownDataMessage);
                        break;
                }
            });
            return SINGLE_SUCCESS;
        });

        commandManager.register(new BrigadierCommand(premiumLiteral));
    }

    static boolean isConsole(CommandContext<CommandSource> ctx) {
        if (!(ctx.getSource() instanceof Player)) {
            ctx.getSource().sendRichMessage("<red>Console cannot execute this command!</red>");
            return true;
        }
        return false;
    }

    static LiteralArgumentBuilder<CommandSource> command(String cmd, Command<CommandSource> executor) {
        LiteralArgumentBuilder<CommandSource> builder = literal(cmd);
        return builder.executes(executor);
    }
}