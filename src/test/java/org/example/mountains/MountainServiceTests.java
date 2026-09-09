package org.example.mountains;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class MountainServiceTests {
    private final MountainService service = new MountainService();

    private Mountain mountain(String name) {
        return new Mountain(name, 1000, "Example Range", "Wales", true);
    }

    @Test
    void invalidBatchIsRejectedWithoutPartialWrites() {
        assertFalse(service.addMountains(List.of(mountain("Valid"), new Mountain("", 0, "", "", false))));
        assertTrue(service.getAll().isEmpty());
        assertTrue(service.addMountains(List.of(mountain("First"))));
        assertEquals(1, service.getAll().get(0).getId());
    }

    @Test
    void rejectsEmptyNullAndNullEntryBatches() {
        assertFalse(service.addMountains(null));
        assertFalse(service.addMountains(List.of()));
        List<Mountain> withNull = new ArrayList<>();
        withNull.add(null);
        assertFalse(service.addMountains(withNull));
    }

    @Test
    void duplicatesWithinBatchAndExistingCollectionAreAtomic() {
        assertFalse(service.addMountains(List.of(mountain("One"), mountain("One"))));
        assertTrue(service.getAll().isEmpty());
        assertTrue(service.addMountains(List.of(mountain("One"))));
        assertFalse(service.addMountains(List.of(mountain("Two"), mountain("One"))));
        assertEquals(1, service.getAll().size());
    }

    @Test
    void storedDataIsDetachedFromInputsAndReturnedValues() {
        Mountain input = mountain("Original");
        assertTrue(service.addMountains(List.of(input)));
        input.setName("Changed input");
        service.getAll().get(0).setName("Changed list");
        service.getById(1).orElseThrow().setName("Changed detail");
        assertEquals("Original", service.getById(1).orElseThrow().getName());
    }

    @Test
    void updateCopiesInputAndRejectsDuplicateIdentity() {
        service.addMountains(List.of(mountain("One"), mountain("Two")));
        assertFalse(service.updateMountain(2, mountain("One")));
        assertEquals("Two", service.getById(2).orElseThrow().getName());
        Mountain update = mountain("Updated");
        update.setId(999);
        assertTrue(service.updateMountain(2, update));
        update.setName("Changed input");
        assertEquals("Updated", service.getById(2).orElseThrow().getName());
        assertEquals(2, service.getById(2).orElseThrow().getId());
    }

    @Test
    void invalidUpdateLeavesDataUnchanged() {
        service.addMountains(List.of(mountain("One")));
        assertFalse(service.updateMountain(1, new Mountain()));
        assertFalse(service.updateMountain(999, mountain("Other")));
        assertEquals("One", service.getById(1).orElseThrow().getName());
    }

    @Test
    void deletionDoesNotReuseIds() {
        service.addMountains(List.of(mountain("One")));
        assertTrue(service.deleteMountain(1));
        assertFalse(service.deleteMountain(1));
        service.addMountains(List.of(mountain("Two")));
        assertEquals(2, service.getAll().get(0).getId());
    }

    @Test
    void concurrentWritersReceiveUniqueIds() throws Exception {
        var pool = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                final String name = "Mountain " + i;
                tasks.add(() -> service.addMountains(List.of(mountain(name))));
            }
            for (var result : pool.invokeAll(tasks)) {
                assertTrue(result.get());
            }
            assertEquals(40, service.getAll().size());
            assertEquals(40, service.getAll().stream().map(Mountain::getId).distinct().count());
        } finally {
            pool.shutdownNow();
        }
    }
}
