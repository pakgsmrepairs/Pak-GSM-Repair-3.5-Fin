/* Pak GSM Repair 3.7.7 service worker. Network-first for HTML/update metadata so stale app versions do not stick. */
const CACHE_NAME = 'pgr-3-7-7-shell-v1';
const APP_SHELL = ['./', './index.html', './manifest.json', './update.json'];
self.addEventListener('install', event => {
  event.waitUntil((async () => {
    const cache = await caches.open(CACHE_NAME);
    await cache.addAll(APP_SHELL.map(path => new Request(path, {cache:'reload'}))).catch(() => {});
    await self.skipWaiting();
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
  const isHtml = req.mode === 'navigate' || url.pathname.endsWith('/index.html') || url.pathname === new URL('./', self.location.href).pathname;
  const isUpdate = url.pathname.endsWith('/update.json');
  if (isHtml || isUpdate) {
    event.respondWith((async () => {
      try {
        const fresh = await fetch(new Request(req, {cache:'no-store'}));
        if (fresh && fresh.ok && isHtml) {
          const cache = await caches.open(CACHE_NAME);
          cache.put(req, fresh.clone()).catch(() => {});
        }
        return fresh;
      } catch (err) {
        const cached = await caches.match(req) || await caches.match('./index.html') || await caches.match('./');
        if (cached) return cached;
        throw err;
      }
    })());
    return;
  }
  event.respondWith((async () => {
    const cached = await caches.match(req);
    const network = fetch(req).then(response => {
      if (response && response.ok) caches.open(CACHE_NAME).then(cache => cache.put(req, response.clone())).catch(() => {});
      return response;
    }).catch(() => cached);
    return cached || network;
  })());
});
