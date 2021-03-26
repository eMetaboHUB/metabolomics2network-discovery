# ChEBI discovery

[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.5.0.svg)](https://www.scala-js.org)

- ScalaJs functional implementation based of the ontology based matching describe in the 
  *"Improving lipid mapping in Genome Scale Metabolic Networks using ontologies."* [[1]](#1) using the 
  [Chemical Entities of Biological Interest (ChEBI)](https://www.ebi.ac.uk/chebi/)

  
### Javascript Examples 

- input : 
  - `chebid` a reference ChEBI id : `String`
  - `listChebIds` a list of CheBid : `Array[String]` 
  - `score` (optional) max score to reach : `Double` (default : `4.5`)

- output : list of object `{uri: <string>, property: <string> , score: <double>}` 
  - `uri` uri from the input list
  - `property` possible value : `"is_conjugate_acid_of", "is_conjugate_base_of",
    "has_functional_parent","is_tautomer_of","chas_parent_hydride","is_substituent_group_from",
    "is_enantiomer_of"` 
  - `score` distance between `uri` and `chebid` . distance is positive (1.0) when `chebid` matches a more generic class 
    in the list. The distance is slightly increased (by 0.1) when a relation different tha `is_a` of the lipid
    is present in the `listChebIds`

### cdn

```html
<script type="text/javascript" src="https://cdn.jsdelivr.net/gh/emetabohub/chebi-discovery@latest/dist/chebi-discovery-web.js">
```


```html
<script type="text/javascript" src="https://cdn.jsdelivr.net/gh/emetabohub/chebi-discovery@latest/dist/chebi-discovery-web.js"> </script>
<script>
  let config = `{
             "sources" : [{
               "id"  : " - Forum Semantic Metabolomics - ",
			         "url" : "https://forum.semantic-metabolomics.fr/sparql"
             }]
        }`;
        
        
        let chebIdRef="http://purl.obolibrary.org/obo/CHEBI_15756";
        let lIdChebis = ["http://purl.obolibrary.org/obo/CHEBI_7896"];
        let maxDeep = 3.0 ;
        
        ChebiDiscovery(config)
           .ontology_based_matching(chebIdRef,lIdChebis,maxDeep)
               .then ( (lTupleObject) => {
                   let div = document.createElement("div") ;
                   div.innerHTML = "<h3>ontology based matching of</h3> <pre>"+JSON.stringify(lTupleObject)+"</code>" ;
                   document.body.appendChild(div);
               })
               .catch( msg => { console.error(msg) ; } ) ;
                
</script>
<body/>
 ```

see [jsfiddle example](https://jsfiddle.net/ofilangi/6avgw0md/31/)

### Scala Example

see example : [test](./src/test/scala)

### Dev/Test environment (sparql endpoint with ChEBI.owl)

``` 
docker-compose up -d
```
check if the chebi ontology is loaded with `docker-compose logs`

open ./test.html in the web browser.


### Lib generation

```shell=
## devel ->  <script type="text/javascript" src="<root>/chebi-discovery/target/scala-2.13/scalajs-bundler/main/chebi-discovery-fastopt-bundle.js"> </script> 
sbt fastOptJS::webpack
## cdn
./update_cdn_libjs.sh
```

## References
<a id="1">[1]</a>
Poupin, N., Vinson, F., Moreau, A. et al. Improving lipid mapping in Genome Scale Metabolic Networks using ontologies. Metabolomics 16, 44 (2020). https://doi.org/10.1007/s11306-020-01663-5
