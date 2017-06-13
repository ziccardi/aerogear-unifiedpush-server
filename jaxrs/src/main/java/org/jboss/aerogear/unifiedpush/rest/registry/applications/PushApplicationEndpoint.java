/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dto.Count;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;

import com.qmino.miredot.annotations.ReturnType;

@Path("/applications")
public class PushApplicationEndpoint extends AbstractBaseEndpoint {
    private static final int MAX_PAGE_SIZE = 25;
    private static final int DEFAULT_PAGE_SIZE = 8;

    @Inject
    private PushApplicationService pushAppService;

    @Inject
    private PushMessageMetricsService metricsService;

    @Inject
    private InstallationDao installationDao;

    /**
     * Create Push Application
     *
     * @param pushApp   new {@link PushApplication}
     * @return          created {@link PushApplication}
     *
     * @statuscode 201 The PushApplication Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.api.PushApplication")
    public Response registerPushApplication(PushApplication pushApp) {

         // some validation
        try {
            validateModelClass(pushApp);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        pushAppService.addPushApplication(pushApp);

        return Response.created(UriBuilder.fromResource(PushApplicationEndpoint.class).path(String.valueOf(pushApp.getPushApplicationID())).build()).entity(pushApp)
                .build();
    }

    /**
     * List Push Applications
     *
     * @param page                  page number
     * @param pageSize              number of items per page
     * @param includeDeviceCount    put device count into response headers, default {@code false}
     * @param includeActivity       put activity into response headers, default {@code false}
     * @return                      list of {@link PushApplication}s
     *
     * @responseheader total                                Total count of items
     * @responseheader activity_app_{pushApplicationID}     Count number of messages for Push Application
     * @responseheader activity_variant_{variantID}         Count number of messages for Variant
     * @responseheader deviceCount_app_{pushApplicationID}  Count number of devices for Push Application
     * @responseheader deviceCount_variant_{variantID}      Count number of devices for Variant
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.util.List<org.jboss.aerogear.unifiedpush.api.PushApplication>")
    public Response listAllPushApplications(@QueryParam("page") Integer page,
                                            @QueryParam("per_page") Integer pageSize,
                                            @QueryParam("includeDeviceCount") @DefaultValue("false") boolean includeDeviceCount,
                                            @QueryParam("includeActivity")    @DefaultValue("false") boolean includeActivity) {
        if (pageSize != null) {
            pageSize = Math.min(MAX_PAGE_SIZE, pageSize);
        } else {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        if (page == null) {
            page = 0;
        }

        final PageResult<PushApplication, Count> pageResult = getSearch().findAllPushApplicationsForDeveloper(page, pageSize);
        ResponseBuilder response = Response.ok(pageResult.getResultList());
        response.header("total", pageResult.getAggregate().getCount());
        for (PushApplication app : pageResult.getResultList()) {
            if (includeActivity) {
                putActivityIntoResponseHeaders(app, response);
            }
            if (includeDeviceCount) {
                putDeviceCountIntoResponseHeaders(app, response);
            }
        }
        return response.build();
    }

    /**
     * Get Push Application.
     *
     * @param pushApplicationID     id of {@link PushApplication}
     * @param includeDeviceCount    boolean param to put device count into response headers, default {@code false}
     * @param includeActivity       boolean param to put activity into response headers, default {@code false}
     * @return                      requested {@link PushApplication}
     *
     * @responseheader activity_app_{pushApplicationID}     Count number of messages for Push Application
     * @responseheader activity_variant_{variantID}         Count number of messages for Variant
     * @responseheader deviceCount_app_{pushApplicationID}  Count number of devices for Push Application
     * @responseheader deviceCount_variant_{variantID}      Count number of devices for Variant
     *
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @GET
    @Path("/{pushAppID}")
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.api.PushApplication")
    public Response findById(
            @PathParam("pushAppID") String pushApplicationID,
            @QueryParam("includeDeviceCount") @DefaultValue("false") boolean includeDeviceCount,
            @QueryParam("includeActivity")    @DefaultValue("false") boolean includeActivity) {

        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp != null) {
            ResponseBuilder response = Response.ok(pushApp);
            if (includeActivity) {
                putActivityIntoResponseHeaders(pushApp, response);
            }
            if (includeDeviceCount) {
                putDeviceCountIntoResponseHeaders(pushApp, response);
            }
            return response.build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    private void putActivityIntoResponseHeaders(PushApplication app, ResponseBuilder response) {
        response.header("activity_app_" + app.getPushApplicationID(), metricsService.countMessagesForPushApplication(app.getPushApplicationID()));
    }

    private void putDeviceCountIntoResponseHeaders(PushApplication app, ResponseBuilder response) {
        long appCount = 0;
        for (Variant variant : app.getVariants()) {
            long variantCount = installationDao.getNumberOfDevicesForVariantID(variant.getVariantID());
            appCount += variantCount;
            response.header("deviceCount_variant_" + variant.getVariantID(), variantCount);
        }
        response.header("deviceCount_app_" + app.getPushApplicationID(), appCount);
    }

    /**
     * Update Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @param updatedPushApp    new info of {@link PushApplication}
     *
     * @statuscode 204 The PushApplication updated successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @PUT
    @Path("/{pushAppID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.lang.Void")
    public Response updatePushApplication(@PathParam("pushAppID") String pushApplicationID, PushApplication updatedPushApp) {

        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp != null) {

            // some validation
            try {
                validateModelClass(updatedPushApp);
            } catch (ConstraintViolationException cve) {

                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // update name/desc:
            pushApp.setDescription(updatedPushApp.getDescription());
            pushApp.setName(updatedPushApp.getName());
            pushAppService.updatePushApplication(pushApp);

            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    /**
     * Reset MasterSecret for Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return                  updated {@link PushApplication}
     *
     * @statuscode 204 The MasterSecret for Push Application reset successfully
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @PUT
    @Path("/{pushAppID}/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.api.PushApplication")
    public Response resetMasterSecret(@PathParam("pushAppID") String pushApplicationID) {

        //PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, extractUsername(request));
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp != null) {
            // generate the new 'masterSecret' and apply it:
            String newMasterSecret = UUID.randomUUID().toString();
            pushApp.setMasterSecret(newMasterSecret);
            pushAppService.updatePushApplication(pushApp);

            return Response.ok(pushApp).build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    /**
     * Delete Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     *
     * @statuscode 204 The PushApplication successfully deleted
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @DELETE
    @Path("/{pushAppID}")
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.lang.Void")
    public Response deletePushApplication(@PathParam("pushAppID") String pushApplicationID) {

        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp != null) {
            pushAppService.removePushApplication(pushApp);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    /**
     * Count Push Applications
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return                  count number for each {@link org.jboss.aerogear.unifiedpush.api.VariantType}
     */
    @GET
    @Path("/{pushAppID}/count")
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.util.Map<java.lang.String, java.lang.Long>")
    public Response countInstallations(@PathParam("pushAppID") String pushApplicationID) {

        Map<String, Long> result = pushAppService.countInstallationsByType(pushApplicationID);

        return Response.ok(result).build();
    }
}
