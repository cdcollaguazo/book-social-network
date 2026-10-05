import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class Environment {

  private config: any;

  async loadConfig() {
    return fetch('assets/environment.json')
      .then(res => res.json())
      .then(json => {
        this.config = json;
      });
  }

  get(key: string) {
    const keys = key.split(".");
    let value = this.config;

    for (const k of keys) {
      value = value?.[k];
      if (value === undefined) break;
    }

    return value;
  }

}
