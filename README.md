
## Prerequisites

* install solr
* create new core with name content 
`bin/solr create -c content`
* have a knowledge about solr basics

# The eDismax Query Parser Overview

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

## Extended DisMax Parameters

In addition to all the <<the-dismax-query-parser.adoc#dismax-query-parser-parameters,DisMax parameters>>, Extended DisMax includes these query parameters:

`mm`::
 Minimum should match.  See the <<the-dismax-query-parser.adoc#mm-minimum-should-match-parameter,DisMax mm parameter>> for a description of `mm`. The default eDisMax `mm` value differs from that of DisMax:
+
* The default `mm` value is 0%:
** if the query contains an explicit operator other than "AND" ("-", "+", "OR", "NOT"); or
** if `q.op` is "OR" or is not specified.
* The default `mm` value is 100% if `q.op` is "AND" and the query does not contain any explicit operators other than "AND".

`boost`::
A multivalued list of strings parsed as <<function-queries.adoc#available-functions,functions>> whose results will be multiplied into the score from the main query for all matching documents. This parameter is shorthand for wrapping the query produced by eDisMax 
