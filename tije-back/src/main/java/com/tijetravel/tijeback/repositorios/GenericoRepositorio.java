package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface GenericoRepositorio<T, ID> extends JpaRepository<T, ID> {
}
