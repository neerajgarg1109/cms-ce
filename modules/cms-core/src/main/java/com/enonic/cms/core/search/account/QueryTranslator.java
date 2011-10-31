package com.enonic.cms.core.search.account;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.enonic.cms.core.search.SearchSortOrder;

import static org.elasticsearch.index.query.FilterBuilders.matchAllFilter;
import static org.elasticsearch.index.query.FilterBuilders.prefixFilter;
import static org.elasticsearch.index.query.FilterBuilders.termsFilter;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

@Component
public final class QueryTranslator
{
    private static final Logger LOG = LoggerFactory.getLogger( QueryTranslator.class );

    public SearchSourceBuilder build(AccountSearchQuery query)
    {
        final SearchSourceBuilder builder = new SearchSourceBuilder()
            .explain( true )
            .from( query.getFrom() )
            .size( query.getCount() );

        builder.query( buildQuery( query ));

        final TermsFacetBuilder typeFacet = FacetBuilders.termsFacet( "type" )
            .field( AccountIndexField.TYPE_FIELD.id() )
            .allTerms( true );

        final TermsFacetBuilder userStoreFacet = FacetBuilders.termsFacet( "userstore" )
            .field( AccountIndexField.USERSTORE_FIELD.id() + ".untouched" )
            .allTerms( true );

        builder.facet( typeFacet ).facet( userStoreFacet );

        setupSorting(query, builder);

        LOG.info( "Search query: " + builder.toString() );

        return builder;
    }

    private void setupSorting( AccountSearchQuery query, SearchSourceBuilder searchBuilder )
    {
        if ( query.getSortField() != null )
        {
            final SortOrder sortOrder = query.getSortOrder() == SearchSortOrder.ASC ? SortOrder.ASC : SortOrder.DESC;
            final AccountIndexField sortField = query.getSortField();

            final String fieldName;
            switch ( sortField )
            {
                case DISPLAY_NAME_FIELD:
                case USERSTORE_FIELD:
                case NAME_FIELD:
                    fieldName = sortField.id() + ".untouched";
                    break;

                default:
                    fieldName = sortField.id();
            }
            searchBuilder.sort( fieldName, sortOrder );
        }
        else if ( query.getQuery().isEmpty() )
        {
            // no query and not sort field specified => sort by account type (1st users 2nd groups), and then by display name
            searchBuilder.sort( AccountIndexField.TYPE_FIELD.id(), SortOrder.DESC );
            searchBuilder.sort( AccountIndexField.DISPLAY_NAME_FIELD.id() + ".untouched", SortOrder.ASC );
        }
        else
        {
            // default sort
            FieldSortBuilder sortBuilder = SortBuilders.fieldSort( AccountIndexField.LAST_MODIFIED_FIELD.id() )
                .order( SortOrder.DESC ).missing( "_last" );
            searchBuilder.sort( sortBuilder );
        }
    }

    private FilterBuilder buildFilter( AccountSearchQuery query )
    {
        final String queryStr = query.getQuery().trim().toLowerCase();
        final String[] queryTerms = StringUtils.split( queryStr, " ," );
        if ( queryTermsEmpty( queryTerms ) )
        {
            return matchAllFilter();
        }

        if ( queryTerms.length == 1 )
        {
            return buildFilterTerm( queryStr );
        }

        AndFilterBuilder andFilter = FilterBuilders.andFilter();
        for ( String queryTerm : queryTerms )
        {
            FilterBuilder termFilter = buildFilterTerm( queryTerm.trim() );
            andFilter.add( termFilter );
        }
        return andFilter;
    }

    private boolean queryTermsEmpty( final String[] queryTerms )
    {
        for ( String term : queryTerms )
        {
            if ( StringUtils.isNotBlank( term ) )
            {
                return false;
            }
        }
        return true;
    }

    private FilterBuilder buildFilterTerm( final String term )
    {
        final String searchString;
        if ( term.endsWith( "*" ) && ( term.length() > 1 ) )
        {
            searchString = StringUtils.substringBeforeLast( term, "*" );
        }
        else
        {
            searchString = term;
        }
        return FilterBuilders.orFilter( prefixFilter( AccountIndexField.NAME_FIELD.id(), searchString ),
                                        prefixFilter( AccountIndexField.DISPLAY_NAME_FIELD.id(), searchString ),
                                        prefixFilter( AccountIndexField.FIRST_NAME_FIELD.id(), searchString ),
                                        prefixFilter( AccountIndexField.EMAIL_FIELD.id(), searchString ) );

    }

    private FilterBuilder buildFilterTermStrict( final String term )
    {
        if ( term.endsWith( "*" ) && (term.length() > 1) )
        {
            final String prefix = StringUtils.substringBeforeLast( term, "*" );
            return FilterBuilders.orFilter(
                prefixFilter( AccountIndexField.NAME_FIELD.id(), prefix ),
                prefixFilter( AccountIndexField.DISPLAY_NAME_FIELD.id(), prefix ),
                prefixFilter( AccountIndexField.FIRST_NAME_FIELD.id(), prefix ),
                prefixFilter( AccountIndexField.EMAIL_FIELD.id(), prefix )
            );
        }
        else
        {
            return FilterBuilders.orFilter(
                termsFilter( AccountIndexField.NAME_FIELD.id(), term ),
                termsFilter( AccountIndexField.DISPLAY_NAME_FIELD.id(), term ),
                termsFilter( AccountIndexField.FIRST_NAME_FIELD.id(), term ),
                termsFilter( AccountIndexField.EMAIL_FIELD.id(), term )
            );
        }
    }

    private QueryBuilder buildQuery( AccountSearchQuery query )
    {
        final BoolQueryBuilder qb = boolQuery();

        qb.must( filteredQuery( matchAllQuery(), buildFilter( query ) ) );

        String[] userStores = query.getUserStores();
        if ( userStores != null )
        {
            qb.must( termsQuery( AccountIndexField.USERSTORE_FIELD.id() + ".untouched", userStores ) );
        }

        if ( ! query.getUsers() )
        {
            qb.mustNot( termQuery( AccountIndexField.TYPE_FIELD.id(), AccountType.USER.name().toLowerCase() ) );
        }
        if ( ! query.getGroups() )
        {
            qb.mustNot( termQuery( AccountIndexField.TYPE_FIELD.id(), AccountType.GROUP.name().toLowerCase() ) );
        }

        if ( qb.hasClauses() )
        {
            return qb;
        }
        else
        {
            return matchAllQuery();
        }
    }
}
