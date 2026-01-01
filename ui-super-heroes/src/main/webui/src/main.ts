// Polyfill for process object - must be first!
(window as any).process = (window as any).process || {
  env: {},
  argv: [],
  version: '',
  versions: {},
  platform: 'browser',
  browser: true
};
(window as any).global = (window as any).global || window;

import {enableProdMode} from '@angular/core';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {AppModule} from './app/app.module';
import {environment} from './environments/environment';

if (environment.production) {
  enableProdMode();
}

console.log('Initialising with NG_APP_ENV: ', process.env.NG_APP_ENV)

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));
