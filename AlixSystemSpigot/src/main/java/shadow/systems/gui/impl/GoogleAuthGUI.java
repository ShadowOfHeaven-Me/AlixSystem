package shadow.systems.gui.impl;

import alix.common.data.AuthSetting;
import alix.common.data.LoginParams;
import alix.common.messages.Messages;
import alix.common.packets.message.MessageWrapper;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.collections.list.LoopList;
import alix.common.utils.formatter.AlixFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import shadow.systems.gui.AlixGUI;
import shadow.systems.gui.item.GUIItem;
import shadow.systems.login.auth.GoogleAuth;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.version.AlixMaterials;
import shadow.utils.objects.savable.data.AuthDataChanges;
import shadow.utils.users.UserManager;
import shadow.utils.users.types.VerifiedUser;

import java.util.Arrays;

public final class GoogleAuthGUI extends AlixGUI {

    private static final GUIItem whatIsThis, GO_BACK_GUI_ITEM;
    private static final ItemStack showQRCodeItem, applyChangesItem, resetTokenItem, resetTokenConfirmItem, viewRecoveryCodesItem;
    private static final AuthItemType PASSWORD, AUTH, AUTH_AND_PASSWORD;
    private static final String guiTitle;

    static {
        guiTitle = Messages.get("gui-title-google-auth");
        PASSWORD = new AuthItemType(setLore(create(Material.OBSIDIAN, Messages.get("gui-google-auth-config-password-name")), Messages.getSplit("gui-google-auth-config-password-lore")), AuthSetting.PASSWORD);
        AUTH = new AuthItemType(setLore(create(Material.NETHER_STAR, Messages.get("gui-google-auth-config-auth-name")), Messages.getSplit("gui-google-auth-config-auth-lore")), AuthSetting.AUTH_APP);
        AUTH_AND_PASSWORD = new AuthItemType(setLore(create(Material.BEACON, Messages.get("gui-google-auth-config-auth-and-password-name")), Messages.getSplit("gui-google-auth-config-auth-and-password-lore")), AuthSetting.PASSWORD_AND_AUTH_APP);
    }

    private static final AuthItemType[] AUTH_TYPES = {
            PASSWORD, AUTH, AUTH_AND_PASSWORD
    };

