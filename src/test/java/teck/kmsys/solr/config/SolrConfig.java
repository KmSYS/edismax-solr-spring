package teck.kmsys.solr.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.mapping.SimpleSolrMappingContext;
import teck.kmsys.solr.EdisMaxQueryParser;
import teck.kmsys.solr.SimpleEdismaxQuery;

@Configuration
@ComponentScan
public class SolrConfig {
    private String solrHost = "http://localhost:8983/solr/";

    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder(solrHost).build();
    }

    @Bean
    public SolrTemplate solrTemplate(SolrClient client) {
        SolrTemplate template = new SolrTemplate(client) {

            @Override
            public void afterPropertiesSet() {
                super.afterPropertiesSet();
                registerQueryParser(SimpleEdismaxQuery.class, new EdisMaxQueryParser(new SimpleSolrMappingContext()));
            }
        };

        template.afterPropertiesSet();
        return template;
    }

}