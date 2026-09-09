package org.example.mountains;



//import org.example.mountains.Response;
//import org.example.mountains.SharedResources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*This is the simplified test code - you can use it to check your service actually works
(and if you pass all the tests you will get full marks for the "Function" section of the marking rubric).
However, it does NOT tell you if you are following best practice for: parameters, data types (headers and content),
choice of HTTP operations, or return codes. You need to read the course content carefully to decide how you actually
implement operations to get these marks.

FULL DISCLOSURE - as implied in the marking scheme, the marks for function, HTTP operations, return codes, parameters,
data types will be created automatically. However, I can't share the code for that with you
without essentially telling you exactly what decisions you need to make, and making those decisions is a big part of the
assessment.
 */
public class DemoTests {
    private static final int METHOD_NOT_IMPLEMENTED = 405;
    private static final int NEPAL_ALTITUDE_FILTER = 8400;
    private static final int PEN_Y_FAN_ALTITUDE = 886;
    private static final int CADAIR_IDRIS_ALTITUDE = 893;
    private static final int UPDATED_ANNAPURNA_ALTITUDE = 8091;

    public static void main(String[] args) {
        DemoTests markTests = new DemoTests();
        markTests.runTests();
    }

    private boolean testResult(final Response response, final String expectedResponse) {
        if (response.getResponse().statusCode() == METHOD_NOT_IMPLEMENTED) {
            System.out.println("***Method not implemented");
            return false;
        } else if (!SharedResources.checkResult(expectedResponse, response)) {
            System.out.println("***Incorrect Result");
            return false;
        } else {
            System.out.println("Result match");
            return true;
        }
    }

