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

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import javax.ws.rs.core.Link;

/**
 * This class must be used for attributes in models of the server side. On the server side, data models
 * use class {@link Link} for hyperlinks that will be part of the response body. In order to create
 * a readable representation of such hyperlinks that only consits of the URL, media type, and relation
 * type, we need this link converter.
 */
public class ServerLinkConverter implements Converter<Link>
{
	@Override public void serialize( final Link link, final ObjectWriter objectWriter, final Context context )
		throws Exception
	{
		objectWriter.writeName( link.getRel( ) );
		objectWriter.beginObject( );
		objectWriter.writeString( "href", replaceCharacters( link.getUri( ).toASCIIString( ) ) );
		objectWriter.writeString( "rel", link.getRel( ) );

		if ( link.getType( ) != null && link.getType( ).isEmpty( ) == false )
		{
			objectWriter.writeString( "type", link.getType( ) );
		}

		objectWriter.endObject( );
	}

	@Override public Link deserialize( final ObjectReader objectReader, final Context context ) throws Exception
	{
		String uri = "";
		String type = "";
		String rel = "";

		objectReader.beginObject( );
		while ( objectReader.hasNext( ) )
		{
			objectReader.next( );
			if ( "href".equals( objectReader.name( ) ) )
			{
				uri = objectReader.valueAsString( );
			}
			if ( "rel".equals( objectReader.name( ) ) )
			{
				rel = objectReader.valueAsString( );
			}
			if ( "type".equals( objectReader.name( ) ) )
			{
				type = objectReader.valueAsString( );
			}
		}

		objectReader.endObject( );

		return Link.fromUri( uri ).rel( rel ).type( type ).build( );
	}

	private String replaceCharacters( final String body )
	{
		return body.replace( "%3F", "?" )
				   .replaceAll( "%7B", "{" )
				   .replaceAll( "%7D", "}" );
	}
}