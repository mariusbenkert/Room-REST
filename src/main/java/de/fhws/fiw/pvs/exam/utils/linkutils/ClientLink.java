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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class can be used in data models on the client side when the server sends hyperlinks
 * as part of the response body, i.e. the representation. For example, if the Java class on
 * the client side contains an attribute <code>self</code>:
 *
 * <code>
 *
 * @JsonConverter( ClientLinkConverter.class )
 * private ClientLink self;
 * </code>
 */
public class ClientLink
{
	private String url;

	private String mediaType;

	private String relationType;

	public ClientLink( )
	{
	}

	public ClientLink( final String url, final String mediaType, final String relationType )
	{
		this.url = url;
		this.mediaType = mediaType;
		this.relationType = relationType;
	}

	public String getUrl( )
	{
		return this.url;
	}

	public void setUrl( final String url )
	{
		this.url = url;
	}

	public String getMediaType( )
	{
		return this.mediaType;
	}

	public void setMediaType( final String mediaType )
	{
		this.mediaType = mediaType;
	}

	public String getRelationType( )
	{
		return this.relationType;
	}

	public void setRelationType( final String relationType )
	{
		this.relationType = relationType;
	}

	@Override public String toString( )
	{
		return "NorburyLink{" +
			"url='" + this.url + '\'' +
			", mediaType='" + this.mediaType + '\'' +
			", relationType='" + this.relationType + '\'' +
			'}';
	}

	public static Map<String, ClientLink> parseFromHttpHeader( final List<String> headerLinks )
	{
		return headerLinks.stream( )
						  .map( h -> parseFromHttpHeader( h ) )
						  .collect( Collectors.toMap( h -> h.getRelationType( ), h -> h ) );
	}

	public static ClientLink parseFromHttpHeader( final String header )
	{
		final String[] elements = header.split( ";" );
		return new ClientLink( parseHref( elements[ 0 ] ), parseType( elements[ 2 ] ), parseRel( elements[ 1 ] ) );
	}

	private static String parseHref( final String headerElement )
	{
		return parse( headerElement, "<([^>]*)>" );
	}

	private static String parseRel( final String headerElement )
	{
		return parse( headerElement, "^rel=\"(.+)\"$" );
	}

	private static String parseType( final String headerElement )
	{
		return parse( headerElement, "^type=\"(.+)\"$" );
	}

	private static String parse( final String headerElement, final String patternExpression )
	{
		final Pattern pattern = Pattern.compile( patternExpression );
		final Matcher matcher = pattern.matcher( headerElement );
		return matcher.find( ) ? matcher.group( 1 ) : null;
	}
}
