package com.fastfacts.salesportal.repository;

import com.fastfacts.salesportal.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for Form entities.
 */
@Repository
public interface FormRepository extends JpaRepository<Form, Long> {
}
