package btcviz;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class Account {

    @GraphId Long id;
    public String name;

    public Account() {}
    public Account(String name) { this.name = name; }

    @RelatedTo(type="TRANSACTS_WITH", direction=Direction.BOTH)
    public @Fetch Set<Account> transactors;

    public void transactsWith(Account account) {
        if (transactors == null) {
            transactors = new HashSet<Account>();
        }
        transactors.add(account);
    }

    public String toString() {
        String results = name + " transacts with\n";
        if (transactors != null) {
            for (Account account : transactors) {
                results += "\t- " + account.name + "\n";
            }
        }
        return results;
    }

}
