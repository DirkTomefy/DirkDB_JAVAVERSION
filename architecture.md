# Architecture réseau

## Schéma global

```text
+-------------------+        TCP (haute disponibilité des serveurs)        +---------------------------+
| ClientSocket / CLI|  -> ProxyCache  -----------------------------------> | Load Balancer (LB :3949)  |
+-------------------+     (pour être plus rapide, cache local côté client) +---------------------------+
                                                                             |               |
                                                                             | TCP           | TCP
                                                                             v               v
                                                              +----------------+  +----------------+
                                                              | ServerSocket   |  | ServerSocket   |
                                                              | m1 :3948       |  | m2 :3950       |
                                                              | data/m1        |  | data/m2        |
                                                              +----------------+  +----------------+
```

## Objectif

- Le **Load Balancer** assure la **haute disponibilité** et le routage vers les serveurs.
- Le **ProxyCache** est une couche **locale (côté client)** avec un **temps de rétention (TTL)** pour accélérer les lectures.

## Rôle des composants

### 1) ClientSocket / CLI

- Client interactif.
- Envoie des commandes terminées par `;`.
- Lit la réponse jusqu'à la ligne `END`.

### 2) ProxyCache (côté client, local)

- Petit proxy TCP local placé **entre** le client et le LB.
- Il met en cache les réponses des requêtes de lecture (READ) pendant un TTL.

#### Temps de rétention (TTL)

- Une entrée du cache est valide uniquement pendant `TTL` millisecondes.
- Après expiration, la prochaine lecture sera de nouveau forwardée au LB.

#### Stratégie simple (recommandée)

- **READ** (`alaivo`, `asehoy`): cacheable.
- **WRITE** (insert/update/delete/create/drop): forward + **invalidation du cache**.
- **USE** (`ampiasao <db>`): forward + **invalidation du cache** (pour éviter des confusions entre DB).

### 3) Load Balancer (LB)

- Processus TCP qui écoute sur `:3949`.
- Route les requêtes vers les instances `m1` et `m2`.
- Avec `AsyncReplicatingProxy`:
  - routage READ/WRITE
  - synchronisation de `ampiasao` (session)
  - (option) réplication master↔master par copie de fichiers après WRITE.

### 4) ServerSocket (m1/m2)

- Exécute la requête SQL (parse + eval).
- Écrit sur disque dans son `dataDirectory`:
  - tables: `<dataDir>/<db>/tables/<table>.json`
  - views: `<dataDir>/<db>/views/<view>.json`
  - domains: `<dataDir>/<db>/domains/<domain>.json`

## Flux concrets

### A) READ (ex: `alaivo * ao@ t1;`)

1. ClientSocket -> ProxyCache
2. ProxyCache:
   - si cache hit (TTL OK): renvoie la réponse
   - sinon: forward -> LB
3. LB -> ServerSocket (m1 ou m2)
4. réponse -> LB -> ProxyCache -> ClientSocket

### B) WRITE (ex: `manampia ao@ t1 ...;`)

1. ClientSocket -> ProxyCache
2. ProxyCache forward -> LB
3. LB -> ServerSocket (m1 ou m2)
4. réponse -> ClientSocket
5. ProxyCache invalide le cache

### C) USE (ex: `ampiasao db1;`)

1. ClientSocket -> ProxyCache
2. ProxyCache forward -> LB
3. LB -> ServerSocket (m1 ou m2)
4. réponse -> ClientSocket
5. ProxyCache invalide le cache (stratégie simple)

## Paramètres suggérés (ProxyCache)

- `TTL`: 2000ms à 10000ms pour commencer.
- `maxEntries`: 100 à 1000 selon la mémoire.

## Démarrage (exemple)

- Démarrer `m1` et `m2` (deux terminaux), puis le LB.
- Ensuite démarrer le ProxyCache local, puis connecter le client sur le port local du ProxyCache.