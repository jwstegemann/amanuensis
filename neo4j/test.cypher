MATCH (u:User {login: "dummy"})
MATCH (n:Story {id: "52f101192fcd935400587fd5"})
WHERE (n)<-[:canWrite|:canGrant*1..5]-(u)
OPTIONAL MATCH (n)<-[:canRead]-(x:User)
OPTIONAL MATCH (n)<-[:canWrite]-(y:User)
OPTIONAL MATCH (n)<-[:canGrant]-(z:User)
WITH n,u,collect(x) as readers, collect(y) as writers, collect(z) as granters
CREATE (n)-[r:Slot {name: "Unterstories"}]->(m:Story {id: "5356618047dc8a1f0074c9b3", title: "Unterstory 2", content: "Unterstory 2", created: "2014-04-22T14:33:04.825+02:00",, createdBy: "Dummy"})
WITH m,u,readers,writers,granters
FOREACH (reader IN readers |
  MERGE (m)<-[:canRead]-(reader))
FOREACH (writer IN writers |
  MERGE (m)<-[:canWrite]-(writer))
FOREACH (granter IN granters |
  MERGE (m)<-[:canGrant]-(granter))
  MERGE (m)<-[:canGrant]-(u)
FOREACH (tagname IN [] |
  MERGE (t:Tag {name: tagname})
MERGE (m)-[:is]->(t:Tag))
RETURN readers UNION writers UNION granters
