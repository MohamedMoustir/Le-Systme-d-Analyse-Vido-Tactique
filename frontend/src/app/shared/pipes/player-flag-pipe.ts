import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'playerFlag',
})
export class PlayerFlagPipe implements PipeTransform {

  transform(value: string): string {
    const flag: { [key: string]: string } = {
      'MA': '🇲🇦',
      'FR': '🇫🇷',
      'ES': '🇪🇸',
      'BR': '🇧🇷',
      'AR': '🇦🇷'
    }
    return flag[value.toUpperCase()] || '🏳️'
  }

}
