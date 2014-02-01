
// create indices und constraints

create constraint on (s:Story) assert s.id is unique

create constraint on (u:User) assert u.login is unique

create constraint on (t:Tag) assert t.name is unique

create index on :Slot(name)


// create a User

create (u:User {login:"hallo", pwd:"245f1022bf83e29395b1415c3b45b41c6b99a1f6aa4bacff7ec5b989b4eb55", name:"Hallo Welt", permissions:[]})


// Das Gegenteil:

DROP INDEX ON :Story(id)
DROP INDEX ON :User(login
DROP INDEX ON :Slot(name))

