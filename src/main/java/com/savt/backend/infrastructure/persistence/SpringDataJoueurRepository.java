package com.savt.backend.infrastructure.persistence;

import com.savt.backend.domain.entity.Joueur;
import com.savt.backend.domain.entity.VideoMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringDataJoueurRepository  extends MongoRepository<Joueur,String> {
    List<Joueur> findByEquipeId(String equipeId);

}
