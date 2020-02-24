package org.example;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import java.util.ArrayList;
import java.util.List;
import reactor.core.publisher.Flux;

public class App 
{
    public static Row getFromOtherTable(Integer key, PreparedStatement pState, CqlSession session){
        BoundStatement bound = pState.bind(key);
        Row row = session.execute(bound).one();
        System.out.println(row.getFormattedContents());
        return row;
    }
    public static void main( String[] args )
    {
        try (CqlSession session = CqlSession.builder().build()) {
            System.out.println("Reactive Flux that doesn't blockLast()");
            List<Integer> keys = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                keys.add(i);
            }
            List<Row> rowList = new ArrayList<>();
            List<BoundStatement> bStmnts = new ArrayList<>();
            PreparedStatement pStatement = session.prepare("SELECT clustering1 FROM profile_test.test WHERE pkey = ?");
            for (Integer key : keys){
                BoundStatement query = pStatement.bind(key);
                bStmnts.add(query);
            }
            PreparedStatement otherStatement = session.prepare("SELECT clustering1 FROM profile_test.other_test WHERE pkey = ?");
            Flux.just(bStmnts.toArray(new BoundStatement[bStmnts.size()]))
                    .flatMap(session::executeReactive)
                    .subscribe(row -> {
                        System.out.println("entered the subscribe " + row.getInt("clustering1"));
                        //execute query here
                        //add result to some array
                        //return the array from the method
                    });
//            for (Row row : rowList){
//                System.out.println(row.getFormattedContents());
//            }
        }
    }
}
