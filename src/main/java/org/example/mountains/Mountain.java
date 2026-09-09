package org.example.mountains;


import java.util.Objects;
/**
 * Data transfer object used by both the server and the client.
 *
 * <p>This follows the bean-style approach used in Chapters 11.1 and 11.2: Spring and Jackson can
 * map JSON to and from this class because it has a no-argument constructor plus standard getters
 * and setters.</p>
 *
 * <p>The autograder provides the same public shape for the client side, so field names and access
 * methods should stay stable.</p>
 */
public final class Mountain {
    private int id;
    private String name;
    private int altitude;
    private String range;
    private String country;
    private boolean isNorthern;

    /**
     * Required by Jackson when JSON request bodies are converted into {@code Mountain} objects.
     */
    public Mountain(){}

    public Mountain(final String name, final int altitude, final String range, final String country,
                    final boolean isNorthern) {
        this.id = 0;
        this.setName(name);
        this.setAltitude(altitude);
        this.setRange(range);
        this.setCountry(country);
        this.setNorthern(isNorthern);
    }

    /**
     * Creates a fully populated mountain, including a known identifier.
     *
     * @param id unique identifier assigned by the service
     * @param name mountain name
     * @param altitude height in metres
     * @param range mountain range
     * @param country country name
     * @param isNorthern hemisphere flag
     */
    public Mountain(int id, String name, int altitude, String range, String country, boolean isNorthern) {
        this.id = id;
        this.name = name;
        this.altitude = altitude;
        this.range = range;
        this.country = country;
        this.isNorthern = isNorthern;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public int getAltitude() {
        return altitude;
    }


    public String getRange() {
        return range;
    }

    public String getCountry() {
        return country;
    }

    public boolean getIsNorthern() {
        return isNorthern;
    }

    @Override
    public String toString() {
        return name + " is in the " + range + " range in " + country + ". It is in the "
                + (isNorthern ? "Northern" : "Southern") + " hemisphere and is " + altitude + "m high.";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Mountain mountain)) {
            return false;
        }
        return name.equals(mountain.getName()) && range.equals(mountain.getRange())
                && country.equals(mountain.getCountry());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, range, country);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setNorthern(boolean northern) {
        isNorthern = northern;
    }
}
