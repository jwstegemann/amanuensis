#! /bin/bash

if [ $# -lt 4 ]
  then
    echo "create_user <neo4j-url> <elasticsearch-url> <login> <name>";
    exit
fi

#
# update stories in neo4j
#
echo create user on neo4j

query='{
  "query": "match (g:User {login: \"public\"}) create (u:User { login:{login}, pwd:{pwd}, name:{name}, permissions:[] })-[:canGrant]->(g)",
  "params": {
    "login": "$3",
    "pwd": "1eb1e1d6ad08dcf1a1020ae999d24ee836b30f0987acf3934e18783cc551bfc",
    "name": "$4"
  }
}'

replacedQuery=`echo $query | sed "s/\\$3/$3/" | sed "s/\\$4/$4/"`

curl -H "accept: applicaton/json" -H "X-Stream:true" -H "content-type:application/json" "$1/db/data/cypher" -d "$replacedQuery"



#
# for each story in neo4j update index in elastic-search
#
echo create user on elasticsearch

curl -s -XPOST "$2/users/user/" -d' { "login": "$3" }'



