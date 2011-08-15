package com.enonic.cms.store.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.resolver.DialectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.jdbc.support.JdbcUtils;

import com.enonic.cms.store.support.decorators.DecoratedConnection;
import com.enonic.cms.store.support.decorators.DecoratedDataSource;
import com.enonic.cms.store.support.decorators.DecoratedPreparedStatement;
import com.enonic.cms.store.support.decorators.DecoratedStatement;

public class DataSourceFactory
        implements FactoryBean, InitializingBean, BeanFactoryPostProcessor
{
    private final static Logger LOG = LoggerFactory.getLogger( DataSourceFactory.class );

    private DataSource originalDataSource;

    private DataSource dataSource;

    private boolean decorateQueryTimeout;

    public void setOriginalDataSource( DataSource originalDataSource )
    {
        this.originalDataSource = originalDataSource;
    }

    public void afterPropertiesSet()
            throws Exception
    {
        Dialect dialect = null;
        Connection connection = null;

        try
        {
            connection = originalDataSource.getConnection();

            dialect = DialectFactory.buildDialect( new Properties(), connection );
        }
        catch ( Exception e )
        {
            LOG.error( "cannot open connection to database or determine database type", e );
        }
        finally
        {
            JdbcUtils.closeConnection( connection );
        }

        decorateQueryTimeout = dialect != null && dialect instanceof PostgreSQLDialect;

        if ( decorateQueryTimeout )
        {
            LOG.info( "decorating database connection for ignoring setQueryTimeout calls" );

            dataSource = new QueryTimeoutDecoratedDataSource( originalDataSource );
        }
        else
        {
            dataSource = originalDataSource;
        }
    }

    public Object getObject()
            throws Exception
    {
        return dataSource;
    }

    public Class<?> getObjectType()
    {
        return DataSource.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory )
            throws BeansException
    {
        if ( decorateQueryTimeout )
        {
            // set timeout value for unspecified timeout in @Transactional annotation to JDBC default value
            BeanDefinition transactionManager = beanFactory.getBeanDefinition( "transactionManager" );
            // remove defaultTimeout that is specified in applicationContext.xml .
            transactionManager.getPropertyValues().removePropertyValue( "defaultTimeout" );
        }
    }

    private static class QueryTimeoutDecoratedDataSource
            extends DecoratedDataSource
    {
        private QueryTimeoutDecoratedDataSource( DataSource dataSource )
        {
            super( dataSource );
        }

        @Override
        public Connection getConnection()
                throws SQLException
        {
            return new QueryTimeoutDecoratedConnection( super.getConnection() );
        }
    }


    private static class QueryTimeoutDecoratedConnection
            extends DecoratedConnection
    {
        public QueryTimeoutDecoratedConnection( Connection connection )
        {
            super( connection );
        }

        @Override
        public Statement createStatement()
                throws SQLException
        {
            return new QueryTimeoutDecoratedStatement( super.createStatement() );
        }

        @Override
        public PreparedStatement prepareStatement( String sql )
                throws SQLException
        {
            return new QueryTimeoutDecoratedPreparedStatement( super.prepareStatement( sql ) );
        }

        @Override
        public Statement createStatement( int resultSetType, int resultSetConcurrency )
                throws SQLException
        {
            return new QueryTimeoutDecoratedStatement( super.createStatement( resultSetType, resultSetConcurrency ) );
        }

        @Override
        public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency )
                throws SQLException
        {
            return new QueryTimeoutDecoratedPreparedStatement(
                    super.prepareStatement( sql, resultSetType, resultSetConcurrency ) );
        }

        @Override
        public Statement createStatement( int resultSetType, int resultSetConcurrency, int resultSetHoldability )
                throws SQLException
        {
            return new QueryTimeoutDecoratedStatement(
                    super.createStatement( resultSetType, resultSetConcurrency, resultSetHoldability ) );
        }

        @Override
        public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency,
                                                   int resultSetHoldability )
                throws SQLException
        {
            return new QueryTimeoutDecoratedPreparedStatement(
                    super.prepareStatement( sql, resultSetType, resultSetConcurrency, resultSetHoldability ) );
        }

        @Override
        public PreparedStatement prepareStatement( String sql, int autoGeneratedKeys )
                throws SQLException
        {
            return new QueryTimeoutDecoratedPreparedStatement( super.prepareStatement( sql, autoGeneratedKeys ) );
        }

        @Override
        public PreparedStatement prepareStatement( String sql, int[] columnIndexes )
                throws SQLException
        {
            return new QueryTimeoutDecoratedPreparedStatement( super.prepareStatement( sql, columnIndexes ) );
        }

        @Override
        public PreparedStatement prepareStatement( String sql, String[] columnNames )
                throws SQLException
        {
            return new QueryTimeoutDecoratedPreparedStatement( super.prepareStatement( sql, columnNames ) );
        }
    }


    private static class QueryTimeoutDecoratedStatement
            extends DecoratedStatement
    {
        public QueryTimeoutDecoratedStatement( Statement statement )
        {
            super( statement );
        }

        @Override
        public void setQueryTimeout( int seconds )
                throws SQLException
        {
            // ignore
        }
    }

    private static class QueryTimeoutDecoratedPreparedStatement
            extends DecoratedPreparedStatement
    {
        private QueryTimeoutDecoratedPreparedStatement( PreparedStatement statement )
        {
            super( statement );
        }

        @Override
        public void setQueryTimeout( int seconds )
                throws SQLException
        {
            // ignore
        }
    }

}