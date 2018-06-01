# feup-sdis
Projects for the Distributed Systems (SDIS) class of the Master in Informatics and Computer Engineering (MIEIC) at the Faculty of Engineering of the University of Porto (FEUP). 

- [First Project](#first-project)
- [Second Project](#second-project)

Made in colaboration with [Carlos Freitas](https://github.com/CarlosFr97), [Julieta Frade](https://github.com/julietafrade97) and [Luís Martins](https://github.com/luisnmartins).

**Completed in 28/05/2018.**

## First Project

## Second Project

### Compile and start RMI

```
sh compile.sh
rmiregistry
```

### Run

#### Peer

```
Peer.Peer <version> <server id> <access_point> <MC_port> <MC_IP_address> <MDB_port> <MDB_IP_address> <MDR_port> <MDR_IP_address>
```

Eg.: Peer.Peer 1.0 1 1  8000 224.0.0.1 8001 224.0.0.2 8002 224.0.0.3

#### Backup Protocol

```
RMI.Application //<host>/<peer_access_point> BACKUP <file_path> <desired_replication_degree>
```

Eg.: RMI.Application //localhost/1 BACKUP /usr/users2/2015/Desktop/file.txt 3

#### Restore Protocol

```
RMI.Application //<host>/<peer_access_point> RESTORE <file_path>
```

Eg.: Application //localhost/1 RESTORE /usr/users2/2015/Desktop/file.txt 

#### Delete Protocol

```
RMI.Application //<host>/<peer_access_point> DELETE <file_path>
```

Eg.: RMI.Application //localhost/1 DELETE /usr/users2/2015/Desktop/file.txt

#### Reclaim Protocol

```
RMI.Application //<host>/<peer_access_point> RECLAIM <max_disk_space>
```

Eg.: RMI.Application //localhost/1 RECLAIM 30000

#### State Protocol

```
RMI.Application //<host>/<peer_access_point> STATE
```

Eg.: RMI.Application //localhost/1 STATE

#### Notice

The Backup, Restore, Delete protocols can be called with ENH so that the enhanced version can be used.

Eg.:  RMI.Application localhost/1 ENHBACKUP /usr/users2/2015/Desktop/file.txt 3
