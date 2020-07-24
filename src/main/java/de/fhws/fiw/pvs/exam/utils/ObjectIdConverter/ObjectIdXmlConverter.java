package de.fhws.fiw.pvs.exam.utils.ObjectIdConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.bson.types.ObjectId;

public class ObjectIdXmlConverter extends XmlAdapter<String, ObjectId>
{

    @Override
    public String marshal(ObjectId id) throws Exception
    {
        if (id == null)
        {
            return null;
        } else {
            return id.toString();
        }
    }

    @Override
    public ObjectId unmarshal(String id) throws Exception
    {
        return new ObjectId(id);
    }

}