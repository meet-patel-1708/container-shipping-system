import { Injectable } from '@angular/core';

declare global {
  interface Window { Razorpay: new (options: Record<string, unknown>) => RazorpayInstance }
}
export interface RazorpayInstance {
  open(): void;
  on(event: string, cb: (res: any) => void): void;
}

@Injectable({ providedIn: 'root' })
export class RazorpayLoader {
  private promise?: Promise<void>;

  load(): Promise<void> {
    if (window.Razorpay) return Promise.resolve();
    this.promise ??= new Promise((resolve, reject) => {
      const s = document.createElement('script');
      s.src = 'https://checkout.razorpay.com/v1/checkout.js';
      s.onload = () => resolve();
      s.onerror = () => { this.promise = undefined; reject(new Error('Could not load Razorpay')); };
      document.head.appendChild(s);
    });
    return this.promise;
  }
}