    private void runTests() {
        MountainConnector connector = new MountainConnector(SharedResources.BASE_URI);

        connector.addMountains(SharedResources.addTestData()).ifPresentOrElse(
                value -> System.out.println("Add Data Success: "
                        + testResult(value, SharedResources.EMPTY_RESULT)), () -> System.out.println("Add Data Failure")
        );

        connector.getAll().ifPresentOrElse(
                value -> System.out.println("Get All Mountains: " + testResult(value, SharedResources.GET_ALL_INITIAL)),
                () -> System.out.println("Get All Mountains Failed")
        );

        connector.getByCountry("Argentina").ifPresentOrElse(
                value -> System.out.println("Get Mountains by Country - Argentina: "
                        + testResult(value, SharedResources.ARGENTINA_INITIAL)),
                () -> System.out.println("Get Mountains by Country - Argentina Failed")
        );

        connector.getByCountryAndRange("Nepal", "Himalayas").ifPresentOrElse(
                value -> System.out.println("Get by Country and Range - Nepal/Himalayas: "
                        + testResult(value, SharedResources.NEPAL_INITIAL)),
                () -> System.out.println("Get by Country and Range - Nepal/Himalayas Failed")
        );

        connector.getByHemisphere(true).ifPresentOrElse(
                value -> System.out.println("Get by Hemisphere (Northern): "
                        + testResult(value, SharedResources.NORTH_INITIAL)),
                () -> System.out.println("Get by Hemisphere (Northern) Failed")
        );

        connector.getByHemisphere(false).ifPresentOrElse(
                value -> System.out.println("Get by Himalayas (Southern): "
                        + testResult(value, SharedResources.SOUTH_INITIAL)),
                () -> System.out.println("Get by Himalayas (Southern) Failed")
        );

        connector.getByCountryAltitude("Nepal", NEPAL_ALTITUDE_FILTER).ifPresentOrElse(
                value -> System.out.println("Get by Country and Altitude - Nepal over 8400: "
                        + testResult(value, SharedResources.NEPAL_OVER_8400)),
                () -> System.out.println("Get by Country and Altitude - Nepal over 8400 Failed")
        );

        connector.getByName("Cymru", "Eryri", "YrWyddfa").ifPresentOrElse(
                value -> System.out.println("Get by Country, Range, Name - YrWyddfa: "
                        + testResult(value, SharedResources.GET_YR_WYDDFA)),
                () -> System.out.println("Get by Country, Range, Name - YrWyddfa Failed")
        );

        Mountain penYFan = new Mountain("PenYFan", PEN_Y_FAN_ALTITUDE, "BannauBrycheiniog", "Wales", true);
        Mountain cadairIdris = new Mountain("CadairIdris", CADAIR_IDRIS_ALTITUDE, "Eryri", "Wales", true);
        List<Mountain> addList = new ArrayList<>();
        addList.add(penYFan);
        addList.add(cadairIdris);

        connector.addMountains(addList).ifPresentOrElse(
                value -> System.out.println("Add new Data - Wales: "
                        + testResult(value, SharedResources.EMPTY_RESULT)),
                () -> System.out.println("Add new Data - Wales Failed")
        );
        connector.getByCountry("Wales").ifPresentOrElse(
                value -> System.out.println("Get by Country - Wales: "
                        + testResult(value, SharedResources.WALES_AFTER_ADDING)),
                () -> System.out.println("Get by Country - Wales Failed")
        );

        connector.addMountains(addList).ifPresentOrElse(
                value -> System.out.println("Add new Data - Wales (again): "
                        + testResult(value, SharedResources.EMPTY_RESULT)),
                () -> System.out.println("Add new Data - Wales (again) Failed")
        );
        connector.getByCountry("Wales").ifPresentOrElse(
                value -> System.out.println("Get by Country - Wales (again): "
                        + testResult(value, SharedResources.WALES_AFTER_ADDING)),
                () -> System.out.println("Get by Country - Wales (again) Failed")
        );

        Mountain updateAnnapurna = new Mountain("Annapurna", UPDATED_ANNAPURNA_ALTITUDE,
                "Annapurna", "Nepal", true);
        Optional<Response> annapurna = connector.getByName("Nepal", "Himalayas", "Annapurna");
        if (annapurna.isEmpty() || annapurna.get().getMountains().isEmpty()) {
            System.out.println("Get Annapurna by Name (to retrieve ID) Failed");
        } else {
            int id = annapurna.get().getMountains().get(0).getId();
            connector.getById(id).ifPresentOrElse(
                    value -> System.out.println("Get by ID - Annapurna: "
                            + testResult(value, SharedResources.ANNAPURNA_INITIAL)),
                    () -> System.out.println("Get by ID - Annapurna Failed"));

            connector.updateMountain(id, updateAnnapurna).ifPresentOrElse(
                    value -> System.out.println("Update Mountain - Annapurna: "
                            + testResult(value, SharedResources.EMPTY_RESULT)),
                    () -> System.out.println("Update Mountain - Annapurna Failed"));

            connector.getByCountry("Nepal").ifPresentOrElse(
                    value -> System.out.println("Get by Country - Nepal (check updated): "
                            + testResult(value, SharedResources.UPDATED_IN_NEPAL)),
                    () -> System.out.println("Get by Country - Nepal (check updated) Failed"));
        }

        Optional<Response> getAntofalla = connector.getByName("Argentina", "Andes", "Antofalla");
        if (getAntofalla.isEmpty() || getAntofalla.get().getMountains().isEmpty()) {
            System.out.println("Get Antofalla by Name (to retrieve ID) Failed");
        } else {
            int id = getAntofalla.get().getMountains().get(0).getId();
            connector.deleteMountain(id).ifPresentOrElse(
                    value -> System.out.println("Deleting Mountain - Antofalla: "
                            + testResult(value, SharedResources.EMPTY_RESULT)),
                    () -> System.out.println("Deleting Mountain - Antofalla Failed"));

            connector.getByCountry("Argentina").ifPresentOrElse(
                    value -> System.out.println("Get by Country - Argentina (check delete): "
                            + testResult(value, SharedResources.UPDATED_IN_ARGENTINA)),
                    () -> System.out.println("Get by Country - Argentina (check delete) Failed"));
        }
    }
}
