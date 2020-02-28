package org.example;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

            Flux.just(bStmnts.toArray(new BoundStatement[bStmnts.size()]))
                .flatMap(session::executeReactive)
                .flatMap(row -> {
                    System.out.println("This is the after the first executeReactive "+row.getFormattedContents());
                    return Flux.just("SELECT * FROM profile_test.other_test WHERE pkey = "+row.getInt("clustering1"));
                })
                .flatMap(session::executeReactive)
                .subscribe(row -> {
                    System.out.println("This is the after the second executeReactive "+row.getFormattedContents());
                });

            Flux<Integer> rs =
                Flux.just(bStmnts.toArray(new BoundStatement[bStmnts.size()]))
                .flatMap(session::executeReactive)
                .map(row -> {
                    return row.getInt("clustering1");
                })
                .publish();

            ReactiveResultSet something = session.executeReactive("SELECT * FROM profile_test.other_test WHERE pkey = "+rs.map(Objects::toString));
        }
    }
}