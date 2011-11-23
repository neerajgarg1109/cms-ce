package com.enonic.cms.core.search.util;


import org.apache.commons.lang.StringUtils;

import com.enonic.cms.core.content.index.queryexpression.FieldExpr;

/**
 * Created by IntelliJ IDEA.
 * User: rmh
 * Date: 11/23/11
 * Time: 12:06 PM
 */
public class IndexFieldNameResolver
    extends IndexFieldNameConstants
{

    public static String normalizeFieldName( final String fieldName )
    {
        return doNormalizeFieldName( fieldName );
    }

    public static String toFieldName( final FieldExpr expression )
    {
        return doNormalizeFieldName( expression.getPath() );
    }


    private static String doNormalizeFieldName( final String fieldName )
    {
        if ( StringUtils.isBlank( fieldName ) )
        {
            return fieldName;
        }

        return fieldName.replace( QUERY_LANGUAGE_PROPERTY_SEPARATOR, INDEX_FIELDNAME_PROPERTY_SEPARATOR )
            .replace( ".", INDEX_FIELDNAME_PROPERTY_SEPARATOR )
            .replaceAll( "@", "" )
            .toLowerCase();
    }


    public static String getNumericField( String fieldName )
    {
        return fieldName + NUMERIC_FIELD_POSTFIELD;
    }

    public static String getOrderByFieldName( FieldExpr expression )
    {
        return doGetOrderByFieldName( expression.getPath() );
    }

    public static String getOrderByFieldName( String fieldName )
    {
        return doGetOrderByFieldName( fieldName );
    }

    private static String doGetOrderByFieldName( String fieldName )
    {
        return ORDER_FIELD_PREFIX + doNormalizeFieldName( fieldName );
    }

    public static String getCategoryKeyFieldName()
    {
        return CATEGORY_FIELD_PREFIX + "_key";
    }

    public static String getCategoryKeyNumericFieldName()
    {
        return CATEGORY_FIELD_PREFIX + getNumericField( "_key" );
    }

    public static String getSectionKeyNumericFieldName()
    {
        return SECTION_FIELD_PREFIX + getNumericField( ".menuitemkey" );
    }

    public static String getCategoryNameFieldName()
    {
        return CATEGORY_FIELD_PREFIX + "_name";
    }

    public static String getContentTypeKeyFieldName()
    {
        return CONTENT_TYPE_PREFIX + "_key";
    }

    public static String getContentTypeKeyNumericFieldName()
    {
        return CONTENT_TYPE_PREFIX + getNumericField( "_key" );
    }

    public static String getContentTypeNameFieldName()
    {
        return CONTENT_TYPE_PREFIX + "_name";
    }

}
