import { Component, effect, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { JoueurCardComponent } from '../../shared/components/joueur-card/joueur-card.component';
import { JoueurFormComponent } from '../../shared/components/joueur-form/joueur-form.component';
import { EquipeService } from '../../core/services/equipe.service';
import { JoueurService } from '../../core/services/joueur.service';
import { EquipeStore } from '../../core/store/equipe.store';
import { SidebarComponent } from "../../core/layout/sidebar/app-sidebar";
import { BehaviorSubject } from 'rxjs';

@Component({
  standalone: true,
  imports: [CommonModule, JoueurCardComponent, JoueurFormComponent, ReactiveFormsModule, SidebarComponent],
  templateUrl: './team.component.html'
})
export class TeamComponent implements OnInit {
  private fb = inject(FormBuilder);
  private joueurService = inject(JoueurService);
  private equipeService = inject(EquipeService);
  public store = inject(EquipeStore);

  isModalOpen = signal(false);
  editingJoueurId = signal<number | null>(null);
  photoPreview = signal<string | null>(null);
  showDeleteCard: boolean = false;
  joueurToDeleteId: number | null = null;

  selectedFile: File | null = null;

  joueurForm = this.fb.group({
    nomComplet: ['', [Validators.required]],
    numeroMaillot: [null as number | null, [Validators.required, Validators.min(1)]],
    poste: ['', [Validators.required]],
    photoUrl: ['']
  });

  chekCanExit() {
    return this.joueurForm.dirty
  }

  constructor() {
    effect(() => {
      console.log('Current players in store:', this.store.joueurs());
    });
  }

  ngOnInit() {
    this.store.loadMyTeam();
  }

  openModal() {
    this.editingJoueurId.set(null);
    this.joueurForm.reset();
    this.photoPreview.set(null);
    this.selectedFile = null;
    this.isModalOpen.set(true);
  }

  onEditJoueur(joueur: any) {
    this.editingJoueurId.set(joueur.id);
    this.joueurForm.patchValue({
      nomComplet: joueur.nomComplet,
      numeroMaillot: joueur.numeroMaillot,
      poste: joueur.poste,
      photoUrl: joueur.photoUrl
    });
    this.photoPreview.set(joueur.photoUrl);
    this.selectedFile = null;
    this.isModalOpen.set(true);
  }

  closeModal() {
    this.isModalOpen.set(false);
  }


  onDeleteJoueur(id: number) {
    this.joueurToDeleteId = id;
    this.showDeleteCard = true;
  }

  confirmerSuppression() {
    if (this.joueurToDeleteId !== null) {
      this.store.deleteJoueur(this.joueurToDeleteId.toString());
      this.fermerCard();
    }
  }

  fermerCard() {
    this.showDeleteCard = false;
    this.joueurToDeleteId = null;
  }

  onSubmitJoueur() {
    if (this.joueurForm.invalid) return;

    const formData = new FormData();

    const nomComplet = this.joueurForm.get('nomComplet')?.value || '';
    const poste = this.joueurForm.get('poste')?.value || '';
    const numeroMaillot = this.joueurForm.get('numeroMaillot')?.value;

    formData.append('nomComplet', nomComplet);
    formData.append('poste', poste);

    if (numeroMaillot !== null && numeroMaillot !== undefined) {
      formData.append('numeroMaillot', numeroMaillot.toString());
    }

    if (this.selectedFile) {
      formData.append('photo', this.selectedFile);
    }

    const id = this.editingJoueurId()?.toString();

    if (id) {
      this.store.updateJoueur({ id, dto: formData as any });
    } else {
      this.store.addSingleJoueur(formData as any);
    }

    this.closeModal();
  }

  onPhotoSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;

    if (fileList && fileList.length > 0) {
      const file = fileList[0];

      this.selectedFile = file;

      const reader = new FileReader();
      reader.onload = () => {
        this.photoPreview.set(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  onFileSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    const file = element.files?.[0];

    if (file) {
      this.store.importCsv(file);
      element.value = '';
    }
  }

  openTeamCreationModal() {
    console.log('Open Team Creation Form');
  }
}