package de.fhws.fiw.pvs.exam.utils;

import de.fhws.fiw.pvs.exam.resources.Room;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import java.util.Collection;

public class CacheUtil
{
    public static <T> Response.ResponseBuilder evaluateEtagCollection(Request request, ContainerRequestContext requestContext, Collection<T> data, GenericEntity<Collection<T>> identifier)
    {
        EntityTag eTag = new EntityTag(Integer.toString(data.toString().hashCode()));

        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);

        boolean hasNoEtag = requestContext.getHeaderString("If-Match") == null;

        if (builder != null || hasNoEtag)
        {
            builder = Response.ok(identifier);
        } else
        {
            builder = Response.notModified();
        }

        return builder.tag(eTag);
    }

    public static <T> Response.ResponseBuilder evaluateEtagSingle(Request request, ContainerRequestContext requestContext, T data)
    {
        EntityTag eTag = new EntityTag(Integer.toString(data.toString().hashCode()));

        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);

        boolean hasNoEtag = requestContext.getHeaderString("If-Match") == null;

        if (builder != null || hasNoEtag)
        {
            builder = Response.ok(data);
        } else
        {
            builder = Response.notModified();
        }

        return builder.tag(eTag);
    }
}
