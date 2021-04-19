WikiFactCrawl accepts any Wikidata topic and returns a list of facts about related
topics.  The facts are of form subject, property, object:

For example: 

Joan of England place of birth: Château d'Angers

WikiFactCrawl uses a relevancy heuristic and a breadth-first search of the 
public WikiData knowledge graph.  It locates related topics of interest and
collects facts about those topics.

This source code is for the Java library.

The library is currently available online as a rest endpoint.:
```http://molten-method-309519.uc.r.appspot.com/q?wikiDataID=wd:Q42&maxDepth=2```
where 
*Q42 is the WikiData entity id.  To find an id for any topic,
visit wikidata.org and search for your topic.  The entity id
is shown at the top of the page
*3 is the maximum depth of the crawl. Please note the expansion of searching
is nearly exponential and this number does not need to be high to return
lots of results.


Algorithm
================
Parameters: 
```
root:= WikiData unique identifier for any topic
maxDepth:= how many links away from root to crawl
```

Pseudo-Code:
```
Queue uncrawled:= []
list crawled:= []
list facts:= []
uncrawled.enqueue(root)
while uncrawled has items
    item = uncrawled.dequeue()
    if distance(item, root) < maxDepth
        uncrawled.enqueue(synonyms of item not in crawled)
        uncrawled.enqueue(relatives of item not in crawled)
        uncrawled.enqueue(lists inside item not in crawled)
    crawled.add(i)
    facts.add(all facts of item)
loop
print facts
```
Output:
The list of facts about topics related to root

Technical details
================
The four tasks of crawling topic are run in parallel
*Synonyms
*Relatives
*Lists
*Facts

The crawl follows through links which are subproperties of certain parent classes.
The links can be any number of levels of subproperty below the parent class.  Each
task is accomplished by querying WikiData's SparQL endpoint.  The specific queries
and parent property classes can be found in the code. The exact heuristics
used to crawl will evolve with future versions.

Written by Jae Muzzin
