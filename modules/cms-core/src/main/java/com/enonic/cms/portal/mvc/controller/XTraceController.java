package com.enonic.cms.portal.mvc.controller;

import com.enonic.cms.core.security.InvalidCredentialsException;
import com.enonic.cms.core.security.SecurityService;
import com.enonic.cms.core.security.user.QualifiedUsername;
import com.enonic.cms.core.security.userstore.UserStoreEntity;
import com.enonic.cms.core.security.userstore.UserStoreKey;
import com.enonic.cms.core.security.userstore.UserStoreService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XTraceController extends AbstractController
{
    @Resource
    protected SecurityService securityService;

    @Resource
    protected UserStoreService userStoreService;

    @Override
    protected ModelAndView handleRequestInternal( HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        final String path = request.getRequestURI();
        if ( path.matches( ".+/resources/.+" ) )
        {
            handleResource( request, response );
            return null;
        }

        return handleAuthenticationForm( request, response );
    }

    private ModelAndView handleAuthenticationForm( HttpServletRequest request, HttpServletResponse response )
            throws Exception
    {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put( "authenticationFailed", false );

        if ( isAuthenticationFormSubmitted( request ) )
        {
            try
            {
                authenticateUser( request );
                HttpSession httpSession = request.getSession( true );
                httpSession.setAttribute( "X-Trace-Server-Enabled", "true" );

                response.sendRedirect( (String) request.getAttribute( "xtrace.originalUrl" ) );

                return null;
            }
            catch ( InvalidCredentialsException ice )
            {
                model.put( "authenticationFailed", true );
            }
        }

        model.put( "userStores", createUserStoreMap() );

        return new ModelAndView( "xtraceAuthenticationPage", model );
    }

    private void authenticateUser( HttpServletRequest request )
            throws InvalidCredentialsException
    {
        final String userName = request.getParameter( "_xtrace_username" );
        final String password = request.getParameter( "_xtrace_password" );
        final String userStore = request.getParameter( "_xtrace_userstore" );

        final UserStoreKey userStoreKey = new UserStoreKey( Integer.parseInt( userStore ) );
        final UserStoreEntity systemUserStore = userStoreService.getUserStore( userStoreKey );
        final QualifiedUsername qname = new QualifiedUsername( systemUserStore.getKey(), userName );

        securityService.authenticateUser( qname, password );
    }

    private boolean isAuthenticationFormSubmitted( HttpServletRequest request )
    {
        if( !"POST".equalsIgnoreCase( request.getMethod() ) )
        {
            return false;
        }
        String xtraceAuthentication = request.getParameter( "_xtrace_authentication" );

        if( "true".equalsIgnoreCase( xtraceAuthentication ) )
        {
            return true;
        }
        return false;
    }

    private HashMap<String, String> createUserStoreMap()
    {
        final HashMap<String, String> userStoreMap = new HashMap<String, String>();

        final List<UserStoreEntity> userStoreList = userStoreService.findAll();
        for ( UserStoreEntity userStore : userStoreList )
        {
            userStoreMap.put( userStore.getKey().integerValue().toString(), userStore.getName() );
        }

        return userStoreMap;
    }

    private void handleResource( HttpServletRequest request, HttpServletResponse response )
            throws Exception
    {
    }
}
