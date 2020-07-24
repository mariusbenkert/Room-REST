package de.fhws.fiw.pvs.exam.services;

import de.fhws.fiw.pvs.exam.dao.ReservationDAO;
import de.fhws.fiw.pvs.exam.dao.RoomDAO;
import de.fhws.fiw.pvs.exam.resources.Reservation;
import de.fhws.fiw.pvs.exam.resources.Room;
import de.fhws.fiw.pvs.exam.resources.UserData;
import de.fhws.fiw.pvs.exam.security.Secured;
import de.fhws.fiw.pvs.exam.utils.CacheUtil;
import de.fhws.fiw.pvs.exam.utils.Pagination;
import de.fhws.fiw.pvs.exam.utils.linkutils.Hyperlinks;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Collection;

// POST,PUT and DELETE request can be modified to allow certain role access only
// role enum might need extra roles as well

@Path("rooms")
@Secured
public class RoomService
{
    @Context
    protected UriInfo uriInfo;

    @Context
    protected ContainerRequestContext requestContext;

    @Context
    protected Request request;

    @Context
    protected HttpServletRequest httpServletRequest;

    protected final String mediaTypes = "{" + MediaType.APPLICATION_XML + ", " + MediaType.APPLICATION_JSON + "}";

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRooms(@QueryParam("q") @DefaultValue("") String searchFor,
                             @QueryParam("from") @DefaultValue("") final String from,
                             @QueryParam("to") @DefaultValue("") final String to,
                             @QueryParam("capacity") @DefaultValue("0") final int capacity,
                             @QueryParam("size") @DefaultValue("10") final int size,
                             @QueryParam("offset") @DefaultValue("0") final int offset)
    {
        final Collection<Room> roomPage = RoomDAO.loadQuery(offset, size, searchFor, from, to, capacity);

        Response.ResponseBuilder builder = CacheUtil.evaluateEtagCollection(request, requestContext, roomPage, new GenericEntity<Collection<Room>>(roomPage) {});

        Hyperlinks.addLink(uriInfo, builder, "/exam/api/rooms", "createRoom", mediaTypes);
        Hyperlinks.addLink(uriInfo, builder, "/exam/api/rooms/{ROOMNAME}/ID]", "getSingleRoom", mediaTypes);

        return Pagination.paging(offset, size, uriInfo, builder, RoomDAO.size(from, to, searchFor)).build();
    }

