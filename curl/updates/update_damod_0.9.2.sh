#! /bin/bash


#
# update stories in neo4j
#
echo update stories on neo4j

curl -H "accept: applicaton/json" -H "X-Stream:true" -H "content-type:application/json" "$1/db/data/cypher" -d '{
  "query": "match (s:Story) set s.icon=\"fa-bookmark\"",
  "params": {
  }
}'



#
# for each story in neo4j update index in elastic-search
#
echo update stories on elasticsearch

storyIds=`curl -s -H "accept: applicaton/json" -H "X-Stream:true" -H "content-type:application/json" "$1/db/data/cypher" -d '{
  "query": "match (s:Story) return s.id",
  "params": {
  }
}' | grep -o "\"[a-z0-9]*\"" | sed "s/\"//g"`

for storyId in $storyIds;
do
  echo updating story $storyId on elasticsearch...
  curl -s -XPOST "$2/stories/story/$storyId/_update" -d '{
    "script" : "ctx._source.icon = \"fa-bookmark\""
  }'
done



