-COMPILING-
javac *.java

-STARTING RMI-
rmiregistry

-RUNNING-
> Peer:
Peer <version> <server id> <access_point> <MC_IP_address> <MC_port> <MDB_IP_address> <MDB_port> <MDR_IP_address> <MDR_port>
E.g.: Peer 2.0 1 Peer1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

> Backup:
App <host>/<peer_access_point> BACKUP <file_path> <desired_replication_degree>
E.g.:  App localhost/Peer1 BACKUP C:\Users\johndoe\Documents\sdis.pdf 2

> Restore:
App <host>/<peer_access_point> RESTORE <file_path>
E.g.:  App localhost/Peer1 RESTORE C:\Users\johndoe\Documents\sdis.pdf

> Delete:
App <host>/<peer_access_point> DELETE <file_path>
E.g.:  App localhost/Peer1 DELETE C:\Users\johndoe\Documents\sdis.pdf

> Reclaim Protocol:
App <host>/<peer_access_point> BACKUP <max_amount_disk_space>
E.g.:  App localhost/Peer1 RECLAIM 65000

> State:
App <host>/<peer_access_point> STATE
E.g.:  App localhost/Peer1 STATE


