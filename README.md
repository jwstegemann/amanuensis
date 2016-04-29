Amanuensis
=====

Link to color-scheme: http://colorschemedesigner.com/#3.31Tfsibw0w0

Buildpack: heroku config:add BUILDPACK_URL=https://github.com/ddollar/heroku-buildpack-multi.git

---

#Setup-Process:

---

1. Set Config-Variables:

    AUTH_SECRET="..."

    AMANUENSIS_AUTH="true" / "false"

    AMANUENSIS_SECURE_COOKIE="true" / "false"

    AMANUENSIS_USE_FORWARDED_FOR="true" / "false"

    GRAPHENEDB_URL="http://user:pwd@localhost:9200"

    ELASTICSEARCH_URL="http://user:pwd@localhost:9200"

    AWS_S3_KEY="..."
    AWS_S3_SECRET="..."
    AWS_S3_BUCKET="..."

---

2. Init Neo4J-Database by setting up constraints

    create constraint on (s:Story) assert s.id is unique

    create constraint on (u:User) assert u.login is unique

    create constraint on (t:Tag) assert t.name is unique

    create index on :Slot(name)

---


3. Create Index and Define Mappings in ElasticSearch

    curl/elasticsearch/init <url>

---

4. Create public Group in Neo4J

    create (u:User {
        login:"public", 
        pwd:"public",
        name:"everybody",
        permissions:[]
    })   

---

5. Create a User in Neo4J

    curl/admin/create_user.sh <neo4j-url> <elasticsearch-url> <login> <name> <language>


6. Add User to Index

    curl -XPOST "http://localhost:9200/users/user/<username>" -d'
    {
        "login": "<username>"
    }'
    