package teck.kmsys.solr;

import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.Query;

public interface EdismaxQuery extends Query, FilterQuery {
    String getQueryField();

    void addQueryField(String fieldName);

    void addQueryField(String fieldName,double boost);

    void setMinimumMatchPercent(int percent);

    void setMinimumMatchCount(int count);
}
