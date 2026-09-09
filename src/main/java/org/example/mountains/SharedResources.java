package org.example.mountains;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
Test data and some methods, not all of which are used by code that I've made available to you.
 */
public class SharedResources {
    private static final int YR_WYDDFA_ALTITUDE = 1085;
    private static final int ACONCAGUA_ALTITUDE = 6961;
    private static final int ANNAPURNA_ALTITUDE = 8091;
    private static final int MAKALU_ALTITUDE = 8485;
    private static final int HUASCARAN_ALTITUDE = 6768;
    private static final int ANTOFALLA_ALTITUDE = 6409;

    //URL of server - use this!
    public static final String BASE_URI = "http://localhost:8080/";

    //Result strings for comparison
    static final String EMPTY_RESULT = ""; //We make this explicit to be clear it should be empty not e.g. null
    static final String GET_ALL_INITIAL = """
            YrWyddfa is in the Eryri range in Cymru. It is in the Northern hemisphere and is 1085m high.
            Snowdon is in the Snowdonia range in Wales. It is in the Northern hemisphere and is 1085m high.
            Aconcagua is in the Andes range in Argentina. It is in the Southern hemisphere and is 6961m high.
            Annapurna is in the Himalayas range in Nepal. It is in the Northern hemisphere and is 8091m high.
            Makalu is in the Himalayas range in Nepal. It is in the Northern hemisphere and is 8485m high.
            Huascaran is in the Andes range in Peru. It is in the Southern hemisphere and is 6768m high.
            Antofalla is in the Andes range in Argentina. It is in the Southern hemisphere and is 6409m high.
            """;
    static final String ARGENTINA_INITIAL = """
            Aconcagua is in the Andes range in Argentina. It is in the Southern hemisphere and is 6961m high.
            Antofalla is in the Andes range in Argentina. It is in the Southern hemisphere and is 6409m high.
            """;
    static final String NEPAL_INITIAL = """
            Annapurna is in the Himalayas range in Nepal. It is in the Northern hemisphere and is 8091m high.
            Makalu is in the Himalayas range in Nepal. It is in the Northern hemisphere and is 8485m high.
            """;
    static final String NORTH_INITIAL = """
            YrWyddfa is in the Eryri range in Cymru. It is in the Northern hemisphere and is 1085m high.
            Snowdon is in the Snowdonia range in Wales. It is in the Northern hemisphere and is 1085m high.
            Annapurna is in the Himalayas range in Nepal. It is in the Northern hemisphere and is 8091m high.
            Makalu is in the Himalayas range in Nepal. It is in the Northern hemisphere and is 8485m high.
            """;
    static final String SOUTH_INITIAL = """
            Aconcagua is in the Andes range in Argentina. It is in the Southern hemisphere and is 6961m high.
            Huascaran is in the Andes range in Peru. It is in the Southern hemisphere and is 6768m high.
            Antofalla is in the Andes range in Argentina. It is in the Southern hemisphere and is 6409m high.
            """;
    static final String NEPAL_OVER_8400 = """
            Makalu is in the Himalayas range in Nepal. It is in the Northern hemisphere and is 8485m high.
            """;
    static final String GET_YR_WYDDFA = """
            YrWyddfa is in the Eryri range in Cymru. It is in the Northern hemisphere and is 1085m high.
            """;
    static final String WALES_AFTER_ADDING = """
            Snowdon is in the Snowdonia range in Wales. It is in the Northern hemisphere and is 1085m high.
            PenYFan is in the BannauBrycheiniog range in Wales. It is in the Northern hemisphere and is 886m high.
            CadairIdris is in the Eryri range in Wales. It is in the Northern hemisphere and is 893m high.
            """;

    static final String UPDATED_IN_NEPAL = """
            Annapurna is in the Annapurna range in Nepal. It is in the Northern hemisphere and is 8091m high.
            Makalu is in the Himalayas range in Nepal. It is in the Northern hemisphere and is 8485m high.
            """;

    static final String UPDATED_IN_ARGENTINA = """
            Aconcagua is in the Andes range in Argentina. It is in the Southern hemisphere and is 6961m high.
            """;


    static final String ANNAPURNA_INITIAL = """
            Annapurna is in the Himalayas range in Nepal. It is in the Northern hemisphere and is 8091m high.
            """;

    /*
   Used to check that the result (and *only* the result - not e.g. the return code, content type or parameters) is
   correct. Checks to see if the appropriate parameter passing methods are used; the appropriate return codes are
   used; and the returned data types are best practice are in the hidden tests.
    */
    static boolean checkResult(final String expectedOutput, final Response response) {
        String resStr = arrayListToString(response.getMountains());
        return expectedOutput.equals(resStr);
        //Can be handy for debugging:
            /*if (!result) {
                System.out.println("Expected:");
                System.out.println(resStr);
                outputResult(response);
            }*/
    }

    /*
    Populate with the appropriate test data
     */
    static ArrayList<Mountain> addTestData() {
        Mountain yrWyddfa = new Mountain("YrWyddfa", YR_WYDDFA_ALTITUDE,
                "Eryri", "Cymru", true);
        Mountain snowden = new Mountain("Snowdon", YR_WYDDFA_ALTITUDE,
                "Snowdonia", "Wales", true);
        Mountain aconcagua = new Mountain("Aconcagua", ACONCAGUA_ALTITUDE, "Andes",
                "Argentina", false);
        Mountain annapurna = new Mountain("Annapurna", ANNAPURNA_ALTITUDE, "Himalayas",
                "Nepal", true);
        Mountain makalu = new Mountain("Makalu", MAKALU_ALTITUDE, "Himalayas", "Nepal",
                true);
        Mountain huascaran = new Mountain("Huascaran", HUASCARAN_ALTITUDE, "Andes", "Peru",
                false);
        Mountain antofalla = new Mountain("Antofalla", ANTOFALLA_ALTITUDE, "Andes", "Argentina",
                false);
        ArrayList<Mountain> addList = new ArrayList<>();
        addList.add(yrWyddfa);
        addList.add(snowden);
        addList.add(aconcagua);
        addList.add(annapurna);
        addList.add(makalu);
        addList.add(huascaran);
        addList.add(antofalla);
        return addList;
    }

    /*
    Convert a list of mountains to a string - will crash if the list is null (so always ensure you return an
    empty list when you don't want to return data).
     */
    private static String arrayListToString(List<Mountain> mountainList) {
        StringBuffer buffer = new StringBuffer();
        for (Mountain mountain : mountainList) {
            buffer.append(mountain).append(System.lineSeparator());
        }
        return buffer.toString();
    }

    /*
    Used to generate individual files containing each individual score for autograder - maybe...
     */
    public static void autograderWriter(final Map<String, Integer> scores) {
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            try (FileWriter scoreFile = new FileWriter(entry.getKey() + ".txt")) {
                scoreFile.write(String.format("%s\n", entry.getValue()));
            } catch(IOException ioe) {
                System.out.println("Failed to write to file: " + entry.getKey());
                System.out.println(ioe.getMessage());
                ioe.printStackTrace();
            }
        }
    }
}
