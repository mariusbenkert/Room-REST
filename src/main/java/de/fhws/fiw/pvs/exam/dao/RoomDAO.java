package de.fhws.fiw.pvs.exam.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import de.fhws.fiw.pvs.exam.resources.Reservation;
import de.fhws.fiw.pvs.exam.resources.Room;
import de.fhws.fiw.pvs.exam.utils.DateHelper;
import org.bson.types.ObjectId;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class RoomDAO
{
    private static MongoCollection<Room> roomCollection = DatabaseConnection.getInstance().roomCollection;

    public static Collection<Room> loadQuery(int offset, int size, String searchFor,String from, String to, int capacity)
    {
        Date toDate = null;
        Date fromDate = null;

        if(from.equals("") && to.equals(""))
        {
            return roomCollection.find()
                    .filter(and(regex("name", searchFor),gte("capacity",capacity)))
                    .skip(offset)
                    .limit(size)
                    .sort(new BasicDBObject("name", 1))
                    .into(new ArrayList<>());
        }

        Date[] dates = DateHelper.parseFromAndTo(from, to);
        fromDate = dates[0];
        toDate = dates[1];

        return getAllRoomsWithoutOverlappingReservations(offset, size, searchFor, fromDate, toDate, capacity);
    }

    public static ArrayList<Room> getAllRoomsWithoutOverlappingReservations(int offset, int size, String searchFor, Date fromDate,Date toDate, int capacity)
    {
        ArrayList<Reservation> overlappingReservations = ReservationDAO.loadOverlappingReservations(fromDate, toDate);
        ArrayList<String> roomNames = findOccupiedRooms(overlappingReservations);

        return roomCollection
                .find(and(nin("name", roomNames), regex("name", searchFor), gte("capacity",capacity)))
                .sort(new BasicDBObject("name", 1))
                .skip(offset)
                .limit(size)
                .into(new ArrayList<Room>());
    }

    private static ArrayList<String> findOccupiedRooms(ArrayList<Reservation> reservations)
    {
        ArrayList<String> roomNames = new ArrayList<>();

        for (Reservation reservation: reservations)
        {
            roomNames.add(reservation.roomName);
        }

        return roomNames;
    }

    public static Room loadSingleByIdOrName(String roomNameOrId)
    {
        Room foundRoom;
        foundRoom = roomCollection.find(eq("name", roomNameOrId)).first();

        if (ObjectId.isValid(roomNameOrId))
        {
            foundRoom = roomCollection.find(eq("_id", new ObjectId(roomNameOrId))).first();
        }

        return foundRoom;
    }

    public static Room createRoom(Room room)
    {
        if (roomAlreadyExist(room))
        {
            throw new WebApplicationException(Response.status(409).build());
        }

        room.id = ObjectId.get();
        roomCollection.insertOne(room);

        return room;
    }

    public static void updateRoom(String roomId, Room room)
    {
        Room updatedRoom = null;

        if (ObjectId.isValid(roomId))
        {
            updatedRoom = roomCollection.findOneAndUpdate(eq("_id", new ObjectId(roomId)), combine(set("name", room.name), set("capacity", room.capacity)));

            Room roomToUpdate = loadSingleByIdOrName(roomId);

            if (roomToUpdate.getName() != room.getName())
            {
                ReservationDAO.updateNames(roomToUpdate.getName(), room.getName());
            }
        }

        if (updatedRoom == null)
        {
            throw new WebApplicationException(Response.status(404).build());
        }
    }

    public static void deleteRoom(String roomId)
    {
        Room roomToDelete = RoomDAO.loadSingleByIdOrName(roomId);

        if (roomToDelete == null)
        {
            throw new WebApplicationException(Response.status(404).build());
        }

        if (ObjectId.isValid(roomId))
        {
            DeleteResult dr = roomCollection.deleteOne(eq("_id", new ObjectId(roomId)));

            long deleteCount = dr.getDeletedCount();
            if (deleteCount == 0)
            {
                throw new WebApplicationException(Response.status(404).build());
            } else
            {
                ReservationDAO.cleanUpReservations(roomToDelete.getName());
            }
        }

    }

    public static long size(String from, String to, String searchFor)
    {
        Date toDate = null;
        Date fromDate = null;

        if(from.equals("") && to.equals("")) {
            return roomCollection.countDocuments(regex("name", searchFor));
        }

        Date[] dates = DateHelper.parseFromAndTo(from, to);
        fromDate = dates[0];
        toDate = dates[1];

        ArrayList<Reservation> overlappingReservations = ReservationDAO.loadOverlappingReservations(fromDate, toDate);
        ArrayList<String> roomNames = findOccupiedRooms(overlappingReservations);

        return roomCollection.countDocuments(and(nin("name", roomNames), regex("name", searchFor)));
    }

    private static boolean roomAlreadyExist(Room room)
    {
        Room foundRoom = loadSingleByIdOrName(room.getName());

        if (foundRoom == null)
        {
            return false;
        } else {
            return true;
        }
    }
}
