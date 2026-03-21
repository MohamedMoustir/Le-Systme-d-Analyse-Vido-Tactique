package com.football.analyzer.application.service.implementation;

import com.football.analyzer.application.mapper.EquipeMapper;
import com.football.analyzer.domain.entity.Equipe;
import com.football.analyzer.domain.entity.Joueur;
import com.football.analyzer.domain.exception.BusinessLogicException;
import com.football.analyzer.domain.repository.EquipeRepository;
import com.football.analyzer.domain.repository.JoueurRepository;
import com.football.analyzer.presentation.dto.Response.EquipeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipeServiceImplTest {

  @Mock
  private EquipeRepository equipeRepository;

  @Mock
  private JoueurRepository joueurRepository;

  @Mock
  private EquipeMapper equipeMapper;

  @InjectMocks
  private EquipeServiceImpl equipeService;

  @Test
  void createEquipe_shouldCreateAndReturnEquipeResponse_whenUserHasNoExistingTeam() {
    String userId = "user-1";
    String nomEquipe = "Raja";
    String couleurHex = "#00FF00";

    when(equipeRepository.findByUserId(userId)).thenReturn(Optional.empty());
    when(equipeRepository.save(any(Equipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

    EquipeResponse mappedResponse = EquipeResponse.builder()
      .nom(nomEquipe)
      .couleurHex(couleurHex)
      .joueurs(new ArrayList<>())
      .build();
    when(equipeMapper.toDto(any(Equipe.class))).thenReturn(mappedResponse);

    EquipeResponse result = equipeService.createEquipe(userId, nomEquipe, couleurHex);

    assertNotNull(result);
    assertEquals(nomEquipe, result.getNom());
    assertEquals(couleurHex, result.getCouleurHex());

    ArgumentCaptor<Equipe> captor = ArgumentCaptor.forClass(Equipe.class);
    verify(equipeRepository).save(captor.capture());
    Equipe savedEquipe = captor.getValue();
    assertEquals(userId, savedEquipe.getUserId());
    assertEquals(nomEquipe, savedEquipe.getNom());
    assertEquals(couleurHex, savedEquipe.getCouleurHex());
    assertNotNull(savedEquipe.getJoueurs());
  }

  @Test
  void createEquipe_shouldUseDefaultColor_whenColorIsNull() {
    String userId = "user-2";

    when(equipeRepository.findByUserId(userId)).thenReturn(Optional.empty());
    when(equipeRepository.save(any(Equipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(equipeMapper.toDto(any(Equipe.class))).thenReturn(EquipeResponse.builder().build());

    equipeService.createEquipe(userId, "Wydad", null);

    ArgumentCaptor<Equipe> captor = ArgumentCaptor.forClass(Equipe.class);
    verify(equipeRepository).save(captor.capture());
    assertEquals("#3B82F6", captor.getValue().getCouleurHex());
  }

  @Test
  void createEquipe_shouldThrowBusinessLogicException_whenTeamAlreadyExists() {
    String userId = "user-3";
    when(equipeRepository.findByUserId(userId)).thenReturn(Optional.of(Equipe.builder().id("eq-1").build()));

    assertThrows(BusinessLogicException.class, () -> equipeService.createEquipe(userId, "FUS", "#123456"));

    verify(equipeRepository, never()).save(any(Equipe.class));
    verify(equipeMapper, never()).toDto(any(Equipe.class));
  }

  @Test
  void getMyTeam_shouldReturnEquipeResponse_whenTeamExists() {
    String userId = "user-4";
    Equipe equipe = Equipe.builder().id("eq-2").userId(userId).nom("Team A").build();
    EquipeResponse response = EquipeResponse.builder().id("eq-2").nom("Team A").build();

    when(equipeRepository.findByUserId(userId)).thenReturn(Optional.of(equipe));
    when(equipeMapper.toDto(equipe)).thenReturn(response);

    EquipeResponse result = equipeService.getMyTeam(userId);

    assertEquals("eq-2", result.getId());
    assertEquals("Team A", result.getNom());

    verify(equipeRepository).findByUserId(userId);
    verify(equipeMapper).toDto(equipe);
  }

  @Test
  void getMyTeam_shouldThrowBusinessLogicException_whenTeamNotFound() {
    String userId = "missing-user";
    when(equipeRepository.findByUserId(userId)).thenReturn(Optional.empty());

    assertThrows(BusinessLogicException.class, () -> equipeService.getMyTeam(userId));

    verify(equipeMapper, never()).toDto(any(Equipe.class));
  }
}
