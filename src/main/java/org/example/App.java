package org.example;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import java.util.Iterator;

public class App 
{
    public static void main( String[] args )
    {
        try (CqlSession session = CqlSession.builder().build()) {
            System.out.println("Example of a prepared statement with an execution profile");
            PreparedStatement prep =
                    session.prepare("SELECT * FROM profile_test.test WHERE pkey = ? AND clustering1 = ? AND clustering2 < ?");
            BoundStatement bound =
                    prep.bind("pkey1",1,5).setExecutionProfileName("core");

            ResultSet rs = session.execute(bound);
            for (Row r : rs) {
                System.out.println(r.getFormattedContents());
            }

            System.out.println("Example of a simple statement with the default execution profile");
            SimpleStatement state =
                    SimpleStatement.builder("SELECT * FROM profile_test.test WHERE pkey = 'pkey2' LIMIT 20").build();

            ResultSet simpRs = session.execute(state);
            for (Row simpR : simpRs) {
                System.out.println(simpR.getFormattedContents());
            }

            System.out.println("Example of a search statement with a different execution profile");
            PreparedStatement prepSearch =
                    session.prepare("SELECT * FROM profile_test.test WHERE clustering2 > ? AND clustering2 <= ?");
            BoundStatement boundSearch =
                    prepSearch.bind(10,12).setExecutionProfileName("search");

            ResultSet searchRs = session.execute(boundSearch);
            for (Row searchR : searchRs) {
                System.out.println(searchR.getFormattedContents());
            }
        }
    }
}
