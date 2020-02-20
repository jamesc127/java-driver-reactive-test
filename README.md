# DataStax Unified Java Driver
## Execution Profile Test
This is a quick example of using the [DataStax Unified Java Driver](https://docs.datastax.com/en/developer/java-driver/4.4/) with several differing statement types utilizing [Execution Profiles](https://docs.datastax.com/en/developer/java-driver/4.4/manual/core/configuration/#execution-profiles)
### Purpose
To be able to programmatically choose the `Local` data center without creating multiple `session` objects. We need to be able to execute regular transactional statements against the `core` data center, and search queries against the `search` data center.
### Setup
I did this test quickly and ran everything from within IntelliJ. Run the `Insert` program first to create the keyspace, table, and load the test data. The main `App` program has three examples of query execution.
### Configuration File Setup
a `HOCON` formatted configuration file named `application.conf` needs to be in your `Resources` dir or in the program's classpath. Define the parameters for the `Default` execution profile at the top level, just under `datastax-java-driver`. Under those settings, define the `profiles`:
```hocon
datastax-java-driver {
  basic.contact-points = ["127.0.0.1:9042"]
  basic.load-balancing-policy.local-datacenter = core_dc
  profiles {
    core {
      basic.request.timeout = 100 milliseconds
      basic.request.consistency = LOCAL_QUORUM
      basic.load-balancing-policy.local-datacenter = core_dc
    }
    search {
      basic.request.timeout = 5 seconds
      basic.request.consistency = LOCAL_ONE
      basic.load-balancing-policy.local-datacenter = search_dc
    }
  }
}
```
#### NOTE!
I only have one datacenter in my testing env. You'll see that in my `application.conf` file all dc's are set to `dc1` 
### Prepared Statement with Execution Profile
Set the execution profile when you bind your variables to the `PreparedStatement`
```
BoundStatement bound = preparedStatement.bind("pkey1",1,5).setExecutionProfileName("core");
```
### Simple Statement with the Default Execution Profile
Just like you would expect.
### Prepared Search Statement with a different Execution Profile
```
PreparedStatement prepSearch =
    session.prepare("SELECT * FROM profile_test.test WHERE clustering2 > ? AND clustering2 <= ?");
BoundStatement boundSearch =
    prepSearch.bind(10,12).setExecutionProfileName("search");
```