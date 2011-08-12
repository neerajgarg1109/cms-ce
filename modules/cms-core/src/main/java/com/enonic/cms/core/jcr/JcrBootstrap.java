package com.enonic.cms.core.jcr;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.extensions.jcr.JcrSessionFactory;

import com.google.common.io.Files;

import static com.enonic.cms.core.jcr.JcrCmsConstants.ENONIC_CMS_NAMESPACE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.ENONIC_CMS_NAMESPACE_PREFIX;
import static com.enonic.cms.core.jcr.JcrCmsConstants.GROUPS_NODE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.GROUPS_NODE_TYPE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.ROLES_NODE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.ROLES_NODE_TYPE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.ROOT_NODE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.SYSTEM_USERSTORE_NODE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.USERSTORES_NODE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.USERSTORES_NODE_TYPE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.USERSTORE_NODE_TYPE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.USERS_NODE;
import static com.enonic.cms.core.jcr.JcrCmsConstants.USERS_NODE_TYPE;

public class JcrBootstrap
{
    private static final Logger LOG = LoggerFactory.getLogger( JcrBootstrap.class );

    private JcrSessionFactory sessionFactory;

    private Resource compactNodeDefinitionFile;

    private File homeDir;

    @Autowired
    private JcrAccountsImporter jcrAccountsImporter;

    public JcrBootstrap()
    {
    }

    @PostConstruct
    public void afterPropertiesSet()
        throws Exception
    {
        initialize();
    }

    public void initialize()
    {
        LOG.info( "Initializing JCR repository..." );

        resetLocalRepository();
        Session jcrSession = null;
        try
        {
            jcrSession = sessionFactory.getSession();

            registerNamespaces( jcrSession );
            registerCustomNodeTypes( jcrSession );

            createTreeStructure( jcrSession );

            jcrSession.save();

            jcrAccountsImporter.importAccounts();

            // log imported tree
            LOG.info( JcrHelper.sessionViewToXml( jcrSession, "/enonic" ) );

            jcrSession.save();
        }
        catch ( Exception e )
        {
            throw new RepositoryRuntimeException( "Error while initializing JCR repository", e );
        }
        finally
        {
            if ( jcrSession != null )
            {
                jcrSession.logout();
            }
        }
        LOG.info( "JCR repository initialized" );
    }

    private void createTreeStructure( Session jcrSession )
        throws RepositoryException
    {
        Node root = jcrSession.getRootNode();

        if ( root.hasNode( ROOT_NODE ) )
        {
            root.getNode( ROOT_NODE ).remove();
            jcrSession.save();
        }
        Node enonic = root.addNode( ROOT_NODE, JcrConstants.NT_UNSTRUCTURED );
        Node userstores = enonic.addNode( USERSTORES_NODE, USERSTORES_NODE_TYPE );

        Node systemUserstore = userstores.addNode( SYSTEM_USERSTORE_NODE, USERSTORE_NODE_TYPE );
        Node groupsRoles = systemUserstore.addNode( GROUPS_NODE, GROUPS_NODE_TYPE );
        Node usersRoles = systemUserstore.addNode( USERS_NODE, USERS_NODE_TYPE );
        Node systemRoles = systemUserstore.addNode( ROLES_NODE, ROLES_NODE_TYPE );

        systemRoles.addNode( "ea", "cms:role" );
        systemRoles.addNode( "developer", "cms:role" );
        systemRoles.addNode( "administrator", "cms:role" );
        systemRoles.addNode( "contributor", "cms:role" );
        systemRoles.addNode( "expert", "cms:role" );
        systemRoles.addNode( "everyone", "cms:role" );
        systemRoles.addNode( "authenticated", "cms:role" );
    }

    private void registerCustomNodeTypes( Session jcrSession )
        throws RepositoryException, IOException, ParseException
    {
        Reader fileReader = new InputStreamReader( compactNodeDefinitionFile.getInputStream() );
        try
        {
            NodeType[] nodeTypes = CndImporter.registerNodeTypes( fileReader, jcrSession );
            for ( NodeType nt : nodeTypes )
            {
                LOG.info( "Registered node type: " + nt.getName() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    private void registerNamespaces( Session jcrSession )
        throws RepositoryException
    {
        Workspace workspace = jcrSession.getWorkspace();
        NamespaceRegistry reg = workspace.getNamespaceRegistry();

        String[] prefixes = reg.getPrefixes();
        Set<String> registeredPrefixes = new HashSet( Arrays.<String>asList( prefixes ) );

        registerNamespace( reg, registeredPrefixes, ENONIC_CMS_NAMESPACE_PREFIX, ENONIC_CMS_NAMESPACE );
    }

    private void registerNamespace( NamespaceRegistry reg, Set<String> registeredPrefixes, String prefix, String uri )
        throws RepositoryException
    {
        if ( !registeredPrefixes.contains( prefix ) )
        {
            reg.registerNamespace( prefix, uri );
            LOG.info( "JCR namespace registered " + prefix + ":" + uri );
        }
        else
        {
            String registeredUri = reg.getURI( prefix );
            if ( !uri.equals( registeredUri ) )
            {
                throw new RepositoryRuntimeException(
                    "Namespace prefix is already registered with a different URI: " + prefix + ":" + registeredUri );
            }
        }
    }

    public void setSessionFactory( org.springframework.extensions.jcr.JcrSessionFactory sessionFactory )
    {
        this.sessionFactory = sessionFactory;
    }

    public void setCompactNodeDefinitionFile( Resource compactNodeDefinitionFile )
    {
        this.compactNodeDefinitionFile = compactNodeDefinitionFile;
    }

    private void resetLocalRepository()
    {
        if ( this.homeDir.exists() )
        {
            try
            {
                Files.deleteRecursively( this.homeDir );
            }
            catch ( IOException e )
            {
                // DO NOTHING
            }
        }
    }

    @Value("${cms.home}/jackrabbit")
    public void setHomeDir( final File homeDir )
    {
        this.homeDir = homeDir;
    }

}