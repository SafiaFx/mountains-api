package org.example.mountains;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the mountain API.
 *
 * <p>The endpoint design follows the lecture guidance from Chapters 9 to 11:
 * GET with path parameters for resource location and query parameters for filtering,
 * GET with a path parameter for a unique item, POST and PUT with JSON request bodies,
 * and DELETE with a path parameter.</p>
 */
@RestController
@RequestMapping("/mountains")
public class MountainController {
    private final MountainService mountainService;

    public MountainController(final MountainService mountainService) {
        this.mountainService = mountainService;
    }

    /**
     * Creates one or more new mountains from a JSON request body.
     *
     * <p>Using {@link RequestBody} here matches Chapter 10.9 and Chapter 11.2: uploaded structured
     * data should be sent in the body as JSON rather than forced into query parameters.</p>
     */
    @PostMapping
    public ResponseEntity<Void> addMountains(@RequestBody final List<Mountain> mountains) {
        return mountainService.addMountains(mountains)
                ? ResponseEntity.status(HttpStatus.CREATED).build()
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    /**
     * Reads the whole collection, optionally filtered by collection-wide query parameters.
     */
    @GetMapping
    public ResponseEntity<List<Mountain>> getMountains(
            @RequestParam(required = false) final Boolean isNorthern,
            @RequestParam(required = false) final Integer minAltitude) {
        return filterResponse(null, null, null, isNorthern, minAltitude);
    }

    /**
     * Reads mountains located within one country.
     *
     * <p>Country is treated as part of the resource location, so it is carried in the path rather
     * than as a query parameter.</p>
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<List<Mountain>> getByCountry(@PathVariable final String country,
                                                       @RequestParam(required = false) final Boolean isNorthern,
                                                       @RequestParam(required = false) final Integer minAltitude) {
        return filterResponse(country, null, null, isNorthern, minAltitude);
    }

    /**
     * Reads mountains located within one country and one range.
     */
    @GetMapping("/country/{country}/range/{range}")
    public ResponseEntity<List<Mountain>> getByCountryAndRange(@PathVariable final String country,
                                                               @PathVariable final String range,
                                                               @RequestParam(required = false)
                                                               final Boolean isNorthern,
                                                               @RequestParam(required = false)
                                                               final Integer minAltitude) {
        return filterResponse(country, range, null, isNorthern, minAltitude);
    }

    /**
     * Reads one named mountain within a country and range hierarchy.
     */
    @GetMapping("/country/{country}/range/{range}/name/{name}")
    public ResponseEntity<List<Mountain>> getByName(@PathVariable final String country,
                                                    @PathVariable final String range,
                                                    @PathVariable final String name) {
        return filterResponse(country, range, name, null, null);
    }

    /**
     * Reads a single mountain identified by its path parameter.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Mountain> getById(@PathVariable final int id) {
        Optional<Mountain> mountain = mountainService.getById(id);
        return mountain.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Replaces the data for an existing mountain.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMountain(@PathVariable final int id,
                                               @RequestBody final Mountain mountain) {
        if (mountainService.getById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return mountainService.updateMountain(id, mountain)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    /**
     * Deletes a mountain identified by its path parameter.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMountain(@PathVariable final int id) {
        return mountainService.deleteMountain(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private boolean matches(final String expected, final String actual) {
        return expected == null || expected.equals(actual);
    }

    private ResponseEntity<List<Mountain>> filterResponse(final String country, final String range,
                                                          final String name, final Boolean isNorthern,
                                                          final Integer minAltitude) {
        List<Mountain> result = mountainService.findByFilter(mountain ->
                matches(country, mountain.getCountry())
                        && matches(range, mountain.getRange())
                        && matches(name, mountain.getName())
                        && (isNorthern == null || mountain.getIsNorthern() == isNorthern)
                        && (minAltitude == null || mountain.getAltitude() > minAltitude));
        return result.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of())
                : ResponseEntity.ok(result);
    }
}
