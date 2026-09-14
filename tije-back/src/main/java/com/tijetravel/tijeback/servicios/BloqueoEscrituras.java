package com.tijetravel.tijeback.servicios;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Coordina escrituras del TP entre transacciones, incluso desde procesos distintos. */
@Service
public class BloqueoEscrituras {
    private final EntityManager entityManager;

    public BloqueoEscrituras(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void adquirir() {
        entityManager.createNativeQuery(
                "SELECT codigo FROM control_escrituras WHERE codigo = 1 FOR UPDATE")
                .getSingleResult();
    }
}
