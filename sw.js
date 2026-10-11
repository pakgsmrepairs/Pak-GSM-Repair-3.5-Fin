/* Pak GSM Repair 4.3 PWA service worker.
   A new release may install in the background, but it is NOT applied until the user taps Update Now.
   The waiting worker downloads/validates the app shell only after receiving APPLY_UPDATE. */
const CACHE_NAME = 'pgr-4-3-shell-v1';
const APP_SHELL = ['./index.html', './manifest.json', './update.json', './cloud-config.js'];

self.addEventListener('install', event => {
  // Do not precache a new release before the user chooses to apply it.
  event.waitUntil(caches.open(CACHE_NAME));
});

self.addEventListener('message', event => {
  if (event.data?.type !== 'APPLY_UPDATE') return;
  const port = event.ports?.[0];
  event.waitUntil((async () => {
    try {
      const cache = await caches.open(CACHE_NAME);
      for (const path of APP_SHELL) {
        const response = await fetch(new Request(path + (path.includes('?') ? '&' : '?') + 'pgrUpdate=' + Date.now(), {cache: 'no-store'}));
        if (!response.ok) throw new Error('Could not download ' + path + ' (HTTP ' + response.status + ').');
        const cleanUrl = new URL(path, self.registration.scope).href;
        await cache.put(cleanUrl, response.clone());
      }
      port?.postMessage({ok: true});
      await self.skipWaiting();
    } catch (error) {
      port?.postMessage({ok: false, error: error?.message || 'Update download failed.'});
    }
  })());
});

self.addEventListener('activate', event => {
  event.waitUntil((async () => {
    const names = await caches.keys();
    await Promise.all(names.filter(name => name.startsWith('pgr-') && name !== CACHE_NAME).map(name => caches.delete(name)));
    await self.clients.claim();
  })());
});

self.addEventListener('fetch', event => {
  const req = event.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;
  const basePath = new URL('./', self.registration.scope).pathname;
  if (!url.pathname.startsWith(basePath)) return;
  const isHtml = req.mode === 'navigate' || url.pathname.endsWith('/index.html') || url.pathname === basePath;
  const isUpdate = url.pathname.endsWith('/update.json');

  if (isUpdate) {
    event.respondWith(fetch(new Request(req, {cache: 'no-store'})).catch(async () => (await caches.match(req)) || Response.error()));
    return;
  }
  if (isHtml) {
    event.respondWith((async () => {
      const cached = await caches.match(req) || await caches.match(new URL('./index.html', self.registration.scope).href);
      if (cached) return cached;
      return fetch(req);
    })());
    return;
  }
  event.respondWith((async () => {
    const cached = await caches.match(req);
    if (cached) return cached;
    const response = await fetch(req);
    if (response.ok) caches.open(CACHE_NAME).then(cache => cache.put(req, response.clone())).catch(() => {});
    return response;
  })());
});
