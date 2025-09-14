package com.cubeyu.ctools.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtils {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    /**
     * 将带颜色代码的字符串转换为Component
     * @param text 原始文本
     * @return 转换后的Component
     */
    public static Component colorize(String text) {
        return SERIALIZER.deserialize(text);
    }

    /**
     * 替换文本中的占位符
     * @param text 原始文本
     * @param placeholders 占位符键值对
     * @return 替换后的文本
     */
    public static String replacePlaceholders(String text, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("占位符必须是键值对形式");
        }
        
        String result = text;
        for (int i = 0; i < placeholders.length; i += 2) {
            result = result.replace(placeholders[i], placeholders[i + 1]);
        }
        return result;
    }
} 