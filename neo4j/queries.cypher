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