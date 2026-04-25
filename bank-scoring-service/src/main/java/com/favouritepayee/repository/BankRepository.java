package com.favouritepayee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.favouritepayee.entity.Bank;

public interface BankRepository extends JpaRepository<Bank, String> {
}
