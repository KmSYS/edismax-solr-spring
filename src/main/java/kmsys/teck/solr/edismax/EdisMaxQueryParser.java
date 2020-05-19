package kmsys.teck.solr.edismax;

import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.solr.core.DefaultQueryParser;
import org.springframework.data.solr.core.QueryParserBase;
import org.springframework.data.solr.core.mapping.SolrPersistentEntity;
import org.springframework.data.solr.core.mapping.SolrPersistentProperty;
import org.springframework.data.solr.core.query.AbstractQueryDecorator;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.lang.Nullable;

public class EdisMaxQueryParser extends QueryParserBase<SolrDataQuery> {

    private MappingContext<? extends SolrPersistentEntity<?>, SolrPersistentProperty> mappingContext;

    DefaultQueryParser defaultQueryParser;

    public EdisMaxQueryParser(@Nullable MappingContext<? extends SolrPersistentEntity<?>, SolrPersistentProperty> mappingContext){
        super(mappingContext);
        this.mappingContext = mappingContext;
        this.defaultQueryParser = new DefaultQueryParser(mappingContext);
    }

    @Override
    public SolrQuery doConstructSolrQuery(SolrDataQuery edismaxQuery, Class<?> aClass) {

        // just use the default parser to construct the query string in first place.
        SolrQuery target = defaultQueryParser.constructSolrQuery(edismaxQuery, aClass);

        // add missing parameters
        target.add("defType", "edismax");
        SimpleEdismaxQuery query = ((SimpleEdismaxQuery)((AbstractQueryDecorator)edismaxQuery).getDecoratedQuery());
        target.add("qf", query.getQueryField());
        if(query.getMinimumMatch()!=null && !query.getMinimumMatch().isEmpty())
            target.add("mm",query.getMinimumMatch());
        return target;
    }
}
