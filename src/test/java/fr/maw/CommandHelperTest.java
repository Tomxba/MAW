package fr.maw;

import fr.maw.command.CommandHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommandHelper Unit Tests")
public class CommandHelperTest {

    @Test
    @DisplayName("Should detect flags case-insensitively in argument arrays")
    public void should_detect_flags_case_insensitively() {
        String[] args1 = {"stone", "-u", "-e"};
        String[] args2 = {"dirt", "-A", "-H"};
        String[] args3 = {"cobblestone"};

        assertTrue(CommandHelper.hasFlag(args1, "-u"));
        assertTrue(CommandHelper.hasFlag(args1, "-e"));
        assertFalse(CommandHelper.hasFlag(args1, "-a"));

        assertTrue(CommandHelper.hasFlag(args2, "-a"));
        assertTrue(CommandHelper.hasFlag(args2, "-h"));

        assertFalse(CommandHelper.hasFlag(args3, "-u"));
        assertFalse(CommandHelper.hasFlag(args3, "-e"));
    }

    @Test
    @DisplayName("Should strip flags cleanly from input strings")
    public void should_strip_flags_cleanly() {
        assertEquals("stone", CommandHelper.cleanFlags("stone -u"));
        assertEquals("oak_stairs[facing=north]", CommandHelper.cleanFlags("oak_stairs[facing=north] -u -e"));
        assertEquals("stone,dirt", CommandHelper.cleanFlags("-h stone,dirt -a"));
        assertEquals("", CommandHelper.cleanFlags("-u -e -a -h"));
        assertEquals("", CommandHelper.cleanFlags(null));
    }
}
