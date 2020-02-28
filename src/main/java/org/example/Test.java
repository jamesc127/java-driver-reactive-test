package org.example;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import reactor.core.publisher.Flux;
import sun.lwawt.macosx.CSystemTray;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void func(String a) {
        String s = a.concat("flux");
        System.out.println(s);
    }

    public static void main(String[] args) {

        try (CqlSession session = CqlSession.builder().build()) {                                              // (1)

            // Method 1 - basic execution pattern
            ResultSet rs = session.execute("select itemname from profile_test.itemTable");              // (2)
            Row row = rs.one();
            System.out.println("Method 1 - ItemName is :" + row.getString("itemname"));                 // (3)


            // Method 2 - prepared statement execution
            PreparedStatement pStatement = session.prepare("select itemname from profile_test.itemTable WHERE itemid = ?");
            BoundStatement bstatemnt = pStatement.bind(101);
            ResultSet rs1 = session.execute(bstatemnt);
            Row row1 = rs1.one();
            System.out.println("Method 2 - ItemName is :" + row1.getString("itemname"));

            // Method 3 - run one query in flux
            Flux.from(session.executeReactive("select itemname from profile_test.itemTable where itemid=103"))
                    .subscribe(row4 -> {
                        System.out.println("Method 3 - ItemName is : " + row4.getString("itemname"));
                    });

            // Method 4 - multiple query execution in flux
            Flux.just("select itemname from profile_test.itemTable where itemid=101",
                    "select itemname from profile_test.itemTable where itemid=104")
                    .flatMap(session::executeReactive)
                    .subscribe(row3 -> {
                        System.out.println("Method 4 - ItemName is : " + row3.getString("itemname"));
                    });

            // Method 5 - Manipulate values in the flux stream
            /* List<String> itemn = new ArrayList<>();
            itemn.add("text");
            itemn.forEach(System.out::println);
            System.out.println(itemn.size()); */

            Flux.just("select itemname from profile_test.itemTable where itemid=102",
                      "select itemname from profile_test.itemTable where itemid=103")
                    .flatMap(session::executeReactive)
                    .map(s -> s.getString("itemname").concat(" flux"))
                    .subscribe(System.out::println);

            // Method 6 - More advanced - probably doesn't work because of string format issue.
            // sub code check !!
            /*System.out.println(session.execute("select locationcode from profile_test.itemquantity where itemname="+"'pringles'")
                    .one()
                    .getString("locationcode")); */

            Flux.just("select itemname from profile_test.itemTable where itemid=102",
                    "select itemname from profile_test.itemTable where itemid=103")
                    .flatMap(session::executeReactive)
                    .map(s -> session.execute("select locationcode from profile_test.itemquantity where itemname="+s.getString("itemname")).one().getString("locationcode"))
                    .subscribe(System.out::println);

            // Method 7 - more advanced flux in a flux solution.

            // sub code check !!
            /* System.out.println(session.execute("select locationcode from profile_test.itemquantity where itemname="+"'pringles'")
                    .one()
                    .getString("locationcode")); */

            Flux.just("select itemname from profile_test.itemTable where itemid=102",
                    "select itemname from profile_test.itemTable where itemid=106")
                    .flatMap(session::executeReactive)
                    .flatMap(row6 -> { return Flux.just("select locationcode from profile_test.itemquantity where itemname='"+row6.getString("itemname")+"'");
                    })
                    .flatMap(session::executeReactive)
                    .subscribe(r -> {
                        System.out.println("Method 7 - Location Code  is : " + r.getString("locationcode"));
                    });

            /* explainer code

            Flux.just("select locationcode from profile_test.itemquantity where itemname='salsa'",
                    "select locationcode from profile_test.itemquantity where itemname='Pringles'")
                    .flatMap(session::executeReactive)
                    .subscribe(System.out::println);

            */

            // Method 8 - More Advanced code with Bound Statements and flux in a flux.

            int[] pkeys = { 101,102,103,104,105,106,107 };
            System.out.println(pkeys.length);

            List<Row> rowList = new ArrayList<>();
            List<BoundStatement> bStmnts = new ArrayList<>();
            PreparedStatement pStmt = session.prepare("select itemname from profile_test.itemTable where itemid=?");
            for (Integer key : pkeys){
                BoundStatement query = pStmt.bind(key);
                bStmnts.add(query);
            }

            Flux.just(bStmnts.toArray(new BoundStatement[bStmnts.size()]))
                    .flatMap(session::executeReactive)
                    .flatMap(row6 -> { return Flux.just("select locationcode from profile_test.itemquantity where itemname='"+row6.getString("itemname")+"'");
                    })
                    .flatMap(session::executeReactive)
                    .subscribe(r -> {
                        System.out.println("Method 8 - Location Code  is : " + r.getString("locationcode"));
                    });
        }
    }
}
