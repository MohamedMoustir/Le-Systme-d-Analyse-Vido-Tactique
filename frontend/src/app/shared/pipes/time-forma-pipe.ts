import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'timeFormat', 
  standalone: true 
})
export class TimeFormatPipe implements PipeTransform {

  transform(value: string | number): string {
    if (!value) return '00:00';

    const totalSeconds = Number(value);
    
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    const formattedMinutes = String(minutes).padStart(2, '0');
    const formattedSeconds = String(seconds).padStart(2, '0');

    return `${formattedMinutes}:${formattedSeconds}`;
  }
}
