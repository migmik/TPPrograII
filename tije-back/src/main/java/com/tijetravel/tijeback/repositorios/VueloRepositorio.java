package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tijetravel.tijeback.modelos.Vuelo;

public interface VueloRepositorio extends JpaRepository<Vuelo, Integer> {
}
