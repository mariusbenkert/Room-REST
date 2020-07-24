package de.fhws.fiw.pvs.exam.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import de.fhws.fiw.pvs.exam.resources.Reservation;
import de.fhws.fiw.pvs.exam.resources.UserData;
import org.bson.types.ObjectId;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class ReservationDAO
{
    public static final int OPEN = 8;
    public static final int CLOSE = 22;

    public static Calendar calendar = GregorianCalendar.getInstance();
    private static MongoCollection<Reservation> reservationCollection = DatabaseConnection.getInstance().reservationCollection;

    public static Collection<Reservation> loadReservationsForRoom(int offset, int size, String roomName)
    {
        return reservationCollection.find()
                .filter(eq("roomName", roomName))
                .skip(offset)
                .limit(size)
                .sort(new BasicDBObject("roomName", 1))
                .into(new ArrayList<>());
    }

    public static Reservation loadSingleById(String id)
    {
        Reservation foundReservation = null;

        if (ObjectId.isValid(id))
        {
            foundReservation = reservationCollection.find(eq("_id", new ObjectId(id))).first();
        }
        return foundReservation;
    }

    public static Reservation createReservation(Reservation reservation, UserData userData)
    {
        // Validity and overlap check
        checkReservationForOverlapsAndValidity(reservation);

        // is given room valid?
        if (!isGivenRoomValid(reservation))
        {
            throw new WebApplicationException(Response.status(409).build());
        }

        reservation.setId(ObjectId.get());
        reservation.setCn(userData.getCn());
        reservationCollection.insertOne(reservation);
        return reservation;
    }


    public static void updateReservation(Reservation reservation, Reservation reservationToUpdate, String reservationId, UserData userData)
    {
        if (reservation.getRoomName() == null)
        {
            reservation.setRoomName(reservationToUpdate.getRoomName());
        }
        // Validity and overlap check
        checkReservationForOverlapsAndValidity(reservation);

        // is given room valid?
        if (!isGivenRoomValid(reservation))
        {
            throw new WebApplicationException(Response.status(409).build());
        }

        if (!reservationToUpdate.getCn().equals(userData.getCn()))
        {
            throw new WebApplicationException(Response.status(403).build());
        }

        Reservation updatedReservation = null;
        if (ObjectId.isValid(reservationId))
        {
            updatedReservation = reservationCollection
                    .findOneAndUpdate(eq("_id"
                            , new ObjectId(reservationId))
                            , combine(set("roomName", reservation.roomName)
                                    , set("startTime", reservation.startTime)
                                    , set("endTime", reservation.endTime)));
        }

        if (updatedReservation == null)
        {
            throw new WebApplicationException(Response.status(404).build());
        }
    }

    public static void deleteReservation(String reservationId, UserData userData)
    {
        Reservation reservationToDelete = ReservationDAO.loadSingleById(reservationId);

        if (reservationToDelete == null)
        {
            throw new WebApplicationException(Response.status(404).build());
        }

        if (!reservationToDelete.getCn().equals(userData.getCn()))
        {
            throw new WebApplicationException(Response.status(403).build());
        }

        if (ObjectId.isValid(reservationId))
        {
            reservationCollection
                    .deleteOne(eq("_id", new ObjectId(reservationId)));
        }
    }

    public static long size(String roomName)
    {
        return reservationCollection.countDocuments(eq("roomName", roomName));
    }

    public static void updateNames(String oldName, String updatedName)
    {
        reservationCollection.updateMany(eq("roomName", oldName), set("roomName", updatedName));
    }

    public static void cleanUpReservations(String roomName)
    {
        reservationCollection.deleteMany(eq("roomName", roomName));
    }

    private static boolean isGivenRoomValid(Reservation reservation)
    {
        return RoomDAO.loadSingleByIdOrName(reservation.getRoomName()) != null;
    }


    private static boolean checkReservationForOverlapsAndValidity(Reservation reservation)
    {
        Calendar calendar = GregorianCalendar.getInstance();

        calendar.setTime(reservation.startTime);
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        int startDay = calendar.get(Calendar.DAY_OF_YEAR);
        int startYear = calendar.get(Calendar.YEAR);

        calendar.setTime(reservation.endTime);
        int endHour = calendar.get(Calendar.HOUR_OF_DAY);
        int endDay = calendar.get(Calendar.DAY_OF_YEAR);
        int endYear = calendar.get(Calendar.YEAR);


        // check if reservation start and end is on the same day
        if (startYear != endYear || startDay != endDay)
        {
            throw new WebApplicationException(Response.status(409).entity("Reservation needs to start and end on the same day!").build());
        }

        //check opening hours
        if (OPEN > startHour || CLOSE < endHour)
        {
            throw new WebApplicationException(Response.status(409).entity("Reservation can only be between 8 and 22!").build());
        }

        //check start end order
        if (reservation.startTime.after(reservation.endTime))
        {
            throw new WebApplicationException(Response.status(409).entity("StartTime should be before EndTime!").build());
        }

        HashSet<Reservation> collidingRes = reservationCollection
                .find(and(
                        eq("roomName", reservation.roomName)
                        , gte("endTime", reservation.startTime)
                        , lte("startTime", reservation.endTime)
                        , ne("_id", reservation.id)
                ))
                .into(new HashSet<>());

        if (!collidingRes.isEmpty())
        {
            throw new WebApplicationException(Response.status(409).entity("Overlaps with one or more existing Reservations!").entity(collidingRes).build());
        }

        return false;
    }

    public static ArrayList<Reservation> loadOverlappingReservations(Date from, Date to)
    {
        ArrayList<Reservation> reservations = reservationCollection.
                find(and(lte("startTime", to), gte("endTime", from)))
                .sort(new BasicDBObject("startTime", 1))
                .into(new ArrayList<>());

        System.out.println(reservations);
        return reservations;
    }
}
