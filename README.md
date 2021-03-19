# ChEBI discovery

[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.5.0.svg)](https://www.scala-js.org)

- ScalaJs functional implementation based of the ontology based matching describe in the 
  *"Improving lipid mapping in Genome Scale Metabolic Networks using ontologies."* [[1]](#1) using the 
  [Chemical Entities of Biological Interest (ChEBI)](https://www.ebi.ac.uk/chebi/)

  
### Javascript Examples 

- input : a list of CheBid
- output : list of object `{uri1: <string> , uri2: <string>, prop: <string> , score: <double>}` 

```javascript=
  var config = `
        {
             "sources" : [{
               "id"  : " - Sparql Endpoint - ",
               "url" : "http://localhost:8890/sparql"
             }]
        }
        `

        {
        var lIdChebis = ["http://purl.obolibrary.org/obo/CHEBI_15756","http://purl.obolibrary.org/obo/CHEBI_7896"]

        ChebiDiscovery(config)
           .ontology_based_matching(lIdChebis)
               .then ( (lTupleObject) => {
               $("#metabolomics2network1")
                 .html("<h3>ontology based matching of</h3> <pre>"+ JSON.stringify(lIdChebis)+"</pre><br/><code style=\"color:#FF0000\">"+JSON.stringify(lTupleObject)+"</code>")
                })
                .catch( msg => { console.error(msg) ; } ) ;
        }
 ```

see example : [test.html](./test.html)

### Scala Example

```scala=
  ChebiDiscovery().ontology_based_matching(List("http://purl.obolibrary.org/obo/CHEBI_36023",
        "http://purl.obolibrary.org/obo/CHEBI_30828"))
        .map( (response : Seq[(URI,URI,String,Double)]) =>{
          response.foreach(println)
        })
```

see example : [test](./src/test/scala)

### Dev/Test environment (sparql endpoint with ChEBI.owl)

``` 
docker-compose up -d
```
check if the chebi ontology is loaded with `docker-compose logs`

open ./test.html in the web browser.


## References
<a id="1">[1]</a>
Poupin, N., Vinson, F., Moreau, A. et al. Improving lipid mapping in Genome Scale Metabolic Networks using ontologies. Metabolomics 16, 44 (2020). https://doi.org/10.1007/s11306-020-01663-5
