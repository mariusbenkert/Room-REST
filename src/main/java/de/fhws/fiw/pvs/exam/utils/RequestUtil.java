package de.fhws.fiw.pvs.exam.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;

public class RequestUtil
{

    public static okhttp3.Response executeRequest(final Request request)
    {
        OkHttpClient client = new OkHttpClient();
        okhttp3.Response response;

        try
        {
            response = client.newCall(request).execute();
        } catch (final IOException e)
        {
            response = null;
        }

        return response;
    }
}
