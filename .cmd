# Terminal 1
java -cp target/classes sqlTsinjo.socket.server.ServerSocket

# Terminal 2
java -cp target/classes sqlTsinjo.socket.server.ServerSocket 3950

# Terminal 3
java -cp target/classes sqlTsinjo.socket.proxy.AsyncReplicatingProxy