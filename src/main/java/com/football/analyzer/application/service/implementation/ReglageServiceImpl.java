package com.football.analyzer.application.service.implementation;

import com.football.analyzer.application.service.ReglageService;
import com.football.analyzer.domain.entity.Equipe;
import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.repository.EquipeRepository;
import com.football.analyzer.domain.repository.UserRepository;
import com.football.analyzer.presentation.dto.Response.ReglageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReglageServiceImpl implements ReglageService {

    private final EquipeRepository equipeRepository;
    private final UserRepository userRepository;

    @Override
    public ReglageDTO getReglages(String userId) {
        Equipe equipe = equipeRepository.findByUserId(userId).orElse(new Equipe());

        User user = userRepository.findByEmail(userId).orElse(new User());

        return ReglageDTO.builder()
                .nomComplet(user.getNom() != null ? user.getNom() : "Coach")
                .email(user.getEmail() != null ? user.getEmail() : "")
                .nomClub(equipe.getNom() != null ? equipe.getNom() : "Mon Équipe")
                .couleurClub(equipe.getCouleurHex() != null ? equipe.getCouleurHex() : "#8b5cf6")
                .notificationsActives(true)
                .langue("fr")
                .build();
    }

    @Override
    public ReglageDTO updateReglages(String userId, ReglageDTO dto) {
        Equipe equipe = equipeRepository.findByUserId(userId).orElseGet(() -> {
            Equipe newEquipe = new Equipe();
            newEquipe.setUserId(userId);
            return newEquipe;
        });

        equipe.setNom(dto.getNomClub());
        equipe.setCouleurHex(dto.getCouleurClub());
        equipeRepository.save(equipe);

        userRepository.findByEmail(userId).ifPresent(user -> {
            if (dto.getNomComplet() != null && !dto.getNomComplet().isEmpty()) {
                user.setNom(dto.getNomComplet());
            }
            if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
                user.setEmail(dto.getEmail());
            }
            userRepository.save(user);
        });

        return dto;
    }
}