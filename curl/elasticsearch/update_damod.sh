#! /bin/bash

curl -XPOST "$1/stories/story/$2/_update" -d '{
    "script" : "ctx._source.modified = ctx._source.created"
}'

curl -XPOST "$1/stories/story/$2/_update" -d '{
    "script" : "ctx._source.modifiedBy = \"juergen\""
}'

curl -XPOST "$1/stories/story/$2/_update" -d '{
    "script" : "ctx._source.createdBy = \"juergen\""
}'