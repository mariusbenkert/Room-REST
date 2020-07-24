package de.fhws.fiw.pvs.exam.utils;

import de.fhws.fiw.pvs.exam.dao.RoomDAO;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

public class Pagination
{
    public static URI selfUri(int offset, int size, UriInfo uriInfo)
    {
        URI self = uriInfo
                .getRequestUriBuilder()
                .replaceQueryParam("offset", offset)
                .replaceQueryParam("size", size)
                .build();

        return self;
    }

    public static URI previousPage(int offset, int size, UriInfo uriInfo)
    {
        URI self = uriInfo
                .getRequestUriBuilder()
                .replaceQueryParam("offset", (offset - size))
                .replaceQueryParam("size", size)
                .build();

        //check for previous page
        if (offset < size)
        {
            self = URI.create("");
        }

        return self;
    }

    public static URI nextPage(int offset, int size, UriInfo uriInfo, long maxSize)
    {
        URI self = uriInfo
                .getRequestUriBuilder()
                .replaceQueryParam("offset", (offset + size))
                .replaceQueryParam("size", size)
                .build();

        //check for next page
        if (maxSize < (size + offset))
        {
            self = URI.create("");
        }

        return self;
    }

    public static Response.ResponseBuilder paging(int offset, int size, UriInfo uriInfo, Response.ResponseBuilder builder, long maxSize)
    {
        URI prev = Pagination.previousPage(offset, size, uriInfo);

        if (!prev.equals(URI.create("")))
        {
            builder.link(prev, "previous");
        }

        URI next = Pagination.nextPage(offset, size, uriInfo, maxSize);

        if (!next.equals(URI.create("")))
        {
            builder.link(next, "next");
        }

        URI self = Pagination.selfUri(offset, size, uriInfo);
        if (!self.equals(URI.create("")))
        {
            builder.link(self, "self");
        }
        
        return builder;
    }
}
