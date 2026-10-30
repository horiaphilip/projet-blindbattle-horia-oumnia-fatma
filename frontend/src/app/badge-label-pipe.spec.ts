import { BadgeLabelPipe } from './badge-label-pipe';

describe('BadgeLabelPipe', () => {
  const pipe = new BadgeLabelPipe();

  it('create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should translate a known badge', () => {
    expect(pipe.transform('WELCOME')).toBe('Bienvenue dans BlindBattle');
  });

  it('should keep an unknown badge code', () => {
    expect(pipe.transform('SPEEDY')).toBe('SPEEDY');
  });
});
