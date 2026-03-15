package com.football.analyzer.application.service.implementation;

import com.football.analyzer.application.mapper.EquipeMapper;
import com.football.analyzer.application.service.EquipeService;
import com.football.analyzer.domain.entity.Equipe;
import com.football.analyzer.domain.entity.Joueur;
import com.football.analyzer.domain.exception.BusinessLogicException;
import com.football.analyzer.domain.repository.EquipeRepository;
import com.football.analyzer.domain.repository.JoueurRepository;
import com.football.analyzer.presentation.dto.response.EquipeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipeServiceImpl implements EquipeService {

    private final EquipeRepository equipeRepository;
    private final JoueurRepository joueurRepository;
    private final EquipeMapper equipeMapper;
    private final String UPLOAD_DIR = "uploads/";

    private Equipe getEquipeOrThrow(String userId) {
        return equipeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessLogicException("Team not found. Please create your team first."));
    }
      @Override
    public EquipeResponse createEquipe(String userId, String nomEquipe, String couleurHex) {
        Optional<Equipe> existingEquipe = equipeRepository.findByUserId(userId);
        if (existingEquipe.isPresent()) {
            throw new BusinessLogicException("You already have a team created.");
        }

        Equipe newEquipe = Equipe.builder()
                .userId(userId)
                .nom(nomEquipe)
                .couleurHex(couleurHex != null ? couleurHex : "#3B82F6")
                .joueurs(new ArrayList<>())
                .build();

        return equipeMapper.toDto(equipeRepository.save(newEquipe));
    }

    @Override
    public EquipeResponse getMyTeam(String userId) {
        Equipe equipe = getEquipeOrThrow(userId);
        return equipeMapper.toDto(equipe);
    }

    @Override
    public EquipeResponse addJoueur(String userId, String nom, Integer numero, String poste, MultipartFile photo) throws Exception {
        Equipe equipe = getEquipeOrThrow(userId);

        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename().replaceAll(" ", "_");
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, photo.getBytes());
            photoUrl = "http://localhost:8080/uploads/" + fileName;
        }

        Joueur joueur = Joueur.builder()
                .nomComplet(nom)
                .numeroMaillot(numero)
                .poste(poste)
                .photoUrl(photoUrl)
                .build();

        joueurRepository.save(joueur);

        if (equipe.getJoueurs() == null) equipe.setJoueurs(new ArrayList<>());
        equipe.getJoueurs().add(joueur);
        equipeRepository.save(equipe);

        return equipeMapper.toDto(equipe);
    }

    @Override
    public EquipeResponse importCsv(String userId, MultipartFile file) throws Exception {
        Equipe equipe = getEquipeOrThrow(userId);
        List<Joueur> nouveauxJoueurs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    nouveauxJoueurs.add(Joueur.builder()
                            .nomComplet(data[0].trim())
                            .numeroMaillot(Integer.parseInt(data[1].trim()))
                            .poste(data[2].trim())
                            .photoUrl(data.length >= 4 ? data[3].trim() : null)
                            .build());
                }
            }
        }
        joueurRepository.saveAll(nouveauxJoueurs);

        if (equipe.getJoueurs() == null) equipe.setJoueurs(new ArrayList<>());
        equipe.getJoueurs().addAll(nouveauxJoueurs);
        equipeRepository.save(equipe);

        return equipeMapper.toDto(equipe);
    }

    @Override
    public EquipeResponse deleteJoueur(String userId, String joueurId) throws Exception {
        Equipe equipe = getEquipeOrThrow(userId);

        if (equipe.getJoueurs() == null || equipe.getJoueurs().stream().noneMatch(j -> j.getId().equals(joueurId))) {
            throw new IllegalArgumentException("Player not found in the user's team");
        }

        equipe.getJoueurs().removeIf(j -> j.getId().equals(joueurId));
        joueurRepository.deleteById(joueurId);
        equipeRepository.save(equipe);

        return equipeMapper.toDto(equipe);
    }
}