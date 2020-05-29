package tech.kmsys.solr;

import org.apache.solr.client.solrj.SolrQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.solr.core.mapping.SimpleSolrMappingContext;
import org.springframework.data.solr.core.query.SimpleStringCriteria;


import static org.junit.jupiter.api.Assertions.*;

class EdisMaxQueryParserTests {


    private EdisMaxQueryParser queryParser;

    @BeforeEach
    public void setUp() {
        this.queryParser = new EdisMaxQueryParser(new SimpleSolrMappingContext());
    }

    @Test
    public void testDoConstructSolrQuery() {
        SimpleEdismaxQuery query = new SimpleEdismaxQuery();

        query.addCriteria(new SimpleStringCriteria("search_Text"));
        query.setMinimumMatchCount(5);
        query.addQueryField("field_1");
        query.addQueryField("field_2",2);
        query.addQueryField("field_3",0.3);

        SolrQuery solrQuery = queryParser.constructSolrQuery(query,EdismaxQuery.class);

        assertEquals(solrQuery.get("q"),"search_Text");
        assertEquals(solrQuery.get("defType"),"edismax");
        assertEquals(solrQuery.get("mm"),"5");

        assertTrue(solrQuery.get("qf").contains("field_1"));
        assertTrue(solrQuery.get("qf").contains("field_2^2"));
        assertTrue(solrQuery.get("qf").contains("field_3^0.3"));

    }

}
