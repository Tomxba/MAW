package fr.maw.pattern;

import net.minestom.server.instance.block.Block;

import java.util.*;

/**
 * Parser for Pattern and Mask expressions.
 */
public final class PatternParser {

    private PatternParser() {}

    /**
     * Parses a pattern string (e.g. "stone", "oak_stairs[facing=north]", "50%stone,50%dirt").
     */
    public static Pattern parsePattern(String input) throws IllegalArgumentException {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Pattern string cannot be empty");
        }

        String trimmed = input.trim();

        // Check for comma-separated weighted pattern: 50%stone,50%dirt or stone,dirt
        if (trimmed.contains(",") && !isInsideBrackets(trimmed, ',')) {
            String[] parts = splitRespectingBrackets(trimmed, ',');
            RandomPattern.Builder builder = RandomPattern.builder();

            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;

                double weight = 1.0;
                String blockPart = part;

                int percentIdx = part.indexOf('%');
                if (percentIdx > 0) {
                    String weightStr = part.substring(0, percentIdx).trim();
                    try {
                        weight = Double.parseDouble(weightStr);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid weight number: " + weightStr);
                    }
                    blockPart = part.substring(percentIdx + 1).trim();
                }

                Block block = parseBlock(blockPart);
                builder.add(block, weight);
            }
            return builder.build();
        }

        // Single percentage prefix on a single block e.g. "100%stone"
        int percentIdx = trimmed.indexOf('%');
        if (percentIdx > 0 && !isInsideBrackets(trimmed, '%')) {
            trimmed = trimmed.substring(percentIdx + 1).trim();
        }

        return new SingleBlockPattern(parseBlock(trimmed));
    }

    /**
     * Parses a mask string (e.g. "#air", "#existing", "!stone", "stone,dirt").
     */
    public static Mask parseMask(String input) throws IllegalArgumentException {
        if (input == null || input.isBlank()) {
            return Masks.ALWAYS_TRUE;
        }

        String trimmed = input.trim();

        // Negation
        if (trimmed.startsWith("!")) {
            return parseMask(trimmed.substring(1)).negate();
        }

        // Standard tags
        if (trimmed.equalsIgnoreCase("#air") || trimmed.equalsIgnoreCase("air")) {
            return Masks.AIR;
        }
        if (trimmed.equalsIgnoreCase("#existing") || trimmed.equalsIgnoreCase("#solid")) {
            return Masks.EXISTING;
        }

        // Multi-block mask: stone,dirt
        if (trimmed.contains(",") && !isInsideBrackets(trimmed, ',')) {
            String[] parts = splitRespectingBrackets(trimmed, ',');
            List<Block> targetBlocks = new ArrayList<>();
            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;
                targetBlocks.add(parseBlock(part));
            }
            return Masks.ofBlocks(targetBlocks);
        }

        // Single block mask
        Block target = parseBlock(trimmed);
        return Masks.ofBlock(target);
    }

    /**
     * Parses a Block with optional state properties like "oak_stairs[facing=north,half=top]".
     */
    public static Block parseBlock(String input) throws IllegalArgumentException {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Block name cannot be empty");
        }

        String trimmed = input.trim().toLowerCase(Locale.ROOT);

        int bracketStart = trimmed.indexOf('[');
        int bracketEnd = trimmed.indexOf(']');

        String blockName = trimmed;
        Map<String, String> properties = Collections.emptyMap();

        if (bracketStart > 0 && bracketEnd > bracketStart) {
            blockName = trimmed.substring(0, bracketStart).trim();
            String propsStr = trimmed.substring(bracketStart + 1, bracketEnd).trim();
            properties = parseProperties(propsStr);
        }

        if (!blockName.contains(":")) {
            blockName = "minecraft:" + blockName;
        }

        Block block = Block.fromKey(blockName);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block: '" + blockName + "'");
        }

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            try {
                block = block.withProperty(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid property '" + entry.getKey() + "=" + entry.getValue() + "' for block " + blockName);
            }
        }

        return block;
    }

    private static Map<String, String> parseProperties(String propsStr) {
        if (propsStr.isEmpty()) return Collections.emptyMap();

        Map<String, String> map = new HashMap<>();
        String[] pairs = propsStr.split(",");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String key = pair.substring(0, eq).trim();
                String value = pair.substring(eq + 1).trim();
                map.put(key, value);
            }
        }
        return map;
    }

    private static boolean isInsideBrackets(String s, char target) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') depth--;
            else if (c == target && depth > 0) return true;
        }
        return false;
    }

    private static String[] splitRespectingBrackets(String s, char delimiter) {
        List<String> list = new ArrayList<>();
        int depth = 0;
        int lastIdx = 0;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') depth--;
            else if (c == delimiter && depth == 0) {
                list.add(s.substring(lastIdx, i));
                lastIdx = i + 1;
            }
        }
        list.add(s.substring(lastIdx));
        return list.toArray(new String[0]);
    }
}
