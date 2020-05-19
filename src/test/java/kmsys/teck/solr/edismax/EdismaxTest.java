package kmsys.teck.solr.edismax;

import kmsys.teck.solr.config.SolrConfig;
import kmsys.teck.solr.edismax.model.Product;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SolrConfig.class)
public class EdismaxTest {

    @Autowired
    private SolrTemplate solrTemplate;
    private String solrCoreName = "product";

    @Before
    public void clearSolrData() {
        solrTemplate.delete(solrCoreName, new SimpleQuery("*:*"));
    }

    @Test
    public void whenSearchingProductsByNamedQuery_thenAllMatchingProductsShouldAvialble() {
        final Product phone = new Product();
        phone.setId("P0001");
        phone.setName("Smart Phone");
        save(phone);

        final Product phoneCover = new Product();
        phoneCover.setId("P0002");
        phoneCover.setName("Phone Cover");
        save(phoneCover);

        final Product wirelessCharger = new Product();
        wirelessCharger.setId("P0003");
        wirelessCharger.setName("Phone Charging Cable");
        save(wirelessCharger);


        Page<Product> productPage = findWithCustomEdismaxCriteria("Phone", null, "name",
                new SimpleStringCriteria("id:" + wirelessCharger.getId()),
                PageRequest.of(0, 1));
        assertEquals(1, productPage.getNumberOfElements());
    }


    private void save(Product product) {
        SolrInputDocument document = new SolrInputDocument();

        if (product.getId() != null)
            document.addField("id", product.getId());
        if (product.getName() != null)
            document.addField("name", product.getName());
        solrTemplate.saveDocument(solrCoreName, document);
        solrTemplate.commit(solrCoreName);
    }

    private Page<Product> findWithCustomEdismaxCriteria(String searchText,
                                                        String lang,
                                                        String fieldName,
                                                        Criteria criteria,
                                                        Pageable pageable) {

        //create instance of edismaxQuery
        EdismaxQuery edismaxQuery = new SimpleEdismaxQuery();
        //add criteria
        if (searchText != null)
            edismaxQuery.addCriteria(new SimpleStringCriteria(searchText));
        //set pageable
        if (pageable != null)
            edismaxQuery.setPageRequest(pageable);
        //set minimum match percent(not applied if criteria contains boolean operator(AND, OR, NOT) )
        edismaxQuery.setMinimumMatchPercent(50);
        //add filter query
        if (criteria != null)
            edismaxQuery.addFilterQuery(new SimpleFilterQuery(criteria));
        //add query filed with boost
        if (fieldName != null)
            edismaxQuery.addQueryField(fieldName, 2.0);
        //search at another field with language to apply language analyzer on it
        if (lang != null && fieldName != null)
            edismaxQuery.addQueryField(fieldName + "_" + lang, 2.0);

        //get result from Solr Core
        Page<Product> solrDocuments = solrTemplate.query(solrCoreName, edismaxQuery, Product.class);
        return solrDocuments;
    }

}
