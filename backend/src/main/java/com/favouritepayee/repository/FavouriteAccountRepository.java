package com.favouritepayee.repository;

import com.favouritepayee.entity.FavouriteAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavouriteAccountRepository extends JpaRepository<FavouriteAccount, Long> {

    @Query("""
            select fa from FavouriteAccount fa
            where fa.customer.id = :customerId
              and (
                  :search is null
                  or :search = ''
                  or lower(fa.name) like lower(concat('%', :search, '%'))
                  or lower(fa.iban) like lower(concat('%', :search, '%'))
                  or lower(fa.bank) like lower(concat('%', :search, '%'))
              )
            order by lower(fa.name) asc
            """)
    List<FavouriteAccount> searchByCustomer(@Param("customerId") Long customerId, @Param("search") String search);

    boolean existsByCustomerIdAndIban(Long customerId, String iban);

    boolean existsByCustomerIdAndIbanAndIdNot(Long customerId, String iban, Long id);

    long countByCustomerId(Long customerId);

    Optional<FavouriteAccount> findByIdAndCustomerId(Long id, Long customerId);
}
