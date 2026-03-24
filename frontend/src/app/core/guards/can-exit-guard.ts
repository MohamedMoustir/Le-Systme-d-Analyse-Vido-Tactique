import { inject } from '@angular/core';
import { CanDeactivateFn } from '@angular/router';
import { TeamComponent } from '../../features/team/team.component';
export const canExitGuard: CanDeactivateFn<TeamComponent> = (component) => {

  if (component.chekCanExit()) {
    return confirm('Attention ! Vous avez des modifications non enregistrées. Voulez-vous vraiment quitter ?');
  }
  return false;

};
