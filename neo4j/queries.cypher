MATCH (s:Story)-[r]-m
WHERE s.id="529a79ff47dc8a0d00bb92e4"
RETURN type(r) as slot, m.id as id, m.title as title, 
CASE 
 WHEN type(r) = 'self' THEN s.content
 ELSE ''
END as content


MATCH (n:Story), (m:Story) 
WHERE n.id="529a79ff47dc8a0d00bb92e4" and m.id="529b7a4f47dc8a0d008bffde" 
CREATE (n)-[r:Testslot2]->(m)


MATCH (n:Story)
WHERE n.id="529a79ff47dc8a0d00bb92e4"
CREATE (n)-[r:self]->(n)


MATCH (s:Story)-[r]-m
WHERE s.id="529a79ff47dc8a0d00bb92e4"
RETURN type(r) as slot, m.id as id, m.title as title, 
CASE 
 WHEN type(r) = 'self' THEN s.content
 ELSE ''
END as content


---

graph paths

start: 52f101192fcd935400587fd5
ziel: 52f1020f2fcd935c00587fdb

match p=(start:Story {id: "52f101192fcd935400587fd5"})-[:Slot*1..10]-(ziel:Story {id: "52f1020f2fcd935c00587fdb"}) return p

match (start:Story {id: "52f101192fcd935400587fd5"})-[:Slot*1..10]-(middle:Story)-[:Slot*1..10]-(ziel:Story {id: "52f101442fcd935600587fd7"})
match (middle)-[:is]->(t:Tag {name: "offen"})
return middle

---

update for modified, etc.


match (s:Story) 
match (u:User {name: s.createdBy})
set s.modified = s.created, s.modifiedBy=u.login, s.createdBy=u.login

---

update for icon

match (s:Story) set s.icon = "fa-bookmark"