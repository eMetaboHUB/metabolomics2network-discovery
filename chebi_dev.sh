#!/bin/bash

bash /virtuoso.sh &

if [ ! -f "./chebi.owl" ]; then
  # install dependancies
  apt-get update && apt-get install wget -y
  # download resource
  wget ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi.owl

  sleep 2
  cat << EOF | isql-v 1111 dba "${DBA_PASSWORD}"
ld_dir_all ('./', 'chebi.owl', 'https://forum.semantic-metabolomics.org/chebi');
select * from DB.DBA.load_list;
rdf_loader_run();
EOF
fi

echo
echo " ===================================================================  "
echo "  ChEBI is loaded !"
echo "  SPARQL ENDPOINT ==> http://localhost:8890/sparql                    "
echo "  Test Files : ./test.html "
echo " ===================================================================  "
echo

wait