    static {
        GO_BACK_GUI_ITEM = new GUIItem(GO_BACK_ITEM, e -> AccountGUI.add((Player) e.getWhoClicked()));

        whatIsThis = new GUIItem(AlixUtils.getSkull(Messages.get("gui-google-auth-what-is-this-name"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMyNzEwNTI3MTllZjY0MDc5ZWU4YzE0OTg5NTEyMzhhNzRkYWM0YzI3Yjk1NjQwZGI2ZmJkZGMyZDZiNWI2ZSJ9fX0="));
        String[] loreWhatIsThis = Messages.get("gui-google-auth-what-is-this").split(" -nl ");
        setLore(whatIsThis.getItem(), loreWhatIsThis);

        showQRCodeItem = AlixUtils.getSkull(Messages.get("gui-google-auth-show-qr-code-name"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTUzYzE0OTUwZmMzNjQ2NzhiNzU1NDRhY2IxZGEwYzk0MjBiNTA2ZTU4NzEyMDM5M2IzZDFhZDQ4OThlNzRmIn19fQ==");
        String[] loreQRCode = Messages.get("gui-google-auth-show-qr-code-lore").split(" -nl ");
        setLore(showQRCodeItem, loreQRCode);

        applyChangesItem = rename(AlixMaterials.GREEN_CONCRETE.getItemCloned(), Messages.get("gui-google-auth-apply-changes"));

        resetTokenItem = setLore(rename(AlixMaterials.RED_CONCRETE.getItemCloned(), Messages.get("gui-google-auth-reset-token-name")),
                Messages.get("gui-google-auth-reset-token-lore").split(" -nl "));

        resetTokenConfirmItem = create(Material.TNT, Messages.get("gui-google-auth-reset-token-confirm-name"),
                Messages.get("gui-google-auth-reset-token-confirm-lore").split(" -nl "));

        viewRecoveryCodesItem = create(Material.PAPER, Messages.get("gui-google-auth-view-recovery-codes-name"),
                Messages.get("gui-google-auth-view-recovery-codes-lore").split(" -nl "));
    }

    //private final VerifiedUser user;
    //Armed by a first click on resetTokenItem, cleared on a second (confirming) click - a fresh instance is
    //created per GUI open (see add() below), so this naturally resets whenever the player reopens the menu.
    private boolean resetArmed;

    private GoogleAuthGUI(Player player) {
        super(Bukkit.createInventory(player, 27, MessageWrapper.parseToLegacyString(guiTitle)), player);
        //this.user = UserManager.getVerifiedUser(player);
    }

    @Override
    protected GUIItem[] create(Player player) {
        GUIItem[] items = new GUIItem[27];
        Arrays.fill(items, BACKGROUND_ITEM);
        AuthDataChanges changes = new AuthDataChanges();

        VerifiedUser user = UserManager.getVerifiedUser(player);
        LoginParams params = user.getData().getLoginParams();

        LoopList<AuthItemType> authList = LoopList.of(AUTH_TYPES);
        authList.setCurrentIndex(authList.indexOfFirst(t -> t.authSetting.equals(params.getAuthSettings())));

        items[8] = GO_BACK_GUI_ITEM;

        items[10] = whatIsThis;

        items[13] = new GUIItem(showQRCodeItem, e -> {
            MAP.remove(player.getUniqueId());

            //SECURITY (2026-09-23, revised after ShadowOfHeaven's PR review): this button used to also
            //rotate the token here on every view while the app isn't required yet, for the same underlying
            //reason as AuthDataChanges#tryApply()'s comment below - a pre-generated-since-3.10.0 secret
            //could otherwise be cloned by anyone before it's ever actually enabled. That's now handled at
            //the one point it actually needs to be instead: AuthDataChanges#tryApply() rotates the token
            //right before it's shown as part of ACTUALLY enabling the app, which fully closes the same gap
            //without also making the token generally volatile - several other places (email encryption in
            //particular) rely on it staying stable except through that class's/Reset Token's own
            //transactional commitTokenAndEmail() writes. This button can go back to simply showing whatever
            //the current token is.
            if (!params.hasProvenAuthAccess() && !params.getAuthSettings().requiresAuthApp()) {
                AlixScheduler.async(() -> GoogleAuth.showQRCode(user, player));
                return;
            }

            //Shows the persistent secret itself (via QR code) - not a rotating code, so anyone who briefly
            //gets at an already-logged-in, unattended session could otherwise scan it onto their own device
            //outright, same as the reset-token/view-recovery-codes buttons below. Same gate as those.
            runGatedByAuthAccess(user, () -> AlixScheduler.async(() -> GoogleAuth.showQRCode(user, player)));
            //the inv is closed here /\ (or by verifyAuthAccess()'s own GUI, if gated)
        });

        items[16] = new GUIItem(authList.current().item, event -> {
            ClickType type = event.getClick();
            switch (type) {
                case LEFT:
                case RIGHT:
                    AuthItemType c = type == ClickType.RIGHT ? authList.previous() : authList.next();
                    changes.setAuthSetting(c.authSetting);
                    gui.setItem(16, c.item);
                    break;
            }
        });

        items[4] = new GUIItem(resetTokenItem, event -> {
            if (!this.resetArmed) {
                this.resetArmed = true;
                gui.setItem(4, resetTokenConfirmItem);
                return;
            }

            this.resetArmed = false;
            MAP.remove(player.getUniqueId());
            //Resetting the secret hands whoever's sitting at the keyboard right now a brand new QR code -
            //someone who briefly gets at an already-logged-in, unattended session could otherwise steal the
            //2FA factor outright by scanning it onto their own device, without ever knowing the password or
            //the current code. Gated the exact same way AuthDataChanges already gates changing the auth
            //TYPE (hasProvenAuthAccess / verifyAuthAccess re-prompting for the CURRENT code) - see
            //LoginState#handleRecoveryCodeCommand() for why a player who reset via a recovery code (because
            //they lost their device) isn't locked out of this by having no current code to re-enter.
            runGatedByAuthAccess(user, () -> AlixScheduler.async(() -> {
                //regenerateAuthToken() throws if it couldn't safely re-encrypt the stored email under the
                //new token (see its own docs) - caught here so the player gets a clear failure message
                //instead of the reset silently doing nothing (the old token/QR code stays valid either way).
                try {
                    user.getData().regenerateAuthToken();
                } catch (Exception e) {
                    AlixCommonUtils.logException(e);
                    //Player#sendMessage() isn't guaranteed thread-safe off the main thread - AlixScheduler.sync()
                    //here, same as the "View Recovery Codes" handler below and Velocity's port of this handler.
                    AlixScheduler.sync(() -> player.sendMessage(MessageWrapper.parseLegacy(Messages.getWithPrefix("gui-google-auth-reset-token-failed-chat"))));
                    return;
                }

                String[] codes = user.getData().regenerateRecoveryCodes();

                AlixScheduler.sync(() -> {
                    player.sendMessage(MessageWrapper.parseLegacy(Messages.getWithPrefix("gui-google-auth-reset-token-success-chat")));
                    sendRecoveryCodes(player, codes);
                });

                //showQRCode() is already designed to be called from an async context, same as the
                //pre-existing "Show QR Code" button above - it hops back to the main thread internally only
                //where it actually needs to (the teleport), so it's left running on THIS async thread rather
                //than nested inside the sync() block above.
                GoogleAuth.showQRCode(user, player);
            }));
        });

        items[22] = new GUIItem(viewRecoveryCodesItem, event -> {
            MAP.remove(player.getUniqueId());
            player.closeInventory();
            //Recovery codes are plaintext, fully-usable-remotely substitutes for the app code (see
            ///recoverycode) - anyone who reads them off an unattended session can log in as this player
            //from anywhere afterward, same severity as reading the password itself. Same gate as the reset
            //button above.
            runGatedByAuthAccess(user, () -> user.getData().loadRecoveryCodes(codes -> {
                //Covers an account that enabled 2FA before recovery codes existed at all, or one that
                //somehow otherwise has none yet - generates them here rather than showing an empty list,
                //since regenerateRecoveryCodes() itself is cheap (in-memory generation + a fire-and-forget
                //DB write) and there's no good reason to make the player go reset their whole 2FA secret
                //just to get backup codes.
                String[] toShow = codes.length > 0 ? codes : user.getData().regenerateRecoveryCodes();
                AlixScheduler.sync(() -> sendRecoveryCodes(player, toShow));
            }));
        });

        items[26] = new GUIItem(applyChangesItem, event -> changes.tryApply(user));
        return items;
    }

    //Runs action immediately if this account already proved app-code access once before (the common case -
    //hasProvenAuthAccess is a persistent, not per-session, flag - see LoginParams) OR if the app currently
    //isn't required at all - there's nothing to protect yet, and these buttons are reachable even while the
    //account is still PASSWORD-only (never went through AuthDataChanges).
    //
    //Checks params.getAuthSettings().requiresAuthApp(), NOT UserTokensFileManager.hasToken() (an earlier,
    //WRONG version of this check, found+fixed 2026-09-23 after a real player hit it on a totally fresh
    //account): since Alix 3.10.0, PersistentUserData.saveToDatabase() eagerly generates+persists a token for
    //EVERY account on its very first save, whether or not the app was ever enabled - so hasToken() is true
    //for virtually every account almost immediately and is not a "was 2FA ever configured" signal at all.
    //Gating on it meant these buttons demanded a code for a secret the player had genuinely never seen -
    //exactly the catch-22 this whole gating mechanism was built to prevent in the first place.
    //Without SOME check here, opening the verification prompt itself (AbstractAuthBuilder's constructor
    //calls data.getToken(), which LAZILY GENERATES a fresh secret as a side effect of just being shown, on
    //top of the one saveToDatabase() already silently created) would still be harmless data-wise but would
    //still demand a code nobody has ever seen - see AuthDataChanges' equivalent reasoning for "first time
    //enabling" the app.
    //Otherwise prompts for the CURRENT code first via the same VerifiedVirtualAuthBuilder GUI/flow
    //AuthDataChanges already uses for changing the auth TYPE, running action only if that's entered correctly.
    private static void runGatedByAuthAccess(VerifiedUser user, Runnable action) {
        LoginParams params = user.getData().getLoginParams();

        if (params.hasProvenAuthAccess() || !params.getAuthSettings().requiresAuthApp()) {
            action.run();
            return;
        }
        user.getDuplexProcessor().verifyAuthAccess(action);
    }

    private static void sendRecoveryCodes(Player player, String[] codes) {
        player.sendMessage(MessageWrapper.parseLegacy(Messages.getWithPrefix("gui-google-auth-recovery-codes-chat-header")));
        for (String code : codes) {
            player.sendMessage(MessageWrapper.parseLegacy(AlixFormatter.translateColors("&e" + code)));
        }
        player.sendMessage(MessageWrapper.parseLegacy(Messages.getWithPrefix("gui-google-auth-recovery-codes-chat-footer")));
    }

    public static void add(Player player) {
        GoogleAuthGUI gui = new GoogleAuthGUI(player);
        MAP.put(player.getUniqueId(), gui);
        player.openInventory(gui.gui);
    }

    private static final class AuthItemType {

        private final ItemStack item;
        private final AuthSetting authSetting;

        private AuthItemType(ItemStack item, AuthSetting authSetting) {
            this.item = item;
            this.authSetting = authSetting;
        }
    }
}