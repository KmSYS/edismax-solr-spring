
## Prerequisites

* Install Apache Solr `http://lucene.apache.org/solr/downloads.html`
* create new core with name content `bin/solr create -c content`
* have a basic knowledge about Apache Solr 

## Dismax Query parser
A Dismax query is nothing but a union of documents produced by the sub-queries and scores each document produced by the sub-query. In general, the DisMax query parser’s interface is more like that of Google than the interface of the standard Solr request handler. This similarity makes DisMax the appropriate query parser for many consumer applications.

#### The commonly used query parameters are:

* q – Defines the raw input strings for the query.
* qf – Query Fields: specifies the fields in the index on which to perform the query. If absent, defaults to df.
* bq – Boost Query: specifies a factor by which a term or phrase should be “boosted” in importance when
considering a match.

## The eDismax Query Parser

The eDisMax(Extended DisMax) query parser is an improved version of the `the-dismax-query-parser`

As Dismax had a lot of limitations, EDismax query parser was added, So Extended Dismax:

* supports the-standard-query-parser syntax such as (non-exhaustive list):
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

`mm`::
 Minimum should match.  See the the-dismax-query-parser for a description of `mm`. The default eDisMax `mm` value differs from that of DisMax:
+
* The default `mm` value is 0%:
** if the query contains an explicit operator other than "AND" ("-", "+", "OR", "NOT"); or
** if `q.op` is "OR" or is not specified.
* The default `mm` value is 100% if `q.op` is "AND" and the query does not contain any explicit operators other than "AND".

`boost`::
A multivalued list of strings parsed as function-queries whose results will be multiplied into the score from the main query for all matching documents. This parameter is shorthand for wrapping the query produced by eDisMax 
