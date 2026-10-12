package org.example.eventclaimsservice.repository;

import org.example.eventclaimsservice.entity.IrrigationReading;

import org.springframework.data.repository.reactive
        .ReactiveCrudRepository;

public interface IrrigationReadingRepository
        extends ReactiveCrudRepository<
        IrrigationReading,
        Long> {
}