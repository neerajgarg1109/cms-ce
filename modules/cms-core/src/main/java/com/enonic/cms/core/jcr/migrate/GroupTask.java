package com.enonic.cms.core.jcr.migrate;

import org.apache.jackrabbit.JcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enonic.cms.core.jcr.JcrCmsConstants;
import com.enonic.cms.core.jcr.wrapper.JcrNode;
import com.enonic.cms.core.jcr.wrapper.JcrNodeIterator;
import com.enonic.cms.core.jcr.wrapper.JcrRepository;
import com.enonic.cms.core.jcr.wrapper.JcrSession;
import com.enonic.cms.core.jdbc.JdbcDynaRow;

@Component
public final class GroupTask
    extends OneToOneTask
{

    @Autowired
    private JcrRepository jcrRepository;

    public GroupTask()
    {
        super( "Group", "tGroup where grp_bisdeleted = 0" );
    }

    @Override
    protected void importRow( final JdbcDynaRow row )
    {
        this.logInfo( "Importing group: {0}", row.getString( "grp_sname" ) );

        Integer userStoreKey = row.getInteger( "grp_dom_lkey" );

        JcrSession session = null;

        try
        {
            session = jcrRepository.login();

            JcrNode userstoreNode;
            if ( userStoreKey == null )
            {
                userstoreNode = getDefaultUserStoreNode( session );
            }
            else
            {
                userstoreNode = getUserStoreNode( userStoreKey, session );
            }

            String groupKey = row.getString( "GRP_HKEY" );
            String groupName = row.getString( "GRP_SNAME" );
            String groupDescr = row.getString( "GRP_SDESCRIPTION" );
            Integer groupRestricted = row.getInteger( "GRP_BRESTRICTED" );
            String groupSync = row.getString( "GRP_SSYNCVALUE" );
            Integer groupType = row.getInteger( "GRP_LTYPE" );

            JcrNode groupsNode = userstoreNode.getNode( JcrCmsConstants.GROUPS_NODE );
            JcrNode groupNode = groupsNode.addNode( groupName, JcrCmsConstants.GROUP_NODE_TYPE );
            groupNode.setProperty( "name", groupName );
            groupNode.setProperty( "key", groupKey );
            if ( groupDescr != null )
            {
                groupNode.setProperty( "description", groupDescr );
            }
            groupNode.setProperty( "syncValue", groupSync );
            groupNode.setProperty( "type", groupType );
            groupNode.setProperty( "restricted", groupRestricted );

            groupNode.addNode( "members", JcrConstants.NT_UNSTRUCTURED );

            session.save();
        }
        finally
        {
            jcrRepository.logout( session );
        }
    }

    private JcrNode getDefaultUserStoreNode( JcrSession session )
    {
        String sql = "SELECT * " + "FROM [" + JcrCmsConstants.USERSTORE_NODE_TYPE + "] " + "WHERE localname() = $name";

        JcrNodeIterator nodes = session.createQuery( sql ).bindValue( "name", JcrCmsConstants.SYSTEM_USERSTORE_NODE ).execute();
        if ( nodes.hasNext() )
        {
            return nodes.nextNode();
        }
        return null;
    }

    private JcrNode getUserStoreNode( Integer userStoreKey, JcrSession session )
    {
        String sql = "SELECT * " + "FROM [" + JcrCmsConstants.USERSTORE_NODE_TYPE + "] " + "WHERE key = $key ";

        JcrNodeIterator nodes = session.createQuery( sql ).bindValue( "key", userStoreKey ).execute();
        if ( nodes.hasNext() )
        {
            return nodes.nextNode();
        }
        return null;
    }
}
