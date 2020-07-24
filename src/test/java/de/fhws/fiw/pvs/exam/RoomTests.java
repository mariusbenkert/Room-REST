package de.fhws.fiw.pvs.exam;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import de.fhws.fiw.pvs.exam.resources.Room;
import de.fhws.fiw.pvs.exam.utils.ObjectIdConverter.ObjectIdJsonConverter;
import de.fhws.fiw.pvs.exam.utils.linkutils.ClientLink;
import okhttp3.*;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.fhws.fiw.pvs.exam.utils.RequestUtil.executeRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoomTests
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

    @Test
    public void testDispatcherLogin()
    {
        final Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        final Response response = executeRequest(request);

        assertEquals("Dispatcher was sucessfully called", 200, response.code());
    }

    @Test
    public void testCreateRoom()
    {
        final String roomLocation = postRoom("testCreateRoom");

        final Request request = new Request.Builder()
                .url(roomLocation)
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        final Response response = executeRequest(request);

        assertEquals("Room was not created!", 200, response.code());
    }

    @Test
    public void testUpdateRoom() throws IOException
    {
        String roomName = "testUpdateRoom";
        /* Create new room */
        String roomLocation = postRoom(roomName);

        /* Get single room that was just created */
        final Request request = new Request.Builder()
                .url(roomLocation)
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        final Response response = executeRequest(request);
        final Room theRoom = genson.deserialize(response.body().string(), Room.class);
        assertEquals(roomName, theRoom.getName());

        /* Update this room */
        theRoom.setName("RoomUpdatedTest");

        final RequestBody putBody = RequestBody.create(JSON, genson.serialize(theRoom));
        final Request updateRequest = new Request.Builder()
                .url(roomLocation)
                .addHeader("Authorization", BearerToken)
                .put(putBody)
                .build();

        final Response updateResponse = executeRequest(updateRequest);

        assertEquals("Room was not updated!", 200, updateResponse.code());
    }

    @Test
    public void testDeleteRoom() throws IOException
    {
        /* Create new room */
        final String roomLocation = postRoom("testDeleteRoom");

        /* Delete this room */
        Request request = new Request.Builder()
                .url(roomLocation)
                .addHeader("Authorization", BearerToken)
                .delete()
                .build();

        Response response = executeRequest(request);

        assertEquals("Room was not deleted!", 204, response.code());

        request = new Request.Builder()
                .url(roomLocation)
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        response = executeRequest(request);

        assertEquals("Room was not deleted!", 404, response.code());
    }

    @Test
    public void testGetCollection() throws IOException
    {

        String roomName1 = "testGetCollection1";
        /* Create new room */
        postRoom(roomName1);

        String roomName2 = "testGetCollection2";
        /* Create new room */
        postRoom(roomName2);

        String roomName3 = "testGetCollection3";
        /* Create new room */
        postRoom(roomName3);

        final Request request = new Request.Builder()
                .url(BASE_URL + "/rooms/")
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        final Response responseGetAll = executeRequest(request);
        String responseBody = responseGetAll.body().string();

        assertTrue("Room was not found!", responseBody.contains("\"name\":\"" + roomName1 + "\""));
        assertTrue("Room was not found!", responseBody.contains("\"name\":\"" + roomName2 + "\""));
        assertTrue("Room was not found!", responseBody.contains("\"name\":\"" + roomName3 + "\""));
        assertEquals("Get request for collection failed!", 200, responseGetAll.code());
    }

    @Test
    public void testGetSingleByName() throws IOException
    {
        String roomName = "testGetSingleByName";
        /* Create new room */
        postRoom(roomName);

        /* Get created room by name */
        final Request request = new Request.Builder()
                .url(BASE_URL + "/rooms/" + roomName)
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        final Response response = executeRequest(request);

        assertTrue("Room was not found!", response.body().string().contains("\"name\":\"" + roomName + "\""));
    }

    @Test
    public void testQueryRoomByName() throws IOException
    {
        String roomName = "testQueryRoomByName";
        /* Create new Room */
        postRoom(roomName);

        /* Query created room by name */
        final Request request = new Request.Builder()
                .url(BASE_URL + "/rooms?q=" + roomName)
                .addHeader("Authorization", BearerToken)
                .get()
                .build();

        final Response response = executeRequest(request);

        assertTrue("Room was not found!", response.body().string().contains("\"name\":\"" + roomName + "\""));
    }

    protected Optional<ClientLink> callDispatcherAndGetHeaderLinkWithRelType(final String relType)
    {
        final Request requestDispatcher = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", BasicAuthDataForTests.BasicAuth)
                .get()
                .build();
        final Response responseDispatcher = executeRequest(requestDispatcher);
        final Optional<ClientLink> link = getResponseHeaderLink(responseDispatcher, relType);

        assertTrue(String.format("No link of relType '%s' found.", relType), link.isPresent());
        return link;
    }

    public Optional<ClientLink> getResponseHeaderLink(final Response response, final String relType)
    {
        return response.headers("Link")
                .stream()
                .map(v -> ClientLink.parseFromHttpHeader(v))
                .filter(l -> l.getRelationType().equalsIgnoreCase(relType))
                .findFirst();
    }
}
