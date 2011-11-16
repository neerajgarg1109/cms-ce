package com.enonic.cms.core.portal.xtrace;

import java.util.List;

import com.enonic.cms.core.portal.livetrace.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.enonic.cms.core.security.user.QualifiedUsername;

public class JsonSerializer
{
    private PageRenderingTrace pageTrace;

    private long timeZero = 0;

    public String serialize( PageRenderingTrace pageRenderingTrace )
    {
        this.pageTrace = pageRenderingTrace;
        this.timeZero = pageRenderingTrace.getDuration().getStartTime().getMillis();

        JsonObject wrapper = new JsonObject();

        JsonObject xtrace = new JsonObject();
        appendVersion( xtrace );

        appendPortal( xtrace );

        wrapper.add( "xtrace", xtrace );

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();

        Gson gson = gsonBuilder.create();

        return gson.toJson( wrapper );
    }

    private void appendPortal( JsonObject xtrace )
    {
        xtrace.add( "portal", createPortal() );
    }

    private void appendVersion( JsonObject xtrace )
    {
        xtrace.addProperty( "version", "1.0" );
    }

    private JsonObject createPortal()
    {
        JsonObject portalObject = new JsonObject();

        PortalRequestTrace portalRequestTrace = pageTrace.getPortalRequestTrace();

        portalObject.addProperty("id", portalRequestTrace.getId());
        portalObject.addProperty("request_number", portalRequestTrace.getRequestNumber());
        portalObject.addProperty("url", portalRequestTrace.getUrl());
        portalObject.addProperty("requester", portalRequestTrace.getRequester().toString());

        appendPage( portalObject );

        return portalObject;
    }

    private void appendPage( JsonObject portalObject )
    {
        portalObject.add( "page", createPage() );
    }

    private JsonObject createPage()
    {
        JsonObject pageObject = new JsonObject();

        pageObject.addProperty( "name", pageTrace.getPortalRequestTrace().getUrl() );
        pageObject.addProperty( "cache_hit", pageTrace.isUsedCachedResult() );
        pageObject.addProperty( "ran_as_user", resolveQualifiedUsernameAsString( pageTrace.getRenderer() ) );

        JsonObject duration = new JsonObject();

        duration.addProperty( "start_time_ms", 0 );
        duration.addProperty( "stop_time_ms", pageTrace.getDuration().getStopTime().getMillis() - timeZero );
        duration.addProperty( "total_time_ms", pageTrace.getDuration().getExecutionTimeInMilliseconds() );
        pageObject.add( "duration", duration );

        appendPageDatasources( pageObject );
        appendXsltTransformingObjectForPage( pageObject );
        appendWindows( pageObject );

        pageObject.add( "instruction_post_processing",
                        createInstructionPostProcessingObject( pageTrace.getInstructionPostProcessingTrace() ) );

        return pageObject;
    }

    private void appendPageDatasources( JsonObject page )
    {
        if ( !pageTrace.isUsedCachedResult() )
        {
            page.add( "datasources", createDatasources( pageTrace.getDatasourceExecutionTraces() ) );
        }
    }

    private void appendWindows( JsonObject page )
    {
        page.add( "windows", createWindows( pageTrace.getWindowRenderingTraces() ) );
    }

    private JsonArray createWindows( List<WindowRenderingTrace> windows )
    {
        JsonArray windowsArray = new JsonArray();
        for ( WindowRenderingTrace window : windows )
        {
            createWindow( windowsArray, window );
        }
        return windowsArray;
    }

    private void createWindow( JsonArray windowsArray, WindowRenderingTrace windowTrace )
    {
        JsonObject windowObject = new JsonObject();

        windowObject.addProperty( "name", windowTrace.getPortletName() );
        windowObject.addProperty( "cache_hit", windowTrace.isUsedCachedResult() );
        windowObject.addProperty( "ran_as_user", resolveQualifiedUsernameAsString( windowTrace.getRenderer() ) );

        JsonObject duration = new JsonObject();

        duration.addProperty( "start_time_ms", windowTrace.getDuration().getStartTime().getMillis() - timeZero );
        duration.addProperty( "stop_time_ms", windowTrace.getDuration().getStopTime().getMillis() - timeZero );
        duration.addProperty( "total_time_ms", windowTrace.getDuration().getExecutionTimeInMilliseconds() );
        windowObject.add( "duration", duration );

        appendWindowDatasources( windowObject, windowTrace );
        appendXsltTransformingObjectForWindow( windowObject, windowTrace );

        windowObject.add( "instruction_post_processing",
                          createInstructionPostProcessingObject( windowTrace.getInstructionPostProcessingTrace() ) );

        windowsArray.add( windowObject );
    }

