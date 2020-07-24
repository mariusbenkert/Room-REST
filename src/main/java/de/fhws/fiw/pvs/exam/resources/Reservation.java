package de.fhws.fiw.pvs.exam.resources;

import com.owlike.genson.annotation.JsonConverter;
import com.owlike.genson.annotation.JsonIgnore;
import de.fhws.fiw.pvs.exam.utils.ObjectIdConverter.ObjectIdXmlConverter;
import de.fhws.fiw.pvs.exam.utils.linkutils.ServerLinkConverter;
import org.bson.types.ObjectId;
import org.glassfish.jersey.linking.InjectLink;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Reservation
{

    @XmlJavaTypeAdapter(ObjectIdXmlConverter.class)
    public ObjectId id;

    public String roomName;
    public Date startTime;
    public Date endTime;

    public String cn;

    public Reservation(String roomName, Date startTime, Date endTime)
    {
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomName = roomName;
    }

    public Reservation()
    {
    }

    public ObjectId getId()
    {
        return id;
    }

    public void setId(ObjectId id)
    {
        this.id = id;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public String getRoomName()
    {
        return roomName;
    }

    public void setRoomName(String roomName)
    {
        this.roomName = roomName;
    }

    public String getCn()
    {
        return cn;
    }

    public void setCn(String cn)
    {
        this.cn = cn;
    }

    @InjectLink(style = InjectLink.Style.ABSOLUTE, value = "rooms/${instance.roomName}/reservations/${instance.id}", type = "application/json", rel = "self")
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    protected Link selfUri;

    @JsonConverter(ServerLinkConverter.class)
    public Link getSelfUri()
    {
        return this.selfUri;
    }

    @JsonIgnore
    public void setSelfUri(final Link selfUri)
    {
        this.selfUri = selfUri;
    }

    @Override
    public String toString()
    {
        return "Reservation{" +
                "id=" + id +
                ", roomName='" + roomName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
