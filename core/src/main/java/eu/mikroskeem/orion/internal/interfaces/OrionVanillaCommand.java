package eu.mikroskeem.orion.internal.interfaces;

import org.bukkit.command.CommandSender;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Mark Vainomaa
 */
public interface OrionVanillaCommand {
    default <T extends Number> T parse(String input, T min, T max, boolean Throws, Function<String, T> parse) {
        T result = min;
        try {
            result = parse.apply(input);
        } catch (NumberFormatException e) {
            if(Throws) {
                throw new NumberFormatException(input + "%s is not a valid number");
            }
        }

        /* TODO: is it good idea to use double values? */
        if(result.doubleValue() < min.doubleValue()) {
            result = min;
        } else if(result.doubleValue() > max.doubleValue()) {
            result = max;
        }

        return result;
    }

    default double getDouble(CommandSender sender, String input, double min, double max, boolean Throws) {
        return parse(input, min, max, Throws, Double::parseDouble);
    }

    default float getFloat(CommandSender sender, String input, float min, float max, boolean Throws) {
        return parse(input, min, max, Throws, Float::parseFloat);
    }
}
