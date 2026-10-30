import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'badgeLabel',
})
export class BadgeLabelPipe implements PipeTransform {
  private labels: Record<string, string> = {
    WELCOME: 'Bienvenue dans BlindBattle',
  };

  transform(code: string): string {
    return this.labels[code] ?? code;
  }
}
