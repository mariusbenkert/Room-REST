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
package de.fhws.fiw.pvs.exam;

import com.owlike.genson.GensonBuilder;
import com.owlike.genson.ext.jaxrs.GensonJaxRSFeature;
import de.fhws.fiw.pvs.exam.security.Authentication;
import de.fhws.fiw.pvs.exam.security.Authorization;
import de.fhws.fiw.pvs.exam.services.*;
import de.fhws.fiw.pvs.exam.utils.ObjectIdConverter.ObjectIdJsonConverter;
import org.bson.types.ObjectId;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("api")
public class Application extends ResourceConfig
{
    public Application()
    {
        super();
        registerClasses(getServiceClasses());
        packages("org.glassfish.jersey.examples.linking");
        register(DeclarativeLinkingFeature.class);
        register(MultiPartFeature.class);
        register(CorsFilter.class);
        register(Authentication.class);
        register(Authorization.class);
        register(new GensonJaxRSFeature().use(
                new GensonBuilder().setSkipNull(true)
                        .useIndentation(true)
                        .useDateAsTimestamp(false)
                        .useDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))
                        .withConverter(new ObjectIdJsonConverter(), ObjectId.class)
                        .create()));
    }

    protected Set<Class<?>> getServiceClasses()
    {
        final Set<Class<?>> returnValue = new HashSet<>();

        returnValue.add(StartService.class);
        returnValue.add(RoomService.class);

        return returnValue;
    }
}
