package net.kmsys.test;

import net.kmsys.springsolredismax.EdismaxQuery;
import net.kmsys.springsolredismax.SimpleEdismaxQuery;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.DefaultQueryParser;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.stereotype.Component;

@Component
public class EdismaxTest implements CommandLineRunner {
    @Autowired
    private SolrTemplate solrTemplate;
    @Value("${solr.core.name}")
    private String solrCoreName;

    public Page<Content> findWithCustomEdismaxCriteria(String searchText,
                                                       String lang,
                                                       String fieldName,
                                                       Criteria criteria,
                                                       Pageable pageable) {

        System.out.println("criteria is : " + criteria);

        //create instance of edismaxQuery
        EdismaxQuery edismaxQuery = new SimpleEdismaxQuery();
        //add criteria
        edismaxQuery.addCriteria(new SimpleStringCriteria(searchText));
        //set pageable
        edismaxQuery.setPageRequest(pageable);
        //set minimum match percent(not applied if criteria contains boolean operator like AND, OR, etc..)
        edismaxQuery.setMinimumMatchPercent(1);
        //create new instance of FilterQuery
        FilterQuery fq = new SimpleFilterQuery(criteria);
        //add filter query
        edismaxQuery.addFilterQuery(fq);
        //add query filed with boost
        edismaxQuery.addQueryField(fieldName, 2.0);
        //search at another field with language to apply language analyzer on it
        if (lang != null)
            edismaxQuery.addQueryField(fieldName + "_" + lang, 2.0);

        //print the final query
        System.out.println("solr query is: " + new DefaultQueryParser(null).getQueryString(fq, null));

        //get result from Solr Core
        Page<Content> solrDocuments = solrTemplate.query(solrCoreName, edismaxQuery, Content.class);
        return solrDocuments;
    }

    @Override
    public void run(String... args) throws Exception {
        intialIndex();

        Criteria criteria = new SimpleStringCriteria("field2:value2");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Content> result = findWithCustomEdismaxCriteria("value1", "en", "field1", criteria, pageable);

        System.out.println(result);
    }

    public void intialIndex() {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("field1", "value1");
        document.addField("field2", "value2");
        solrTemplate.saveDocument(solrCoreName, document);
        solrTemplate.commit(solrCoreName);
    }
}
