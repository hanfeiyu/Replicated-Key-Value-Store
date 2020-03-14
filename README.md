# Devlopment Environment
## Language and version
  - Java 11.0.6 2020-01-14 LTS

## IDE
  - Eclipse 2019-12

## Package tool
  - Maven 4.0.0

## Development OS 
  - Mac Darwin

## Testing OS 
  - Mac Darwin

# Usage
  - Features of `config.sh`: 
    - Remove old built images and containers that includes
        "tcss558server"/"tcss558client" in the name
    - Build new docker images and containers 
    - Prepare DF/U/T tests

  -  Parameters of `config.sh`:
    - `./config.sh --df_test <number of servers>` | prepare DF test, number of DF servers and 1 client will be created
    - `./config.sh --u_test <number of servers>` | prepare DF test, number of U servers and 1 client will be created"
    - `./config.sh --t_test_central` | prepare T test, 1 T central server will be created"
    - `./config.sh --t_test_member <number of member servers>` | prepare T test, number of T member servers will be created. \*\*\* NOTICE! This parameter will only take effect after `./config.sh <--t_test_central>` is executed \*\*\*
    - `./config.sh --clean` | clean all the images and containers related to assignment 2

  - In each server container:
    - Use `ifconfig` to obtain IP address of TCP servers

  - In the client container:
    - Use `./bigtest_tc.sh <TCP server IP address>` to run big test of TCP

# Testing instructions for DF/U/T
  - DF
    - 1. Execute `./config.sh --df_test <number of servers>`
    - 2. Use `ifconfig` to obtain `<TCP server IP address>` of TCP servers in each server
         container
    - 3. Use `<TCP server IP address>` to update `/tmp/node.cfg` manually in each server container
    - 4. You may start testing by executing `./bigtest_tc.sh <TCP server IP address>`

  - U
    - 1. Execute `./config.sh --u_test <number of servers>`
    - 2. Use `ifconfig` to obtain `<TCP server IP address>` of TCP servers
    - 3. You may start testing by executing `./bigtest_tc.sh <TCP server IP address>`

  - T
    - 1. Execute `./config.sh --t_test_central` first to activate a
         centralized membership KV store
    - 2. Use `ifconfig` to obtain `<central store IP address>` of central server in its
         container
    - 3. Update `./a1_dockerfiles/docker_server/tcss558server_t_member/runserver.sh` 
         manually, i.e. update `T_CENTRAL_IP_ADDR` to be `<central store IP address>`
    - 4. Execute `./config.sh --t_test_member <number of member servers>` to activate 
         a bunch of member KV stores
    - 5. Use `ifconfig` to obtain `<TCP server IP address>` of TCP servers in
         one of the member server container
    - 6. You may start testing by executing `./bigtest_tc.sh <TCP server IP address>`

# Github link
[hanfeiyu/generic-node](https://github.com/hanfeiyu/genericNode)


