# Load Balancer DirkDB (Round-Robin)

## Architecture
```
Client → (optionnel) ProxyCache local (port 3951) → Load Balancer (port 3949)
                                              → Serveur 1 (127.0.0.1:3948)
                                              → Serveur 2 (127.0.0.1:3950)
```

## Prérequis
- 2 instances DirkDB lancées avec réplication des données
- Ports 3948 et 3950 libres

## Démarrage

### 1. Lancer les serveurs DirkDB
```bash
# Terminal 1 - Serveur 1
./mvnw -q -DskipTests exec:java \
  -Dexec.mainClass=sqlTsinjo.socket.server.ServerSocket \
  -Ddirk.configPath="$PWD/config.json" \
  -Ddirk.instanceId=m1

# Terminal 2 - Serveur 2
./mvnw -q -DskipTests exec:java \
  -Dexec.mainClass=sqlTsinjo.socket.server.ServerSocket \
  -Ddirk.configPath="$PWD/config.json" \
  -Ddirk.instanceId=m2
```

### 2. Lancer le load balancer
```bash
# Terminal 3
./mvnw -q -DskipTests exec:java \
  -Dexec.mainClass=sqlTsinjo.socket.proxy.AsyncReplicatingProxy \
  -Ddirk.configPath="$PWD/config.json"
```

### 3. (Optionnel) Lancer le ProxyCache local (côté client)
```bash
# Terminal 4
./mvnw -q -DskipTests exec:java \
  -Dexec.mainClass=sqlTsinjo.socket.proxy.LocalProxyCacheServer \
  -Dexec.args="127.0.0.1 3949 3951 3000 200"
```

### 4. Tester
```bash
# Les clients se connectent au port 3951 (ProxyCache local) ou 3949 (LB)
telnet localhost 3951
```

## Configuration
Pour changer les IPs/ports des instances, modifier `config.json`.

## Comportement
- Lecture/écriture routées par le LB selon `config.json`.

## Réplication
La réplication dépend de la stratégie activée (ex: copie de fichiers côté LB, ou autre mécanisme).
