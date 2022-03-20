package tsp.informant.velocity.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import tsp.informant.velocity.Informant;

/**
 * @author TheSilentPro
 */
public class Log {

    private static String name = "&6Informant";

    public static void info(String message) {
        log(LogLevel.INFO, message);
    }

    public static void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public static void error(Throwable ex) {
        log(LogLevel.ERROR, ex);
    }

    public static void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public static void debug(Throwable ex) {
        log(LogLevel.DEBUG, ex);
    }

    public static void log(LogLevel level, String message) {
        if (level == LogLevel.DEBUG && !(boolean)Informant.getInstance().getConfig().get("debug")) {
            return;
        }
        Informant.server.getConsoleCommandSource().sendMessage(Component.text(Utils.colorize("&7[&9&l" + name + "&7] " + level.getColor() + "[" + level.name() + "]: " + message)));
    }

    public static void log(LogLevel level, Throwable ex) {
        if (level == LogLevel.DEBUG && !(boolean)Informant.getInstance().getConfig().get("debug")) {
            return;
        }
        Informant.server.getConsoleCommandSource().sendMessage(Component.text(Utils.colorize("&7[" + name + "&7] " + level.getColor() + "[" + level.name() + "]: " + "&4&l[EXCEPTION]: " + ex.getMessage())));
        Informant.server.getConsoleCommandSource().sendMessage(Component.text(Utils.colorize("&4&l[StackTrace]: " + getStackTrace(ex))));
    }

    public static void setName(String logName) {
        name = logName;
    }

    public static String getName() {
        return name;
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public enum LogLevel {

        INFO,
        WARNING,
        ERROR,
        DEBUG;

        private NamedTextColor getColor() {
            switch (this) {
                case INFO:
                    return NamedTextColor.GREEN;
                case WARNING:
                    return NamedTextColor.YELLOW;
                case ERROR:
                    return NamedTextColor.DARK_RED;
                case DEBUG:
                    return NamedTextColor.DARK_AQUA;
                default:
                    return NamedTextColor.WHITE;
            }
        }

    }

}