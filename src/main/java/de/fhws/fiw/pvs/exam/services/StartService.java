package de.fhws.fiw.pvs.exam.services;

import de.fhws.fiw.pvs.exam.utils.linkutils.Hyperlinks;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.*;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("")
public class StartService
{
    @Context
    protected UriInfo uriInfo;

    @Context
    HttpHeaders httpheaders;

    protected final String mediaTypes = "{" + MediaType.APPLICATION_XML + ", " + MediaType.APPLICATION_JSON + "}";

    @GET
    public Response getDispatcher()
    {

        okhttp3.Response fhwsResponse = basicAuthAgainstFhwsApi();

        final Response.ResponseBuilder response = Response.ok();

        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/?q={Q}&from={FROM}&to={TO}&capacity={CAPACITY}&size={SIZE}&offset={OFFSET}", "getRooms", mediaTypes);
        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/", "getAllRooms", mediaTypes);

        return response
                .header("X-fhws-jwt-token", fhwsResponse.header("X-fhws-jwt-token"))
                .build();
    }

    private okhttp3.Response basicAuthAgainstFhwsApi()
    {
        OkHttpClient client = new OkHttpClient();

        final String authorizationString = httpheaders.getHeaderString("Authorization");

        if (authorizationString == null)
        {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        Request request = new Request.Builder()
                .url("https://api.fiw.fhws.de/auth/api/users/me")
                .get()
                .header("Authorization", httpheaders.getHeaderString("Authorization"))
                .build();

        okhttp3.Response response;
        try
        {
            response = client.newCall(request).execute();
        } catch (final IOException e)
        {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }

        if (response.code() == 401)
        {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return response;
    }
}
