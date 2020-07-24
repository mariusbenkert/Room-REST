package de.fhws.fiw.pvs.exam;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.File;

public class StartFromJar
{

    public static void main( final String[] args ) throws Exception
    {
        final Tomcat tomcat = new Tomcat( );
        tomcat.setPort( 8080 );
        final Context context = tomcat.addWebapp( "/exam", "/" );
        final String pathToJar = getJarFileOfThisProject( ).getAbsolutePath( );
        final WebResourceRoot resources = new StandardRoot( context );
        resources.addJarResources( new JarResourceSet( resources, "/WEB-INF/lib/", pathToJar, "/" ) );
        context.setResources( resources );
        Tomcat.addServlet( context, "jersey-container-servlet", new ServletContainer( new Application( ) ) );

        // In case you change "api" you also have to change the annotation at class Application
        context.addServletMappingDecoded( "/api/*", "jersey-container-servlet" );
        tomcat.start( );
        tomcat.getServer( ).await( );
    }

    private static File getJarFileOfThisProject( )
    {
        final String path = StartFromJar.class.getProtectionDomain( ).getCodeSource( ).getLocation( ).getPath( );
        final File fileOfClass = new File( path );
        if ( fileOfClass.isFile( ) )
        {
            return fileOfClass;
        }
        else
        {
            throw new IllegalStateException(
                    "Class " + StartFromJar.class.getCanonicalName( ) + " is not in a JAR file" );
        }
    }

}