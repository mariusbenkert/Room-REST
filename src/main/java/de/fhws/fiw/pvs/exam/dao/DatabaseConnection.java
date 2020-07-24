package de.fhws.fiw.pvs.exam.dao;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.fhws.fiw.pvs.exam.resources.Reservation;
import de.fhws.fiw.pvs.exam.resources.Room;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class DatabaseConnection
{
    private static DatabaseConnection instance;

    CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    MongoClientSettings clientSettings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
            .codecRegistry(pojoCodecRegistry)
            .build();

    MongoClient mongoClient;
    MongoDatabase db;
    MongoCollection<Room> roomCollection;
    MongoCollection<Reservation> reservationCollection;

    private DatabaseConnection()
    {
        try
        {
            mongoClient = MongoClients.create(clientSettings);
            db = mongoClient.getDatabase("pvs-db");
            roomCollection = db.getCollection("rooms", Room.class);
            reservationCollection = db.getCollection("reservations", Reservation.class);
        } catch (Exception e)
        {
            System.out.println("Database Connection failed: \n" + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance()
    {
        if (instance == null)
        {
            instance = new DatabaseConnection();
        }

        return instance;
    }
}
