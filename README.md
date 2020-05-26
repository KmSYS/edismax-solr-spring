
## Prerequisites

* Install Apache Solr `http://lucene.apache.org/solr/downloads.html`
* create new core with name content `bin/solr create -c product`
* have a basic knowledge about Apache Solr 

## Problem
* How to add the following (qf, bq, mm) parameters to the Solr query generated by Spring Data Solr, OR 
* How to use standard query parser and dismax parameters in the same query that  generated by Spring Data Solr, OR
* How to use edismax parameters to the Solr query generated by Spring Data Solr
* so let's take a very brief about `standard query parser`, `dismax query parser ` and finally `edismax query parser `

## Standard Query Parser
the standard query parser can be tricky:
* Conditional Searches: AND, OR, NOT
* Wildcard Searches: *
* Fuzzy searches: ~
* Search Term inclusion/Exclusion: +/-

## Dismax Query parser
Dismax is a subset of `standard query parser` and its purpose is to process queries the simplest way possible and less error messages for the user to deal with.
It can still perform `phrase searches`, `Search Term inclusion/Exclusion: +/-`, `q`, `qf` and `bq`, Don't worry we will dissucess them in edismax section

## eDismax Query Parser

The eDisMax(Extended DisMax) query parser is an improved version of the `the-dismax-query-parser`
It supports both `dismax` and `standard query parser's` syntax and gives better control over the field that can be queried, So edismax:

* supports the-standard-query-parser syntax such as (non-exhaustive list):07h
** boolean operators such as AND (+, &&), OR (||), NOT (-).
** optionally treats lowercase "and" and "or" as "AND" and "OR" in Lucene syntax mode
** optionally allows embedded queries using other query parsers or functions
* includes improved smart partial escaping in the case of syntax errors; fielded queries, +/-, and phrase queries are still supported in this mode.
* improves proximity boosting by using word shingles; you do not need the query to match all words in the document before proximity boosting is applied.
* includes advanced stopword handling: stopwords are not required in the mandatory part of the query but are still used in the proximity boosting part. If a query consists of all stopwords, such as "to be or not to be", then all words are required.
* includes improved boost function: in Extended DisMax, the `boost` function is a multiplier <<the-dismax-query-parser.adoc#bq-bf-shortcomings,rather than an addend>>, improving your boost results; the additive boost functions of DisMax (`bf` and `bq`) are also supported.
* supports pure negative nested queries: queries such as `+foo (-foo)` will match all documents.
* lets you specify which fields the end user is allowed to query, and to disallow direct fielded searches.

#### The commonly used query parameters are:

In addition to all the the-dismax-query-parser, Extended DisMax includes these query parameters:

* `q` (query): Defines the raw input strings for the query.
* `q.alt` (alternative query) :defines a query that will be excuted when `q` is blank or absent.
* `qf` (Query Fields) : specifies the fields in the index on which to perform the query. If absent, defaults to df.
* `bq` (Boost Query) : specifies a factor by which a term or phrase should be “boosted” in importance when
considering a match.
* `mm` (Minimum should match): in any query we have  three types of clauses: (Mandatory(+), Prohibited(-), and Optional)
By default, all words or phrases specified in the q parameter are treated as "optional" clauses unless they are preceded by a "+" or a "-",
so the `mm` works only on optional terms, let's have examples to understand: 
* if mm=3    then it must match at least 3 optional terms 
* if mm=-3   then it must match at least (total-2) optional terms
* if mm=90%  then it must match at least 90% of optional terms
* if mm=-10% then it must Ignore at Most 10% of optional terms, or it should gave same result like (mm=90%)
* If q.op is effectively AND’ed, then mm=100%; if q.op is OR’ed, then mm=1. 
So it should set a default value for the 'mm' parameter in solrconfig.xml file.

* `boost`: Boost Query: specifies a factor by which a term or phrase should be "boosted" in importance when considering a match.

## Code Explanation

### `EdismaxTest` class

* First thing we will Implement it: is method for clear(or delete) data from solr core before run our test method: 

```  
    @Before
    public void clearSolrData() {
        solrTemplate.delete(solrCoreName, new SimpleQuery("*:*"));
    }
```

* use edismax to search by query with `MinumMatch` and `Boost` parameters by creating new method with name  `findWithCustomEdismaxCriteria`:

```

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
    
```

* test(call) `findWithCustomEdismaxCriteria` method:
```

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

```

* Another Implementation for `findWithCustomEdismaxCriteria` method: 

```

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
```

* Test(Call) the second Implementation for `findWithCustomEdismaxCriteria`:

```
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
```
