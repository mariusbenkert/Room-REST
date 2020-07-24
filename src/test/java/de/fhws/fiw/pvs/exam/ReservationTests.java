package de.fhws.fiw.pvs.exam;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import de.fhws.fiw.pvs.exam.resources.Reservation;
import de.fhws.fiw.pvs.exam.resources.Room;
import de.fhws.fiw.pvs.exam.utils.DateHelper;
import de.fhws.fiw.pvs.exam.utils.ObjectIdConverter.ObjectIdJsonConverter;
import okhttp3.*;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.fhws.fiw.pvs.exam.utils.RequestUtil.executeRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReservationTests
{
    private final static String BASE_URL = "http://localhost:8080/exam/api";

    private final static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Genson genson;

    private List<String> roomLocations;

    private String BearerToken;

    @Before
    public void setUp()
    {
        this.genson = new GensonBuilder()
                .setSkipNull(true)
                .useIndentation(true)
                .useDateAsTimestamp(false)
                .useDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))
                .withConverter(new ObjectIdJsonConverter(), ObjectId.class)
                .create();

        roomLocations = new ArrayList<>();

        final Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", BasicAuthDataForTests.BasicAuth)
                .get()
                .build();

        final Response response = executeRequest(request);
        BearerToken = "Bearer " + response.header("X-fhws-jwt-token");
    }

    @After
    public void tearDown()
    {
        for (final String location : roomLocations)
        {
            final Request request = new Request.Builder()
                    .url(location)
                    .addHeader("Authorization", BearerToken)
                    .delete()
                    .build();

            final Response response = executeRequest(request);
        }
    }

    public String postRoom(String roomName)
    {
        Room room = new Room(roomName, 30);

        final RequestBody body = RequestBody.create(JSON, genson.serialize(room));

        final Request request = new Request.Builder()
                .url(BASE_URL + "/rooms")
                .addHeader("Authorization", BearerToken)
                .post(body)
                .build();

        final Response response = executeRequest(request);

        assertTrue("Room was not created!", response.code() == 201);

        final String roomLocation = response.header("Location");
        roomLocations.add(roomLocation);

        return roomLocation;
    }

    public String postReservation(String roomName, int from, int to)
    {
        Reservation reservation = new Reservation(roomName, DateHelper.cloneAndSetHour(new Date(), from), DateHelper.cloneAndSetHour(new Date(), to));

        final RequestBody body = RequestBody.create(JSON, genson.serialize(reservation));

        final Request request = new Request.Builder()
                .url(BASE_URL + "/rooms/" + roomName + "/reservations")
                .addHeader("Authorization", BearerToken)
                .post(body)
                .build();

        final Response response = executeRequest(request);

        assertTrue("Reservation was not created!", response.code() == 201);

        final String reservationLocation = response.header("Location");

        return reservationLocation;
    }

    @Test
    public void getRoomReservations()
    {
        String roomName = "getRoomReservations";
        /* Create new room */
        postRoom(roomName);
        postReservation(roomName, 12, 13);
        postReservation(roomName, 14, 16);

        /* Get created room by name */
        final Request request = new Request.Builder()
                .url(BASE_URL + "/rooms/" + roomName + "/reservations")
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        final Response response = executeRequest(request);

        assertEquals("Get request for collection failed!", 200, response.code());
    }

    @Test
    public void getSingleRoomReservation()
    {
        String roomName = "getSingleRoomReservation";
        /* Create new room */
        postRoom(roomName);
        String location = postReservation(roomName, 12, 13);

        /* Get created room by name */
        final Request request = new Request.Builder()
                .url(location)
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        final Response response = executeRequest(request);

        assertEquals("Get request for collection failed!", 200, response.code());
    }

    @Test
    public void testCreateReservation()
    {
        String roomName = "testCreateReservation";
        postRoom(roomName);
        Reservation reservation = new Reservation(roomName, DateHelper.cloneAndSetHour(new Date(), 12), DateHelper.cloneAndSetHour(new Date(), 13));


        final RequestBody body = RequestBody.create(JSON, genson.serialize(reservation));

        final Request request = new Request.Builder()
                .url(BASE_URL + "/rooms/" + roomName + "/reservations")
                .addHeader("Authorization", BearerToken)
                .post(body)
                .build();

        final Response response = executeRequest(request);

        assertEquals("Reservation was not created!", 201, response.code());
    }

    @Test
    public void testUpdateReservation() throws IOException
    {
        String roomName = "testUpdateReservation";
        /* Create new room */
        postRoom(roomName);
        String location = postReservation(roomName, 12, 13);

        /* Get single room that was just created */
        final Request request = new Request.Builder()
                .url(location)
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        final Response response = executeRequest(request);
        final Reservation theReservation = genson.deserialize(response.body().string(), Reservation.class);
        assertEquals(roomName, theReservation.getRoomName());

        /* Update this room */
        theReservation.setStartTime(DateHelper.cloneAndSetHour(new Date(), 11));

        final RequestBody putBody = RequestBody.create(JSON, genson.serialize(theReservation));
        final Request updateRequest = new Request.Builder()
                .url(location)
                .addHeader("Authorization", BearerToken)
                .put(putBody)
                .build();

        final Response updateResponse = executeRequest(updateRequest);

        assertEquals("Room was not updated!", 200, updateResponse.code());
    }

    @Test
    public void testDeleteReservation() throws IOException
    {
        String roomName = "testDeleteReservation";

        /* Create new room */
        postRoom(roomName);
        String location = postReservation(roomName, 12, 13);

        /* Delete this room */
        Request request = new Request.Builder()
                .url(location)
                .addHeader("Authorization", BearerToken)
                .delete()
                .build();

        Response response = executeRequest(request);

        assertEquals("Room was not deleted!", 204, response.code());

        request = new Request.Builder()
                .url(location)
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        response = executeRequest(request);

        assertEquals("Room was not deleted!", 404, response.code());
    }
}
