package kmsys.teck.solr.edismax;

import kmsys.teck.solr.config.SolrConfig;
import kmsys.teck.solr.edismax.model.Product;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SolrConfig.class)
@TestPropertySource(properties = "solr.core.name=product")
public class EdismaxTest {

    @Autowired
    private SolrTemplate solrTemplate;
    @Value("${solr.core.name}")
    private String solrCoreName;

    @Before
    public void clearSolrData() {
        solrTemplate.delete(solrCoreName, new SimpleQuery("*:*"));
    }

    @Test
    public void whenSearchingProductsByNamedQueryAndMinumMatchAndBoost_thenAllMatchingProductsShouldAvialble() {
        final Product phone = new Product();
        phone.setId("P0001");
        phone.setName("phone");
        phone.setDescription("term1 term2 term3 term4");
        save(phone);

        final Product phoneCover = new Product();
        phoneCover.setId("P0002");
        phoneCover.setName("phone cover");
        phoneCover.setDescription("term5 term6 term7 term8");
        save(phoneCover);

        final Product wirelessCharger = new Product();
        wirelessCharger.setId("P0003");
        wirelessCharger.setName("wireless charger");
        wirelessCharger.setDescription("term9 term10 term11 term12");
        save(wirelessCharger);

        /********************************************************************************************/
        /* get P0001 & P0003 but ignore P0002 by controlling minimumMatchPercent on searchText */
        Page<Product> productPage = findWithCustomEdismaxCriteria("term1 term2 term6 term10 term11",
                "description", 50, 2, PageRequest.of(0, 10));

        //check size is (2)
        assertEquals(2, productPage.getNumberOfElements());
        //get first product then check it
        Product firstProduct = productPage.getContent().get(0);
        assertEquals(firstProduct.getId(), "P0001");
        assertEquals(firstProduct.getDescription(), "term1 term2 term3 term4");
        //get second product then check it
        Product secondProduct = productPage.getContent().get(1);
        assertEquals(secondProduct.getId(), "P0003");
        assertEquals(secondProduct.getDescription(), "term9 term10 term11 term12");

        /********************************************************************************************/
        /* get P0002 & P0003 but ignore P0001 by controlling minimumMatchPercent on searchText */
        productPage = findWithCustomEdismaxCriteria("term1 term5 term6 term10 term11",
                "description", 50, 2, PageRequest.of(0, 10));

        //get first product then check it
        firstProduct = productPage.getContent().get(0);
        assertEquals(firstProduct.getId(), "P0002");
        assertEquals(firstProduct.getDescription(), "term5 term6 term7 term8");
        //get second product then check it
        secondProduct = productPage.getContent().get(1);
        assertEquals(secondProduct.getId(), "P0003");
        assertEquals(secondProduct.getDescription(), "term9 term10 term11 term12");

        /********************************************************************************************/
        /* make P0003 show first by using boost=2.5 on its terms */
        productPage = findWithCustomEdismaxCriteria("term1 term2 term6 term10^2.5 term11",
                "description", 50, 2, PageRequest.of(0, 10));

        //get first product
        firstProduct = productPage.getContent().get(0);
        assertEquals(firstProduct.getId(), "P0003");
        assertEquals(firstProduct.getDescription(), "term9 term10 term11 term12");
        //get second product
        secondProduct = productPage.getContent().get(1);
        assertEquals(secondProduct.getId(), "P0001");
        assertEquals(secondProduct.getDescription(), "term1 term2 term3 term4");

        /********************************************************************************************/

    }


    @Test
    public void whenSearchingProductsByNamedQueryAndCriteria_thenAllMatchingProductsShouldAvialble() {
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
                PageRequest.of(0, 10));

        //check size is (1)
        assertEquals(1, productPage.getNumberOfElements());
        //get wirelessCharger product
        Product wirelessChargerProduct = productPage.getContent().get(0);
        assertEquals(wirelessChargerProduct.getId(), "P0003");
        assertEquals(wirelessChargerProduct.getName(), "Phone Charging Cable");

    }

    private void save(Product product) {
        SolrInputDocument document = new SolrInputDocument();

        if (product.getId() != null)
            document.addField("id", product.getId());
        if (product.getName() != null)
            document.addField("name", product.getName());
        if (product.getDescription() != null)
            document.addField("description", product.getDescription());

        solrTemplate.saveDocument(solrCoreName, document);
        solrTemplate.commit(solrCoreName);
    }

    private Page<Product> findWithCustomEdismaxCriteria(String searchText,
                                                        String fieldName,
                                                        int minimumMatchPercent,
                                                        int boost,
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
        edismaxQuery.setMinimumMatchPercent(minimumMatchPercent);
        //add filter query
        //add query filed with boost
        if (fieldName != null)
            edismaxQuery.addQueryField(fieldName, boost);

        //get result from Solr Core
        Page<Product> solrDocuments = solrTemplate.query(solrCoreName, edismaxQuery, Product.class);
        return solrDocuments;
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
