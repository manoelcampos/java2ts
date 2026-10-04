package io.github.manoelcampos.java2ts.scan;

import io.github.manoelcampos.java2ts.config.ClassSelection;
import io.github.manoelcampos.java2ts.fixtures.Entity;
import io.github.manoelcampos.java2ts.fixtures.Person;
import io.github.manoelcampos.java2ts.fixtures.selection.Animal;
import io.github.manoelcampos.java2ts.fixtures.selection.Cat;
import io.github.manoelcampos.java2ts.fixtures.selection.Dog;
import io.github.manoelcampos.java2ts.fixtures.selection.Pet;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassSelectorTest {
    private static final String SELECTION_PKG = "io.github.manoelcampos.java2ts.fixtures.selection";

    private static Set<Class<?>> select(final ClassSelection selection) {
        return new ClassSelector(selection, ClassPathContext.ofCurrentClassPath()).select();
    }

    @Test
    void selectsByPatternExcludingAnnotationsAndNestedClassesForSingleStar() {
        assertEquals(Set.of(Animal.class, Cat.class, Dog.class, Pet.class), select(ClassSelection.ofPatterns(List.of(SELECTION_PKG + ".*"))));
    }

    @Test
    void selectsNestedClassesWithDoubleStar() {
        assertTrue(select(ClassSelection.ofPatterns(List.of(SELECTION_PKG + ".**"))).contains(Dog.Puppy.class));
    }

    @Test
    void selectsExplicitClassesAndAppliesExclusions() {
        final var selection = ClassSelection.of(
            List.of(Person.class.getName(), Dog.class.getName()), List.of(SELECTION_PKG + ".*"),
            List.of(Cat.class.getName()), List.of("**.Pet"), null, null, null);

        assertEquals(List.of(Person.class, Animal.class, Dog.class), List.copyOf(select(selection)));
    }

    @Test
    void failsWhenExplicitClassIsNotFound() {
        final var selection = ClassSelection.of(List.of("com.inexistent.Foo"), null, null, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> select(selection));
    }

    @Test
    void selectsByAnnotation() {
        final var selected = select(ClassSelection.of(null, null, null, null, List.of(Entity.class.getName()), null, null));
        assertEquals(Set.of(Person.class, Cat.class), selected);
    }

    @Test
    void selectsByImplementedInterfaceAndExtendedClass() {
        final var byInterface = select(ClassSelection.of(null, null, null, null, null, List.of(Pet.class.getName()), null));
        assertEquals(Set.of(Cat.class), byInterface);

        final var bySuperclass = select(ClassSelection.of(null, null, null, null, null, null, List.of(Animal.class.getName())));
        assertEquals(Set.of(Dog.class, Dog.Puppy.class), bySuperclass);
    }

    @Test
    void emptySelectionSelectsNothing() {
        assertTrue(select(ClassSelection.EMPTY).isEmpty());
        assertFalse(ClassSelection.EMPTY.requiresTypeScan());
    }

    @Test
    void ignoresClassesThatCannotBeLoaded() {
        final var loading = new ClassLoading(getClass().getClassLoader());
        assertTrue(loading.tryLoad("com.inexistent.Foo").isEmpty());
        assertFalse(ClassSelector.isConvertible(int[].class));
        assertFalse(ClassSelector.isConvertible(int.class));
        assertFalse(ClassSelector.isConvertible(new Object(){}.getClass()));
    }
}
