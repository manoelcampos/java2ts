package io.github.manoelcampos.java2ts.javadoc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommentCleanerTest {
    @Test
    void cleansComments() {
        assertEquals("See {@link Foo}.\nUse `x < y` and `a`.\nReturns the name", CommentCleaner.clean("  See ,{@link Foo},.\n  Use {@code x < y} and {@literal a}.\n {@return the name}"));
        assertEquals("", CommentCleaner.clean(null));
    }

    @Test
    void removesTagNameFromTagText() {
        assertEquals("Manoel", CommentCleaner.tagText("author", "@author Manoel"));
        assertEquals("Manoel", CommentCleaner.tagText("author", "Manoel"));
    }
}
