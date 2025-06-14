package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;
import ru.marinalyamina.vetclinic.models.entities.ProcedurePrice;
import ru.marinalyamina.vetclinic.repositories.ProcedurePriceRepository;

import java.util.Optional;

@Service
public class ProcedurePriceService {
    private final ProcedurePriceRepository procedurePriceRepository;

    public  ProcedurePriceService(ProcedurePriceRepository procedurePriceRepository){
        this.procedurePriceRepository = procedurePriceRepository;
    }

    public Optional<Integer> getCurrentPriceForProcedure(Long procedureId) {
        return procedurePriceRepository.findTopByProcedureIdOrderByStartDateDesc(procedureId)
                .map(ProcedurePrice::getPrice);
    }

}
