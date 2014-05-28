#! /bin/bash

curl -XPOST "$1/stories/story/$2/_update" -d '{
    "script" : "ctx._source.icon = \"fa-bookmark\""
}'

