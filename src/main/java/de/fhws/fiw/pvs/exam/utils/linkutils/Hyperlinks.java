/*
 * Copyright (c) peter.braun@fhws.de
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.fhws.fiw.pvs.exam.utils.linkutils;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * This class provides some convenience methods for working with hyperlinks in responses.
 */
public class Hyperlinks
{
	/**
	 * Add a link to the given response builder using the host that was used for the request.
	 * <p>
	 * For example: addLink( uriInfo, reponseBuilder, "/demo/api/users/{id}", "getOneUserById", "application/json" 4711 );
	 * <p>
	 * If the incoming URI requests was directed to "https://example.org/demo/api/users?q=Hello", the above
	 * statement would add a hyperlink like "https://example.org/demo/api/users/4711" with the given
	 * relation type and media type.
	 *
	 * @param uriInfo         used to get the URI of the incoming request which is then replaced by the given path
	 * @param responseBuilder the response builder in which the link is added
	 * @param path            the relatativ path of the new hyperlink which might contain placeholders
	 * @param relationType    the relation type of the hyperlink to be added
	 * @param mediaType       the media type of the hyperlink to be added
	 * @param params          parameters that are replaced in the given path
	 */
	public static void addLink( final UriInfo uriInfo, final Response.ResponseBuilder responseBuilder,
		final String path,
		final String relationType, final String mediaType, final Object... params )
	{
		final UriBuilder builder = uriInfo.getAbsolutePathBuilder( );
		builder.replacePath( beforeQuestionMark( path ) );
		builder.replaceQuery( afterQuestionMark( path ) );
		String uriTemplate = builder.toTemplate( );

		if ( params != null )
		{
			for ( final Object p : params )
			{
				uriTemplate = replaceFirstTemplate( uriTemplate, p );
			}
		}

		responseBuilder.header( "Link", linkHeader( uriTemplate, relationType, mediaType ) );
	}

	/**
	 * Add a link to the response header.
	 *
	 * @param responseBuilder he response builder in which the link is added
	 * @param uri             the URI of the hyperlink to be added
	 * @param relationType    the relation type of the hyperlink to be added
	 * @param mediaType       the media type oof the hyperlink to be added
	 */
	public static void addLink( final Response.ResponseBuilder responseBuilder, final URI uri,
		final String relationType,
		final String mediaType )
	{
		responseBuilder.header( "Link", linkHeader( uri.toASCIIString( ), relationType, mediaType ) );
	}

	private static String beforeQuestionMark( final String path )
	{
		if ( path.indexOf( "?" ) != -1 )
		{
			return path.substring( 0, path.indexOf( "?" ) );
		}
		else
		{
			return path;
		}
	}

	private static String afterQuestionMark( final String path )
	{
		if ( path.indexOf( "?" ) != -1 )
		{
			return path.substring( path.indexOf( "?" ) + 1 );
		}
		else
		{
			return "";
		}
	}

	private static String replaceFirstTemplate( final String uri, final Object value )
	{
		return uri.replaceFirst( "\\{id\\}", value.toString( ) );
	}

	private static String linkHeader( final String uri, final String rel, final String mediaType )
	{
		final StringBuilder sb = new StringBuilder( );

		sb.append( '<' ).append( uri ).append( ">;" );
		sb.append( Link.REL ).append( "=\"" ).append( rel ).append( "\"" );

		if ( mediaType != null && mediaType.isEmpty( ) == false )
		{
			sb.append( ";" );
			sb.append( Link.TYPE ).append( "=\"" ).append( mediaType ).append( "\"" );
		}

		return sb.toString( );
	}

}
