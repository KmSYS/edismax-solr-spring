package tech.kmsys.solr;

import org.springframework.data.solr.core.query.SimpleQuery;

public class SimpleEdismaxQuery extends SimpleQuery implements EdismaxQuery {

    private String queryField = "";

    private String minimumMatch;

    public String getQueryField() {
        return queryField;
    }

    public String getMinimumMatch() {
        return minimumMatch;
    }

    public void addQueryField(String fieldName) {
        addQueryField(fieldName, -1);
    }

    public void addQueryField(String fieldName, double boost) {
        String qf = (boost > 0.0) ? String.format("%s^%.1f", fieldName, boost) : fieldName;
        if (queryField.isEmpty())
            queryField = qf;
        else
            queryField += " " + qf;
    }

    public void setMinimumMatchPercent(int percent) {
        minimumMatch = String.format("%d%%", percent);
    }

    public void setMinimumMatchCount(int count) {
        minimumMatch = String.format("%d", count);
    }
}
