package org.example;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

public class Insert {
    public static void main(String[] args){
        try (CqlSession session = CqlSession.builder().build()) {
            session.execute("CREATE KEYSPACE IF NOT EXISTS profile_test WITH REPLICATION = {'class' : 'NetworkTopologyStrategy', 'dc1' : 1};");
            session.execute("DROP TABLE IF EXISTS profile_test.test");
            session.execute("DROP TABLE IF EXISTS profile_test.other_test");
            session.execute("CREATE TABLE IF NOT EXISTS profile_test.test (pkey int,clustering1 int,clustering2 int,PRIMARY KEY((pkey),clustering1,clustering2))");
            session.execute("CREATE TABLE IF NOT EXISTS profile_test.other_test (pkey int,clustering1 int,clustering2 int,PRIMARY KEY((pkey),clustering1,clustering2))");
//            session.execute("CREATE SEARCH INDEX IF NOT EXISTS ON profile_test.test WITH COLUMNS *;");
            PreparedStatement insert = session.prepare("INSERT INTO profile_test.test (pkey, clustering1, clustering2) VALUES (?,?,?)");
            PreparedStatement otherInsert = session.prepare("INSERT INTO profile_test.other_test (pkey, clustering1, clustering2) VALUES (?,?,?)");
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 10; j++) {
                    for (int k = 0; k < 100 ; k++) {
                        BoundStatement bInsert = insert.bind(i,j,k);
                        session.execute(bInsert);
                    }
                }
            }
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 10; j++) {
                    for (int k = 0; k < 100 ; k++) {
                        BoundStatement oInsert = otherInsert.bind(i,j,k);
                        session.execute(oInsert);
                    }
                }
            }
            session.close();
        }
    }
}
