package btcviz;

import org.springframework.data.neo4j.repository.GraphRepository;

public interface AccountRepository extends GraphRepository<Account> {

    Account findByName(String name);

    Iterable<Account> findByTransactorsName(String name);

}
