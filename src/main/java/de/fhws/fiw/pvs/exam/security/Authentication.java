package de.fhws.fiw.pvs.exam.security;

import com.owlike.genson.Genson;
import de.fhws.fiw.pvs.exam.resources.UserData;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static de.fhws.fiw.pvs.exam.utils.RequestUtil.executeRequest;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class Authentication implements ContainerRequestFilter
{
    @Context
    protected HttpServletRequest httpServletRequest;

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        String authorizationHeader;

        try
        {
            authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        } catch (Exception e)
        {
            abortWithUnauthorized(requestContext);
            return;
        }

        if (!isTokenBasedAuthentication(authorizationHeader))
        {
            abortWithUnauthorized(requestContext);
            return;
        }

        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        try
        {
            validateToken(token);
        } catch (Exception e)
        {
            abortWithUnauthorized(requestContext);
            return;
        }
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader)
    {
        boolean isNotNull = authorizationHeader != null;
        boolean hasPrefixBearer = false;

        if (isNotNull)
        {
            hasPrefixBearer = authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
        }

        return isNotNull && hasPrefixBearer;
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext)
    {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .build());
    }

    private void validateToken(String token) throws Exception
    {
        Request request = new Request.Builder()
                .url("https://api.fiw.fhws.de/auth/api/users/me")
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        okhttp3.Response response = executeRequest(request);

        setCurrentUserAndRole(response);

        if (response.code() == 401)
        {
            throw new Exception();
        }
    }

    private void setCurrentUserAndRole(okhttp3.Response response) throws IOException
    {
        String responseBody = response.body().string();

        Genson genson = new Genson();
        UserData userData = genson.deserialize(responseBody, UserData.class);

        httpServletRequest.setAttribute("userData", userData);
    }
}
