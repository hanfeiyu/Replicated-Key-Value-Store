# Replicated-Key-Value-Store

***Replicated-Key-Value-Store Service***

- **Multithreaded** distributed system which supports **RESTful** APIs, PUT, GET, DEL and EXIT, on top
    of **TCP/UDP/RMI** using Java.
- Generic node supports both client and server side services.
- Supports fault tolerance by implementing a two-phase algorithm based on
    simplified **RAFT-like** Consensus Algorithm.
- Supports three different **ad hoc** network auto-joining methods: 
    - ***Dynamic adjacent list***: Dynamic adjacency file will be loaded into each node
        periodically.
    - ***UDP self-discovery***: Nodes will send and catch UDP packets to others
        periodically, a cluster based on this method will be able to
        self-discover all nodes within the topology. 
    - ***TCP centralized discovery***: A central communication center will be
        set up to find all the other nodes within the cluster on top of TCP.
- Deployed using **Docker** containers as seperate ends on **AWS EC2** instances.
- Automatic deployment and testing using **Shell/Bash** scripts.

## Devlopment Environment
#### Language and version
  - Java 11.0.6 2020-01-14 LTS

#### IDE
  - Eclipse 2019-12

#### Package tool
  - Maven 4.0.0

#### Development OS 
  - Mac Darwin

#### Testing OS 
  - Mac Darwin, Linux Ubuntu(Docker container)

## Usage
#### Features of `config.sh`

- Remove old built images and containers that includes "tcss558server"/"tcss558client" in the name.
- Build new docker images and containers onto AWS EC2 instances.
- Prepare DF/U/T tests.

#### Parameters of `config.sh`

Prepare DF test, number of DF servers and 1 client will be created: 

```
./config.sh --df_test <number of servers>
```

Prepare DF test, number of U servers and 1 client will be created:

```
./config.sh --u_test <number of servers>
``` 

Prepare T test, 1 T central server will be created:

```
./config.sh --t_test_central 
```

Prepare T test, number of T member servers will be created: ***NOTICE! This parameter will only take effect after `./config.sh --t_test_central` is executed!*** 

```
./config.sh --t_test_member <number of member servers>
```

Clean all the images and containers related to testing: 

```
./config.sh --clean
```

#### In each server container:
Use `ifconfig` to obtain IP address of TCP servers 

#### In the client container:
Use `./bigtest_tc.sh <TCP server IP address>` to run big test of TCP 

## Testing instructions for DF/U/T (Dynamic File / UDP / TCP)
#### DF test (nodes.cfg refreshed every 1 second)
1. Execute `./config.sh --df_test <number of servers>` 
2. Use `ifconfig` to obtain `<TCP server IP address>` of TCP servers in each server container
3. Use `<TCP server IP address>` to update `/tmp/nodes.cfg` manually in each server container, `vim /tmp/nodes.cfg`, vim already installed 
4. You may start testing by executing `./bigtest_tc.sh <TCP server IP address>` 

#### U test (UDP self-discovery)
1. Execute `./config.sh --u_test <number of servers>` 
2. Use `ifconfig` to obtain `<TCP server IP address>` of TCP servers 
3. You may start testing by executing `./bigtest_tc.sh <TCP server IP address>` 

#### T test (TCP centralized discovery)
1. Execute `./config.sh --t_test_central` first to activate a centralized membership KV store 
2. Use `ifconfig` to obtain `<central store IP address>` of central server in its container 
3. Update `./a2_dockerfiles/docker_server/tcss558server_t_member/runserver.sh` manually, i.e. update `T_CENTRAL_IP_ADDR` to be `<central store IP address>` 
4. Execute `./config.sh --t_test_member <number of member servers>` to activate a bunch of member KV stores 
5. Use `ifconfig` to obtain `<TCP server IP address>` of TCP servers in one of the member server container 
6. You may start testing by executing `./bigtest_tc.sh <TCP server IP address>`

## Github link
[hanfeiyu/Replicated-Key-Value-Store](https://github.com/hanfeiyu/Replicated-Key-Value-Store)

## @Author
[Hanfei Yu](https://github.com/hanfeiyu)

