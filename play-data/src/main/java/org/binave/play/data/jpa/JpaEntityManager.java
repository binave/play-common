package org.binave.play.data.jpa;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * @author by bin jin on 2019/08/28 16:09.
 */
@Getter
@AllArgsConstructor
@ToString
public class JpaEntityManager<T extends JpaRepository> {

    private String name;
    private double version;
    private String url;
    private PlatformTransactionManager transactionManager;
    private T jpaRepository;

    public TransactionStatus getTransactionStatus(TransactionDefinition definition) {
        return this.transactionManager.getTransaction(definition);
    }

}
