package info.clock.factcrawl;

import com.bordercloud.sparql.SparqlClient;
import com.bordercloud.sparql.SparqlClientException;
import com.bordercloud.sparql.SparqlResult;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jae
 */
public class Wikidata {    
    
    public Item makeItem(String id){
        return new Item(id, "");
    }
    private String clean(String url) {
        if (url.contains(":") && !url.contains("//") || url.startsWith("<")) {
            return url;
        } else {
            return "<" + url + ">";
        }
    }

    public List<Item> getSynonyms(Item item) {
        ArrayList<Item> r = new ArrayList<>();
        String endpointUrl = "https://query.wikidata.org/sparql";

        String querySelect = "\n"
                + "SELECT ?item ?itemLabel \n"
                + "WHERE\n"
                + "{  \n"
                + "           {?item wdt:P31 " + item + "} \n"
                + "  UNION  \n"
                + "           {?item wdt:P279 " + item + "}\n"
                + " . SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }"
                + "}\n"
                + "order by ?item";

        try {
            ArrayList<HashMap<String, Object>> data = retrieveData(endpointUrl, querySelect);
            for (HashMap row : data) {
                r.add(new Item(clean(row.get("item").toString()), row.get("itemLabel").toString()));
            }
        } catch (SparqlClientException eex) {
            eex.printStackTrace();
        }
        return r;
    }

    public List<String> getLists(Item item) {
        ArrayList<String> r = new ArrayList<>();
        String endpointUrl = "https://query.wikidata.org/sparql";

        String querySelect = "\n"
                + "SELECT distinct ?list\n"
                + "WHERE\n"
                + "{  \n"
                + "           {" + item + " wdt:P2354 ?list} \n"
                + " . SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }"
                + "}\n"
                + "order by ?item";

        try {
            ArrayList<HashMap<String, Object>> data = retrieveData(endpointUrl, querySelect);
            for (HashMap row : data) {
                r.add(clean(row.get("list").toString()));
            }
        } catch (SparqlClientException eex) {
            eex.printStackTrace();
        }
        //any properties that are subproperties of list_of
        querySelect = "\n"
                + "SELECT distinct ?list\n"
                + "WHERE\n"
                + "{  \n"
                + "     {?p wdt:P1647+ wd:P2354}."
                + "     {?p wikibase:directClaim ?prop}."
                + "     {" + item + " ?prop ?list}  \n"
                + "}\n"
                + "order by ?list";

        try {
            ArrayList<HashMap<String, Object>> data = retrieveData(endpointUrl, querySelect);
            for (HashMap row : data) {
                r.add(clean(row.get("list").toString()));
            }
        } catch (SparqlClientException eex) {
            eex.printStackTrace();
        }
        return r;
    }

    public List<Item> getMembersOf(Item item) {
        ArrayList<Item> r = new ArrayList<>();
        String endpointUrl = "https://query.wikidata.org/sparql";
// any items related to me by property that is subproperty of part of
        String querySelect = "\n"
                + "SELECT distinct ?item ?itemLabel ?propR \n"
                + "WHERE\n"
                + "{  \n"//below says any prop that is subprop of "part of" or "significant person", not referenced by or its inverse
                + "     {{?prop wdt:P1647+ wd:P361}UNION{?prop wdt:P1647+ wd:P3342}}."
                + "  {?prop wikibase:directClaim ?propR}."
                + "       {?item ?propR " + item + "}. \n"
                + "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }."
                + "}\n"
                + "order by ?item";

        try {
            ArrayList<HashMap<String, Object>> data = retrieveData(endpointUrl, querySelect);
            for (HashMap row : data) {
                if(row.get("itemLabel").toString().contains("Dictionary")){
                     System.err.println(item.toString() + " " + row.get("propR") + " " + row.get("itemLabel"));
                }
                r.add(new Item(clean(row.get("item").toString()), row.get("itemLabel").toString()));
            }
        } catch (SparqlClientException eex) {
            eex.printStackTrace();
        }
// any items follows me or preceeds me (eg uss-enteprise c, d, e)
        querySelect = "\n"
                + "SELECT distinct ?item ?itemLabel \n"
                + "WHERE\n"
                + "{  \n"
                + "     {" + item + " wdt:P155+ ?item}UNION{"+item+" wdt:P156+ ?item}.  \n"
                + " SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }"
                + "}\n"
                + "order by ?item";

        try {
            ArrayList<HashMap<String, Object>> data = retrieveData(endpointUrl, querySelect);
            for (HashMap row : data) {
                r.add(new Item(clean(row.get("item").toString()), row.get("itemLabel").toString()));
            }
        } catch (SparqlClientException eex) {
            eex.printStackTrace();
        }
        return r;
    }

