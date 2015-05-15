package net.minecraft.launcher;

import java.net.URI;
import java.net.URISyntaxException;

public class LauncherConstants {
    public static final String VERSION_NAME = "1.0"; //Version du Launcher
    public static final int VERSION_NUMERIC = 9;
    public static final String DEFAULT_PROFILE_NAME = "";
    public static final String SERVER_NAME = "HysteriaCraft";
    public static final URI URL_REGISTER = constantURI("https://account.mojang.com/register");
    public static final String URL_DOWNLOAD_BASE = "http://hysteria-gaming.org/launcher/"; //Dossier site.com/launcher/
    public static final boolean useModResource = false;
    public static final String URL_DOWNLOAD_MODS = "http://hysteria-gaming.org/launcher/resource/"; // http://site.com/ressource/
    public static final String URL_RESOURCE_BASE = "http://hysteria-gaming.org/launcher/resource/"; // http://site.com/ressource/
    public static final String LIBRARY_DOWNLOAD_BASE = "http://hysteria-gaming.org/launcher/resource/"; // http://site.com/ressource/
    public static final String URL_BLOG = "Page Accueil OU Tumblr";
    public static final String URL_STATUS_CHECKER = "http://status.mojang.com/check";
    public static final String URL_BOOTSTRAP_DOWNLOAD = http://hysteria-gaming.org/launcher/bootstrap.jar""; //http://site.com/launcher/bootstrap.jar
    public static final URI URL_FORGOT_USERNAME = constantURI("http://help.mojang.com/customer/portal/articles/1233873");
    public static final URI URL_FORGOT_PASSWORD_MINECRAFT = constantURI("http://help.mojang.com/customer/portal/articles/329524-change-or-forgot-password");
    public static final URI URL_FORGOT_MIGRATED_EMAIL = constantURI("http://help.mojang.com/customer/portal/articles/1205055-minecraft-launcher-error---migrated-account");

    public static URI constantURI(final String input) {
        try {
            return new URI(input);
        }
        catch(final URISyntaxException e) {
            throw new Error(e);
        }
    }
}