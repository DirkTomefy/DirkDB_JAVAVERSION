# Réplication DirkDB

## Options disponibles

### 1. Load balancer + réplication (AsyncReplicatingProxy)

- Le proxy `AsyncReplicatingProxy` route les requêtes selon `config.json`.
- Il peut déclencher une réplication (ex: copie de fichiers après WRITE, selon l'implémentation active).

```bash
./mvnw -q -DskipTests exec:java \
  -Dexec.mainClass=sqlTsinjo.socket.proxy.AsyncReplicatingProxy \
  -Ddirk.configPath="$PWD/config.json"
```

### 2. Réplication fichier (FileReplicator)
- **Avantages** : Simple, pas de modification du protocole
- **Inconvénients** : Délai de réplication, conflits possibles
- **Usage** : Complément des autres méthodes

```bash
# Test
./mvnw -q -DskipTests exec:java -Dexec.mainClass=sqlTsinjo.storage.FileReplicator
```

## Architecture recommandée

### Pour développement
```
Client → (optionnel) LocalProxyCacheServer (port 3951) → AsyncReplicatingProxy (port 3949)
                                                  → m1 (127.0.0.1:3948)
                                                  → m2 (127.0.0.1:3950)
```

## Configuration

### Ports
- Primaire : 3948
- Secondaire : 3950
- Proxy : 3949

### Lancement complet

```bash
# Terminal 1 - Serveur m1
./mvnw -q -DskipTests exec:java \
  -Dexec.mainClass=sqlTsinjo.socket.server.ServerSocket \
  -Ddirk.configPath="$PWD/config.json" \
  -Ddirk.instanceId=m1

# Terminal 2 - Serveur m2
./mvnw -q -DskipTests exec:java \
  -Dexec.mainClass=sqlTsinjo.socket.server.ServerSocket \
  -Ddirk.configPath="$PWD/config.json" \
  -Ddirk.instanceId=m2

# Terminal 3 - LB
./mvnw -q -DskipTests exec:java \
  -Dexec.mainClass=sqlTsinjo.socket.proxy.AsyncReplicatingProxy \
  -Ddirk.configPath="$PWD/config.json"

# Terminal 4 - (optionnel) ProxyCache local
./mvnw -q -DskipTests exec:java \
  -Dexec.mainClass=sqlTsinjo.socket.proxy.LocalProxyCacheServer \
  -Dexec.args="127.0.0.1 3949 3951 3000 200"

# Terminal 5 - Client (connecté au ProxyCache local)
./mvnw -q -DskipTests exec:java \
  -Dexec.mainClass=sqlTsinjo.socket.client.ClientSocket \
  -Dexec.args="127.0.0.1 3951"
```

## Gestion des pannes

### Si le primaire tombe
- Les écritures échouent (normal)
- Les lectures continuent sur le secondaire
- Solution : bascule manuelle ou automatique vers le secondaire

### Si le secondaire tombe
- Le système continue de fonctionner (pas de réplication)
- Les écritures s'accumulent sur le primaire
- Solution : relancer le secondaire, il se resynchronisera

## Monitoring

### Logs à surveiller
- "Erreur réplication sur" : secondaire injoignable
- "Erreur connexion à" : serveur backend down
- "Répliqué:" : réplication fichier réussie

### Vérification manuelle
```sql
-- Sur les 2 serveurs, vérifier que les données sont identiques
AMPIASAO testdb;
ALAIVO * FROM utilisateurs;
```

## Limitations actuelles

1. **Pas de détection automatique de split-brain**
2. **Pas de reconnexion automatique**
3. **Pas de résolution de conflits** (dernière écriture gagne)
4. **Pas de compression** pour la réplication réseau

## Évolutions possibles

1. **Heartbeat** entre serveurs
2. **Élection automatique** du primaire
3. **Compression** des données répliquées
4. **Mode multi-master** avec résolution de conflits
5. **Snapshots** pour recovery rapide
