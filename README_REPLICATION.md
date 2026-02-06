# Réplication DirkDB

## Options disponibles

### 1. Réplication synchrone (ReplicatingProxy.java)
- **Avantages** : Cohérence forte, données toujours synchronisées
- **Inconvénients** : Plus lent (attend les 2 serveurs)
- **Usage** : Quand la cohérence est critique

```bash
# Lancement
java -cp target/classes sqlTsinjo.socket.proxy.ReplicatingProxy
```

### 2. Réplication asynchrone (AsyncReplicatingProxy.java)
- **Avantages** : Rapide (répond après écriture primaire)
- **Inconvénients** : Risque de décalage (replication lag)
- **Usage** : Performance > cohérence immédiate

```bash
# Lancement
java -cp target/classes sqlTsinjo.socket.proxy.AsyncReplicatingProxy
```

### 3. Réplication fichier (FileReplicator.java)
- **Avantages** : Simple, pas de modification du protocole
- **Inconvénients** : Délai de réplication, conflits possibles
- **Usage** : Complément des autres méthodes

```bash
# Test
java -cp target/classes sqlTsinjo.storage.FileReplicator
```

## Architecture recommandée

### Pour production
```
Client → AsyncReplicatingProxy (port 3949)
        → Serveur Primaire (127.0.0.1:3948) [écritures + lectures]
        → Serveur Secondaire (127.0.0.1:3950) [réplication async + lectures]
```

### Pour développement/tests
```
Client → ReplicatingProxy (port 3949)
        → Serveur 1 (127.0.0.1:3948)
        → Serveur 2 (127.0.0.1:3950)
```

## Configuration

### Ports
- Primaire : 3948
- Secondaire : 3950
- Proxy : 3949

### Lancement complet

```bash
# Terminal 1 - Serveur primaire
java -cp target/classes sqlTsinjo.socket.server.ServerSocket

# Terminal 2 - Serveur secondaire
java -cp target/classes sqlTsinjo.socket.server.ServerSocket 3950

# Terminal 3 - Proxy avec réplication
java -cp target/classes sqlTsinjo.socket.proxy.AsyncReplicatingProxy

# Optionnel - Terminal 4 - Réplication fichier (backup)
java -cp target/classes sqlTsinjo.storage.FileReplicator
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
