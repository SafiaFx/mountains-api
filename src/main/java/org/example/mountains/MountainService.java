package org.example.mountains;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;

/**
 * Service-layer storage and validation for mountain data.
 *
 * <p>This keeps controller methods close to the Chapter 11 style: the controller chooses HTTP
 * response codes, while the service handles data validation, duplicate detection, and concurrency.
 * A read/write lock is used because the coursework guidance in Chapter 4 explicitly points toward
 * readers/writers style protection for shared state.</p>
 */
@Service
public class MountainService {
    private final Map<Integer, Mountain> mountains = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private int nextId = 1;

    public MountainService() {
    }

    /**
     * Adds a batch of mountains from a JSON request body.
     *
     * <p>This matches the Chapter 10 and 11 guidance for POST: uploaded structured data belongs in
     * the request body, and the server should validate it before mutating shared state.</p>
     *
     * @param newMountains mountains supplied by the client
     * @return {@code true} when the whole batch is accepted, otherwise {@code false}
     */
    public boolean addMountains(final List<Mountain> newMountains) {
        lock.writeLock().lock();
        try {
            if (newMountains == null || newMountains.isEmpty()) {
                return false;
            }
            if (newMountains.stream().anyMatch(mountain -> !isValid(mountain))) {
                return false;
            }

            List<Mountain> copies = new ArrayList<>();
            for (Mountain mountain : newMountains) {
                Mountain copy = new Mountain(
                        mountain.getName(),
                        mountain.getAltitude(),
                        mountain.getRange(),
                        mountain.getCountry(),
                        mountain.getIsNorthern()
                );
                if (mountains.values().stream().anyMatch(copy::equals)
                        || copies.stream().anyMatch(copy::equals)) {
                    return false;
                }
                copies.add(copy);
            }

            for (Mountain copy : copies) {
                copy.setId(nextId++);
                mountains.put(copy.getId(), copy);
            }
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns every stored mountain.
     *
     * @return a defensive copy of all mountains ordered by identifier
     */
    public List<Mountain> getAll() {
        return findByFilter(mountain -> true);
    }

    /**
     * Applies query-style filtering to the stored data set.
     *
     * <p>The controller builds the predicate from optional query parameters, which lines up with
     * Chapter 10's recommendation to use query parameters for filtering rather than for identifying
     * a specific resource.</p>
     *
     * @param predicate filter chosen by the controller
     * @return matching mountains as detached copies
     */
    public List<Mountain> findByFilter(final Predicate<Mountain> predicate) {
        lock.readLock().lock();
        try {
            return mountains.values().stream()
                    .filter(predicate)
                    .sorted(Comparator.comparingInt(Mountain::getId))
                    .map(mountain -> {
                        Mountain copy = new Mountain(
                                mountain.getName(),
                                mountain.getAltitude(),
                                mountain.getRange(),
                                mountain.getCountry(),
                                mountain.getIsNorthern()
                        );
                        copy.setId(mountain.getId());
                        return copy;
                    })
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds one mountain by its identifier.
     *
     * @param id server-assigned identifier
     * @return an {@link Optional} containing a defensive copy when found
     */
    public Optional<Mountain> getById(final int id) {
        lock.readLock().lock();
        try {
            Mountain mountain = mountains.get(id);
            if (mountain == null) {
                return Optional.empty();
            }

            Mountain copy = new Mountain(
                    mountain.getName(),
                    mountain.getAltitude(),
                    mountain.getRange(),
                    mountain.getCountry(),
                    mountain.getIsNorthern()
            );
            copy.setId(mountain.getId());

            return Optional.of(copy);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Replaces the stored representation of one mountain.
     *
     * <p>This supports a PUT-style full update, which matches the Chapter 9 and 10 description of
     * PUT as an idempotent update of an existing item.</p>
     *
     * @param id identifier of the mountain being replaced
     * @param updatedMountain replacement data supplied in the request body
     * @return {@code true} if the update succeeds, otherwise {@code false}
     */
    public boolean updateMountain(final int id, final Mountain updatedMountain) {
        lock.writeLock().lock();
        try {
            if (!mountains.containsKey(id) || !isValid(updatedMountain)) {
                return false;
            }

            Mountain replacement = new Mountain(
                    updatedMountain.getName(),
                    updatedMountain.getAltitude(),
                    updatedMountain.getRange(),
                    updatedMountain.getCountry(),
                    updatedMountain.getIsNorthern()
            );
            replacement.setId(id);

            boolean duplicateExists = mountains.values().stream()
                    .filter(existing -> existing.getId() != id)
                    .anyMatch(replacement::equals);
            if (duplicateExists) {
                return false;
            }

            mountains.put(id, replacement);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Deletes a mountain identified by path parameter.
     *
     * @param id identifier of the mountain to remove
     * @return {@code true} if the item existed and was removed
     */
    public boolean deleteMountain(final int id) {
        lock.writeLock().lock();
        try {
            return mountains.remove(id) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Validates the minimal data contract expected by the service.
     */
    private boolean isValid(final Mountain mountain) {
        return mountain != null
                && hasText(mountain.getName())
                && hasText(mountain.getRange())
                && hasText(mountain.getCountry())
                && mountain.getAltitude() > 0;
    }

    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }
}
