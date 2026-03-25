package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.EquipeService;
import com.football.analyzer.domain.entity.Joueur;
import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.repository.EquipeRepository;
import com.football.analyzer.presentation.dto.Response.EquipeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService equipeService;
  private final EquipeRepository equipeRepository;

  @GetMapping("/my-team")
    public ResponseEntity<EquipeResponse> getMyTeam(Principal principal) {
        String userId = principal.getName();

        return ResponseEntity.ok(equipeService.getMyTeam(userId));
    }

    @PostMapping(value = "/my-team/joueur", consumes = "multipart/form-data")
    public ResponseEntity<?> addSingleJoueur(
            Principal principal,
            @RequestParam("nomComplet") String nomComplet,
            @RequestParam("numeroMaillot") Integer numeroMaillot,
            @RequestParam("poste") String poste,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        try {
            String userId = principal.getName();
            return ResponseEntity.ok(equipeService.addJoueur(userId, nomComplet, numeroMaillot, poste, photo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/my-team/import-csv")
    public ResponseEntity<?> importJoueursCsv(Principal principal, @RequestParam("file") MultipartFile file) {
        try {
            String userId = principal.getName();
            return ResponseEntity.ok(equipeService.importCsv(userId, file));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PostMapping("/create")
    public ResponseEntity<EquipeResponse> createTeam(
            Principal principal,
            @RequestParam String nomEquipe,
            @RequestParam(required = false) String couleurHex) {

        String userId = principal.getName();

        return ResponseEntity.ok(equipeService.createEquipe(userId, nomEquipe, couleurHex));
    }


//    public List<String> finJouer(String post){
//      return equipeRepository.findAll().stream()
//        .flatMap(p->p.getJoueurs().stream())
//        .filter(p->p.getPoste().equals(post))
//        .map(Joueur::getNomComplet).toList();
//
//    }
}
