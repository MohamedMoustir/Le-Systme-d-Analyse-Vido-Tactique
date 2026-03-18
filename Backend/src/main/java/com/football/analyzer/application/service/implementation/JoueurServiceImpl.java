package com.football.analyzer.application.service.implementation;

import com.football.analyzer.application.service.JoueurService;
import com.football.analyzer.domain.entity.Joueur;
import com.football.analyzer.domain.exception.ResourceNotFoundException;
import com.football.analyzer.domain.repository.JoueurRepository;
import com.football.analyzer.presentation.dto.Response.JoueurResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@AllArgsConstructor
public class JoueurServiceImpl implements JoueurService {

    private final JoueurRepository repo;
    private final String UPLOAD_DIR = "uploads/";

    @Override
    @Transactional
    public JoueurResponseDTO updateJoueur(String id, String nomComplet, Integer numeroMaillot, String poste, MultipartFile photo) throws IOException {
        Joueur existingJoueur = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Joueur non trouvé avec l'id: " + id));

        existingJoueur.setNomComplet(nomComplet);
        existingJoueur.setNumeroMaillot(numeroMaillot);
        existingJoueur.setPoste(poste);

        if (photo != null && !photo.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename().replaceAll(" ", "_");
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, photo.getBytes());

            String newPhotoUrl = "/uploads/" + fileName;
            existingJoueur.setPhotoUrl(newPhotoUrl);
        }

        Joueur updatedJoueur = repo.save(existingJoueur);

        return mapToResponseDTO(updatedJoueur);
    }
    @Override
    public JoueurResponseDTO getJoueurById(String id) {
        Joueur joueur = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Joueur introuvable avec l'id: " + id));
        return mapToResponseDTO(joueur);
    }

    @Override
    @Transactional
    public void deleteJoueur(String id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Impossible de supprimer: Joueur non trouvé");
        }
        repo.deleteById(id);
    }

    private JoueurResponseDTO mapToResponseDTO(Joueur joueur) {
        return JoueurResponseDTO.builder()
                .nomComplet(joueur.getNomComplet())
                .numeroMaillot(joueur.getNumeroMaillot())
                .poste(joueur.getPoste())
                .photoUrl(joueur.getPhotoUrl())
                .build();
    }
}