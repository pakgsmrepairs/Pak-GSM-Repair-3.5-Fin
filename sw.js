/* Pak GSM Repair PWA worker.
   App shell is cache-first: a published GitHub change is NOT served until the user
   checks for an update, downloads the release, and approves the restart. */
const CACHE_NAME = 'pak-gsm-repair-v4.4.7';
const STAGING_CACHE = 'pgr-update-staging';
const APP_SHELL = [
  './', './index.html', './manifest.json', './update.json', './cloud-config.js',
  './icons/icon-192.png', './icons/icon-512.png', './icons/icon-512-maskable.png', './icons/pgr-logo.svg'
];
const absolute = path => new URL(path, self.registration.scope).toString();

self.addEventListener('install', event => {
  event.waitUntil((async () => {
    const cache = await caches.open(CACHE_NAME);
    // Initial install only: seed a local shell. Do not force-skip a waiting worker.
    await Promise.all(APP_SHELL.map(async path => {
      try {
        const request = new Request(absolute(path), { cache: 'reload' });
        const response = await fetch(request);
        if (response.ok) await cache.put(request, response.clone());
      } catch (error) { /* Keep installation available even if an optional asset fails. */ }
    }));
  })());
});

self.addEventListener('activate', event => {
  event.waitUntil((async () => {
    const names = await caches.keys();
    await Promise.all(names.filter(name =>
      name.startsWith('pak-gsm-repair-v') && name !== CACHE_NAME
    ).map(name => caches.delete(name)));
    await self.clients.claim();
  })());
});

self.addEventListener('fetch', event => {
  const req = event.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;
  const scopePath = new URL(self.registration.scope).pathname;
  if (!url.pathname.startsWith(scopePath)) return;

  const isHtml = req.mode === 'navigate' || url.pathname.endsWith('/index.html') || url.pathname === scopePath;
  const isUpdate = url.pathname.endsWith('/update.json');

  // Update metadata is always network checked, but app files stay pinned to the
  // currently approved release until the user taps Update App.
  if (isUpdate) {
    event.respondWith((async () => {
      try {
        const fresh = await fetch(new Request(req, { cache: 'no-store' }));
        if (fresh.ok) return fresh;
      } catch (e) {}
      return await caches.match(req, { ignoreSearch: true }) || Response.error();
    })());
    return;
  }

  event.respondWith((async () => {
    const stage = await caches.open(STAGING_CACHE);
    const approved = await stage.match(absolute('./__update_approved'));
    if (approved) {
      const stagedRequest = isHtml ? absolute('./index.html') : new URL(url.pathname + url.search, self.location.origin).toString();
      const staged = await stage.match(stagedRequest, { ignoreSearch: true });
      if (staged) return staged;
    }

    const cache = await caches.open(CACHE_NAME);
    const cached = await cache.match(req, { ignoreSearch: true });
    if (cached) return cached;
    try {
      const fresh = await fetch(req);
      if (fresh && fresh.ok) await cache.put(req, fresh.clone());
      return fresh;
    } catch (error) {
      if (isHtml) return await cache.match(absolute('./index.html')) || await cache.match(absolute('./')) || Response.error();
      return Response.error();
    }
  })());
});

self.addEventListener('message', event => {
  if (event.data && event.data.type === 'SKIP_WAITING') self.skipWaiting();
});