    private void appendXsltTransformingObjectForPage( JsonObject pageObject )
    {
        if ( !pageTrace.isUsedCachedResult() )
        {
            long startTime = pageTrace.hasDatasourceExecutionTraces() ? pageTrace.getDatasourceExecutionTraces().get(
                pageTrace.getDatasourceExecutionTraces().size() - 1 ).getDuration().getStopTime().getMillis() - timeZero : 0;

            long endTime = pageTrace.getInstructionPostProcessingTrace().getDuration().getStartTime().getMillis() - timeZero;

            JsonObject xsltTransformingObject = new JsonObject();

            JsonObject duration = new JsonObject();

            duration.addProperty( "start_time_ms", startTime );
            duration.addProperty( "stop_time_ms", endTime );
            duration.addProperty( "total_time_ms", endTime - startTime );
            xsltTransformingObject.add( "duration", duration );

            pageObject.add( "xslt_transforming", xsltTransformingObject );
        }
    }

    private void appendXsltTransformingObjectForWindow( JsonObject windowObject, WindowRenderingTrace windowTrace )
    {
        if ( !windowTrace.isUsedCachedResult() )
        {
            long startTime = windowTrace.hasDatasourceExecutionTraces() ? windowTrace.getDatasourceExecutionTraces().get(
                windowTrace.getDatasourceExecutionTraces().size() - 1 ).getDuration().getStopTime().getMillis() - timeZero
                : windowTrace.getDuration().getStartTime().getMillis() - timeZero;

            long endTime = windowTrace.getInstructionPostProcessingTrace().getDuration().getStartTime().getMillis() - timeZero;

            JsonObject xsltTransformingObject = new JsonObject();

            JsonObject duration = new JsonObject();

            duration.addProperty( "start_time_ms", startTime );
            duration.addProperty( "stop_time_ms", endTime );
            duration.addProperty( "total_time_ms", endTime - startTime );
            xsltTransformingObject.add( "duration", duration );

            windowObject.add( "xslt_transforming", xsltTransformingObject );
        }
    }

    private JsonObject createInstructionPostProcessingObject( InstructionPostProcessingTrace trace )
    {
        JsonObject object = new JsonObject();

        JsonObject duration = new JsonObject();
        duration.addProperty( "total_time_ms", trace.getDuration().getExecutionTimeInMilliseconds() );
        duration.addProperty( "start_time_ms", trace.getDuration().getStartTime().getMillis() - timeZero );
        duration.addProperty( "stop_time_ms", trace.getDuration().getStopTime().getMillis() - timeZero );

        object.add( "duration", duration );

        return object;
    }

    private void appendWindowDatasources( JsonObject windowObject, WindowRenderingTrace window )
    {
        if ( !window.isUsedCachedResult() )
        {
            windowObject.add( "datasources", createDatasources( window.getDatasourceExecutionTraces() ) );
        }
    }

    private JsonArray createDatasources( List<DatasourceExecutionTrace> datasources )
    {
        JsonArray datasourceArray = new JsonArray();

        for ( DatasourceExecutionTrace datasource : datasources )
        {
            createDatasource( datasourceArray, datasource );
        }

        return datasourceArray;
    }

    private void createDatasource( JsonArray jsonArray, DatasourceExecutionTrace datasource )
    {
        JsonObject datasourceObject = new JsonObject();
        datasourceObject.addProperty( "name", datasource.getMethodName() );

        JsonObject duration = new JsonObject();

        duration.addProperty( "start_time_ms", datasource.getDuration().getStartTime().getMillis() - timeZero );
        duration.addProperty( "stop_time_ms", datasource.getDuration().getStopTime().getMillis() - timeZero );
        duration.addProperty( "total_time_ms", datasource.getDuration().getExecutionTimeInMilliseconds() );
        datasourceObject.add( "duration", duration );

        jsonArray.add( datasourceObject );
    }

    private String resolveQualifiedUsernameAsString( QualifiedUsername qualifiedUsername )
    {
        String userStoreName = "";
        if ( qualifiedUsername.getUserStoreName() != null )
        {
            userStoreName = qualifiedUsername.getUserStoreName() + "\\";
        }

        return userStoreName + qualifiedUsername.getUsername();
    }

    private long calculateTotalExecutionTimeForWindows( List<WindowRenderingTrace> windows )
    {
        long total = 0;
        for ( WindowRenderingTrace window : windows )
        {
            total += window.getDuration().getExecutionTimeInMilliseconds();
        }

        return total;
    }

    private long calculateTotalExecutionTimeForDatasources( List<DatasourceExecutionTrace> datasources )
    {
        long total = 0;
        for ( DatasourceExecutionTrace datasource : datasources )
        {
            total += datasource.getDuration().getExecutionTimeInMilliseconds();
        }

        return total;
    }

}