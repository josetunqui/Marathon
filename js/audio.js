/**
 * GENERADOR DE EFECTOS SONOROS DE ESCÁNER (Web Audio API)
 * Simula el comportamiento sonoro de un terminal lector de código de barras
 */

class SoundEffects {
  constructor() {
    this.audioCtx = null;
    this.enabled = true;
  }

  initAudio() {
    if (!this.audioCtx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (AudioContext) {
        this.audioCtx = new AudioContext();
      }
    }
    if (this.audioCtx && this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }
  }

  toggleSound(forceState) {
    if (typeof forceState === 'boolean') {
      this.enabled = forceState;
    } else {
      this.enabled = !this.enabled;
    }
    return this.enabled;
  }

  // Beep clásico de escáner de retail (Honeywell / Zebra - 1750 Hz corto y nítido)
  playScanSuccess() {
    if (!this.enabled) return;
    try {
      this.initAudio();
      if (!this.audioCtx) return;

      const osc = this.audioCtx.createOscillator();
      const gain = this.audioCtx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(1850, this.audioCtx.currentTime);

      gain.gain.setValueAtTime(0.25, this.audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.audioCtx.currentTime + 0.08);

      osc.connect(gain);
      gain.connect(this.audioCtx.destination);

      osc.start();
      osc.stop(this.audioCtx.currentTime + 0.08);
    } catch (e) {
      console.warn('Audio no inicializado:', e);
    }
  }

  // Tono de error (Doble tono bajo para producto no encontrado o código inválido)
  playScanError() {
    if (!this.enabled) return;
    try {
      this.initAudio();
      if (!this.audioCtx) return;

      const now = this.audioCtx.currentTime;
      const osc = this.audioCtx.createOscillator();
      const gain = this.audioCtx.createGain();

      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(260, now);
      osc.frequency.setValueAtTime(200, now + 0.1);

      gain.gain.setValueAtTime(0.3, now);
      gain.gain.linearRampToValueAtTime(0.01, now + 0.22);

      osc.connect(gain);
      gain.connect(this.audioCtx.destination);

      osc.start();
      osc.stop(now + 0.22);
    } catch (e) {
      console.warn('Audio no disponible:', e);
    }
  }

  // Acorde de validación / éxito al guardar conciliación
  playSuccessChime() {
    if (!this.enabled) return;
    try {
      this.initAudio();
      if (!this.audioCtx) return;

      const notes = [523.25, 659.25, 783.99, 1046.50]; // C5, E5, G5, C6
      notes.forEach((freq, idx) => {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();

        osc.type = 'triangle';
        osc.frequency.setValueAtTime(freq, this.audioCtx.currentTime + idx * 0.06);

        gain.gain.setValueAtTime(0.18, this.audioCtx.currentTime + idx * 0.06);
        gain.gain.exponentialRampToValueAtTime(0.001, this.audioCtx.currentTime + idx * 0.06 + 0.35);

        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.start(this.audioCtx.currentTime + idx * 0.06);
        osc.stop(this.audioCtx.currentTime + idx * 0.06 + 0.35);
      });
    } catch (e) {
      console.warn('Audio chime falló:', e);
    }
  }
}

const soundEffects = new SoundEffects();
