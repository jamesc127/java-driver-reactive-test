package org.example;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

public class Insert {
    public static void main(String[] args){
        try (CqlSession session = CqlSession.builder().build()) {
            session.execute("CREATE KEYSPACE IF NOT EXISTS profile_test WITH REPLICATION = {'class' : 'NetworkTopologyStrategy', 'dc1' : 1};");
            session.execute("CREATE TABLE IF NOT EXISTS profile_test.test (pkey text,clustering1 int,clustering2 int,PRIMARY KEY((pkey),clustering1,clustering2))");
            session.execute("CREATE SEARCH INDEX IF NOT EXISTS ON profile_test.test WITH COLUMNS *;");
            PreparedStatement insert = session.prepare("INSERT INTO profile_test.test (pkey, clustering1, clustering2) VALUES (?,?,?)");
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 5000; j++) {
                    String pkey = "pkey"+i;
                    BoundStatement bInsert = insert.bind(pkey,i,j);
                    session.execute(bInsert);
                }
            }
            session.close();
        }
    }
}
