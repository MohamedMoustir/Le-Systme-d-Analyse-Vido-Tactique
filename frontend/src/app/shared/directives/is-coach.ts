import { Directive, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';

@Directive({
  selector: '[appIsCoach]',
  standalone: true
})
export class IsCoach implements OnInit {

  constructor(private template: TemplateRef<any>,
    private vcr: ViewContainerRef) {
  }

  ngOnInit(){
    const isCotch = true;
    if(isCotch){
    this.vcr.createEmbeddedView(this.template)
    }else{
      this.vcr.clear();
    }
  }

}
