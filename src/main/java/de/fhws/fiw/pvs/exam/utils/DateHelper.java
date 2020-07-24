package de.fhws.fiw.pvs.exam.utils;

import de.fhws.fiw.pvs.exam.dao.ReservationDAO;
import de.fhws.fiw.pvs.exam.resources.Reservation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateHelper
{
    private static final Calendar calendar = GregorianCalendar.getInstance();

    // returns same date with given hour
    // all units smaller than hour set to 0
    public static Date cloneAndSetHour(Date date, int hour)
    {
        calendar.clear();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static Date[] parseFromAndTo(String from, String to)
    {
        Date[] dates = new Date[2];
        if (!from.equals("") && !to.equals(""))
        {
            try
            {
                dates[0] = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(from);
                dates[1] = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(to);
            } catch (ParseException e)
            {
                new WebApplicationException(Response.status(400).build());
            }
        } else if (!from.equals(""))
        {
            try
            {
                dates[0] = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(from);
                dates[1] = DateHelper.cloneAndSetHour(dates[0], ReservationDAO.CLOSE);
            } catch (ParseException e)
            {
                new WebApplicationException(Response.status(400).build());
            }
        } else if (!to.equals(""))
        {
            System.out.println(3);
            try
            {
                dates[1] = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(to);
                dates[0] = DateHelper.cloneAndSetHour(dates[1], ReservationDAO.OPEN);
            } catch (ParseException e)
            {
                new WebApplicationException(Response.status(400).build());
            }
        }

        return dates;
    }
}
