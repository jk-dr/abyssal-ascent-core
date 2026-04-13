package com.jkdr.abyssalascentdimensionpatcher.util;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.Action;
import org.slf4j.Logger;

public class FormattingCore {
    public static final String MOD_ID = "abyssalascentdimensionpatcher";
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Creates a green, underlined "[Click Here]" text that runs a command when clicked.
     */
    public static MutableComponent createCommandClickableComponent(String command) {
        ClickEvent clickEvent = new ClickEvent(Action.RUN_COMMAND, "/" + command);
        return Component.literal("[Click Here]").withStyle((style) -> 
            style.withClickEvent(clickEvent)
                 .withColor(ChatFormatting.GREEN)
                 .withUnderlined(true)
        );
    }

    /**
     * Creates a blue, underlined link component.
     */
    public static MutableComponent createExternalLinkClickableComponent(String url, Boolean displayRawLink) {
        ClickEvent clickEvent = new ClickEvent(Action.OPEN_URL, url);
        String urlText = displayRawLink ? url : "[Open URL]";

        return Component.literal(urlText).withStyle((style) -> 
            style.withClickEvent(clickEvent)
                 .withColor(ChatFormatting.BLUE)
                 .withUnderlined(true)
        );
    }

    /**
     * Returns the standard gray "<Abyssal Ascent>" prefix for chat messages.
     */
    public static MutableComponent createPrefixWithFormatting() {
        return Component.literal("<Abyssal Ascent>").withStyle(ChatFormatting.GRAY);
    }

    /**
     * A custom translation engine. It pulls a string from the server's language file,
     * splits it by "%s", and manually injects arguments to maintain specific formatting
     * for each part of the message.
     */
    public static Component serverTranslate(String key, ChatFormatting format, Object... args) {
        // Gets the raw translation string from the server's locale
        String raw = Language.getInstance().getOrDefault(key);
        String[] parts = raw.split("%s", -1);
        MutableComponent result = Component.literal("");

        for (int i = 0; i < parts.length; ++i) {
            // Append the text part with the specified base format
            result.append(Component.literal(parts[i]).withStyle(format));
            
            // Inject the argument if one exists for this slot
            if (i < args.length) {
                Object arg = args[i];
                if (arg instanceof Component componentArg) {
                    result.append(componentArg);
                } else {
                    result.append(Component.literal(arg.toString()).withStyle(format));
                }
            }
        }

        return result;
    }

    /**
     * Overload for serverTranslate that defaults to white text.
     */
    public static Component serverTranslate(String key, Object... args) {
        return serverTranslate(key, ChatFormatting.WHITE, args);
    }
}