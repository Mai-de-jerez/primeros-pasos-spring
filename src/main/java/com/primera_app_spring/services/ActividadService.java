package com.primera_app_spring.services;

import com.primera_app_spring.model.Actividad;
import com.primera_app_spring.repository.ActividadRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;

    ActividadService(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    public List<Actividad> obtenerTodas() {
        return actividadRepository.findAll();
    }

    public Optional<Actividad> buscarPorId(Long id) {
        return actividadRepository.findById(id);
    }

    public void guardar(Actividad actividad) {
        actividadRepository.save(actividad);
    }
}