    @GET
    @Path("{roomNameOrId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRoomByIdOrName(@PathParam("roomNameOrId") final String filterNameOrId)
    {
        final Room room = RoomDAO.loadSingleByIdOrName(filterNameOrId);

        if (room == null)
        {
            throw new WebApplicationException(Response.status(404).build());
        }

        Response.ResponseBuilder response = CacheUtil.evaluateEtagSingle(request, requestContext, room);

        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/" + filterNameOrId, "updateRoom", mediaTypes);
        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/" + filterNameOrId, "deleteRoom", null);
        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/" + filterNameOrId+"/reservations", "getAllReservationsForRoom", mediaTypes);

        return response.build();
    }

    @POST
    // @Secured(Role.employee) for employee access only
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createRoom(final Room room)
    {
        if (room == null)
        {
            throw new WebApplicationException(Response.status(404).build());
        }

        RoomDAO.createRoom(room);

        final URI roomURI = uriInfo.getAbsolutePathBuilder().path(room.id.toString()).build();

        return Response.created(roomURI).build();
    }

    @PUT
    // @Secured(Role.employee) for employee access only
    @Path("{roomId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateRoom(@PathParam("roomId") final String roomId, final Room room)
    {
        Room roomToUpdate = RoomDAO.loadSingleByIdOrName(roomId);

        EntityTag eTag = new EntityTag(Integer.toString(roomToUpdate.toString().hashCode()));

        Response.ResponseBuilder response = request.evaluatePreconditions(eTag);

        if (response == null)
        {
            RoomDAO.updateRoom(roomId, room);
            response = Response.ok();
        }

        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/" + roomId, "self", mediaTypes);

        return response.build();
    }

    @DELETE
    // @Secured(Role.employee) for employee access only
    @Path("{roomId}")
    public Response deleteRoom(@PathParam("roomId") final String roomId)
    {
        RoomDAO.deleteRoom(roomId);

        Response.ResponseBuilder response = Response.noContent();

        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms", "getAllRooms", mediaTypes);

        return response.build();
    }

    // reservations from here on

    @GET
    @Path("{roomName}/reservations")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRoomReservations(
            @PathParam("roomName") final String roomName,
            @QueryParam("size") @DefaultValue("10") final int size,
            @QueryParam("offset") @DefaultValue("0") final int offset)
    {
        final Collection<Reservation> reservationPage;
        reservationPage = ReservationDAO.loadReservationsForRoom(offset, size, roomName);

        Response.ResponseBuilder response = CacheUtil.evaluateEtagCollection(request, requestContext, reservationPage, new GenericEntity<Collection<Reservation>>(reservationPage) {});

        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/"+roomName+"/reservations", "createReservation", mediaTypes);

        return Pagination.paging(offset, size, uriInfo, response, ReservationDAO.size(roomName)).build();
    }

    @GET
    @Path("{roomName}/reservations/{Id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSingleReservation(
            @PathParam("roomName") final String roomName,
            @PathParam("Id") final String reservationId)
    {
        final Reservation reservation = ReservationDAO.loadSingleById(reservationId);

        if (reservation == null)
        {
            throw new WebApplicationException(Response.status(404).build());
        }

        if (!reservation.roomName.equals(roomName))
        {
            throw new WebApplicationException(Response.status(404).build());
        }

        Response.ResponseBuilder response = CacheUtil.evaluateEtagSingle(request, requestContext, reservation);

        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/" + roomName + "/reservations/" + reservationId, "updateReservation", mediaTypes);
        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/" + roomName + "/reservations/" + reservationId, "deleteReservation", null);

        return response.build();
    }

    @POST
    @Secured
    @Path("{roomName}/reservations")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createReservation(@PathParam("roomName") final String roomName, final Reservation reservation)
    {
        if (reservation == null)
        {
            throw new WebApplicationException(Response.status(400).build());
        }
        reservation.setRoomName(roomName);

        ReservationDAO.createReservation(reservation, (UserData) httpServletRequest.getAttribute("userData"));

        final URI reservationURI = uriInfo.getAbsolutePathBuilder().path(reservation.id.toString()).build();

        return Response.created(reservationURI).build();
    }

    @PUT
    @Path("{roomName}/reservations/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateReservation(@PathParam("roomName") final String roomName,
                                      @PathParam("id") final String reservationId,
                                      final Reservation reservation)
    {
        if (reservation == null)
        {
            throw new WebApplicationException(Response.status(404).build());
        }

        reservation.setRoomName(roomName);

        Reservation reservationToUpdate = ReservationDAO.loadSingleById(reservationId);

        EntityTag eTag = new EntityTag(Integer.toString(reservationToUpdate.toString().hashCode()));

        Response.ResponseBuilder response = request.evaluatePreconditions(eTag);

        if (response == null)
        {
            ReservationDAO.updateReservation(reservation, reservationToUpdate, reservationId, (UserData) httpServletRequest.getAttribute("userData"));
            response = Response.ok();
        }

        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/" + roomName+ "/reservations/" + reservationId, "self", mediaTypes);

        return response
                .tag(eTag)
                .build();
    }

    @DELETE
    @Path("{roomName}/reservations/{Id}")
    public Response deleteReservation(@PathParam("roomName") final String roomName,
                                      @PathParam("Id") final String reservationId)
    {
        ReservationDAO.deleteReservation(reservationId, (UserData) httpServletRequest.getAttribute("userData"));

        Response.ResponseBuilder response = Response.noContent();

        Hyperlinks.addLink(uriInfo, response, "/exam/api/rooms/" + roomName + "/reservations", "getAllReservations", mediaTypes);

        return response.build();
    }
}
