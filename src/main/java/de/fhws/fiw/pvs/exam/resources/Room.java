package de.fhws.fiw.pvs.exam.resources;

import com.owlike.genson.annotation.JsonConverter;
import com.owlike.genson.annotation.JsonIgnore;
import de.fhws.fiw.pvs.exam.utils.ObjectIdConverter.ObjectIdXmlConverter;
import de.fhws.fiw.pvs.exam.utils.linkutils.ServerLinkConverter;
import org.bson.types.ObjectId;
import org.glassfish.jersey.linking.InjectLink;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Room
{
    @XmlJavaTypeAdapter(ObjectIdXmlConverter.class)
    public ObjectId id;

    public String name;
    public int capacity;

    public Room()
    {
    }

    public Room(String name, int capacity)
    {
        this.name = name;
        this.capacity = capacity;
    }

    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
    }

    public ObjectId getId()
    {
        return id;
    }

    public void setId(ObjectId id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getCapacity()
    {
        return capacity;
    }


    @InjectLink(style = InjectLink.Style.ABSOLUTE, value = "rooms/${instance.id}", type = "{" + MediaType.APPLICATION_XML + ", " + MediaType.APPLICATION_JSON + "}", rel = "self")
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    @XmlElement(name = "self")
    private Link selfUri;

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
        return "Room{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", selfUri=" + selfUri +
                '}';
    }
}