    public List<Item> getMembers(String list) {
        ArrayList<Item> r = new ArrayList<>();
        String endpointUrl = "https://query.wikidata.org/sparql";

        String querySelect = "\n"
                + "SELECT distinct ?item ?itemLabel \n"
                + "WHERE\n"
                + "{  \n"
                + "           {?item wdt:P361 " + list + "}\n"
                + " . SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }"
                + "}\n"
                + "order by ?item";

        try {
            ArrayList<HashMap<String, Object>> data = retrieveData(endpointUrl, querySelect);
            for (HashMap row : data) {
                r.add(new Item(clean(row.get("item").toString()), row.get("itemLabel").toString()));
            }
        } catch (SparqlClientException eex) {
            eex.printStackTrace();
        }

        //all properties that are subproperty of member_of
        querySelect = "\n"
                + "SELECT distinct ?item ?itemLabel \n"
                + "WHERE\n"
                + "{  \n"
                + "           {?prop wdt:P361+ " + list + "}\n"
                + "           {?item ?prop " + list + "}\n"
                + " . SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }"
                + "}\n"
                + "order by ?item";

        try {
            ArrayList<HashMap<String, Object>> data = retrieveData(endpointUrl, querySelect);
            for (HashMap row : data) {
                r.add(new Item(clean(row.get("item").toString()), row.get("itemLabel").toString()));
            }
        } catch (SparqlClientException eex) {
            eex.printStackTrace();
        }
        return r;
    }

    public HashMap<String, String> getProperties(Item item) {
        HashMap<String, String> r = new HashMap<String, String>();
        String endpointUrl = "https://query.wikidata.org/sparql";

        String querySelect = "\n"
                + "SELECT distinct ?pLabel ?valueLabel\n"
                + "WHERE\n"
                + "{  \n"
                + "           {" + item + " ?prop ?value } . "
                + "           MINUS {" + item + " wdt:P373 ?value } . "
                + "           MINUS {" + item + " wdt:P1343 ?value } . "
                + "{{?p wikibase:directClaim ?prop} . "
                + "FILTER NOT EXISTS {?p wikibase:propertyType wikibase:ExternalId}. "
                + "FILTER NOT EXISTS {?p wikibase:propertyType wikibase:CommonsMedia} ."
                + "FILTER NOT EXISTS {?p wikibase:propertyType wikibase:Url}} ."
                + "SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }\n"
                + "}\n"
                + "order by ?pLabel";

        try {
            ArrayList<HashMap<String, Object>> data = retrieveData(endpointUrl, querySelect);
            for (HashMap row : data) {
                r.put(row.get("pLabel").toString(), row.get("valueLabel").toString());
            }
        } catch (SparqlClientException eex) {
            System.out.println(querySelect);
            eex.printStackTrace();
        }
        return r;
    }

    public static ArrayList<HashMap<String, Object>> retrieveData(String endpointUrl, String query) throws SparqlClientException {
        SparqlClient sp = new SparqlClient();
        sp.setEndpointRead(URI.create(endpointUrl));
        SparqlResult result = sp.query(query);
        return result.getModel().getRows();
    }

}
