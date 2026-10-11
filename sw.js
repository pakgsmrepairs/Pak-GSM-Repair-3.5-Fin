/* Pak GSM Repair 4.4 PWA service worker.
   Network-first for HTML and update metadata; cache-first for same-origin static assets. */
const CACHE_NAME = 'pgr-4-4-shell-v1';
const APP_SHELL = [
  './', './index.html', './manifest.json', './update.json',
  './icons/icon-192.png', './icons/icon-512.png', './icons/icon-512-maskable.png'
];

self.addEventListener('install', event => {
  event.waitUntil((async () => {
    const cache = await caches.open(CACHE_NAME);
    try {
      await cache.addAll(APP_SHELL.map(path => new Request(path, { cache: 'reload' })));
    } catch (error) {
      // Installation should still complete if one optional shell URL is temporarily unavailable.
      console.warn('Some app-shell files could not be cached:', error);
    }
    await self.skipWaiting();
  })());
});

self.addEventListener('activate', event => {
  event.waitUntil((async () => {
    const names = await caches.keys();
    await Promise.all(names
      .filter(name => name.startsWith('pgr-') && name !== CACHE_NAME)
      .map(name => caches.delete(name)));
    await self.clients.claim();
  })());
});

self.addEventListener('fetch', event => {
  const req = event.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;

  const basePath = new URL('./', self.registration.scope).pathname;
  const isInAppScope = url.pathname.startsWith(basePath);
  if (!isInAppScope) return;
  const isHtml = req.mode === 'navigate' || url.pathname.endsWith('/index.html') || url.pathname === basePath;
  const isUpdate = url.pathname.endsWith('/update.json');

  if (isHtml || isUpdate) {
    event.respondWith((async () => {
      try {
        const fresh = await fetch(new Request(req, { cache: 'no-store' }));
        if (fresh && fresh.ok && isHtml) {
          const cache = await caches.open(CACHE_NAME);
          cache.put(req, fresh.clone()).catch(() => {});
        }
        return fresh;
      } catch (error) {
        const cached = await caches.match(req) || await caches.match('./index.html') || await caches.match('./');
        if (cached) return cached;
        throw error;
      }
    })());
    return;
  }

  event.respondWith((async () => {
    const cached = await caches.match(req);
    if (cached) {
      // Refresh the cache in the background without delaying the response.
      fetch(req).then(response => {
        if (response && response.ok) caches.open(CACHE_NAME).then(cache => cache.put(req, response.clone()));
      }).catch(() => {});
      return cached;
    }
    try {
      const response = await fetch(req);
      if (response && response.ok) caches.open(CACHE_NAME).then(cache => cache.put(req, response.clone())).catch(() => {});
      return response;
    } catch (error) {
      throw error;
    }
  })());
});

self.addEventListener('message', event => { if (event.data && event.data.type === 'SKIP_WAITING') self.skipWaiting(); });
