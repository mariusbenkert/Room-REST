package de.fhws.fiw.pvs.exam.security;

import de.fhws.fiw.pvs.exam.resources.UserData;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class Authorization implements ContainerRequestFilter
{
    @Context
    private ResourceInfo resourceInfo;

    @Context
    protected HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException
    {

        Method resourceMethod = resourceInfo.getResourceMethod();
        List<Role> methodRoles = extractRoles(resourceMethod);

        try
        {
            if (!methodRoles.isEmpty())
            {
                checkPermissions(methodRoles);
            }
        } catch (Exception e)
        {
            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    private List<Role> extractRoles(AnnotatedElement annotatedElement)
    {
        if (annotatedElement == null)
        {
            return new ArrayList<Role>();
        } else
        {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null)
            {
                return new ArrayList<Role>();
            } else
            {
                Role[] allowedRoles = secured.value();
                return Arrays.asList(allowedRoles);
            }
        }
    }

    private void checkPermissions(List<Role> allowedRoles) throws Exception
    {
        UserData userData = (UserData) httpServletRequest.getAttribute("userData");

        for (Role r : allowedRoles)
        {
            if (r.name().equals(userData.getRole()))
            {
                return;
            }
        }

        throw new Exception();
    }
}
