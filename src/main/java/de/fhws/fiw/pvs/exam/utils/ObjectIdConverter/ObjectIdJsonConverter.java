package de.fhws.fiw.pvs.exam.utils.ObjectIdConverter;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import org.bson.types.ObjectId;

public class ObjectIdJsonConverter implements Converter<ObjectId>
{
    @Override
    public void serialize(ObjectId objectId, ObjectWriter writer, Context context) throws Exception
    {
        writer.writeValue(objectId.toString());
    }

    @Override
    public ObjectId deserialize(ObjectReader reader, Context context) throws Exception
    {
        return new ObjectId(reader.valueAsString());
    }
}
