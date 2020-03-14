#!/bin/bash

NODE_LIST="/tmp/nodes.cfg"

DF_SERVER_IP_ADDR_1=192.168.0.5
DF_SERVER_IP_ADDR_2=192.168.0.5
DF_SERVER_IP_ADDR_3=192.168.0.5
DF_SERVER_IP_ADDR_4=192.168.0.5
DF_SERVER_IP_ADDR_5=192.168.0.5

T_CENTRAL_IP_ADDR=192.168.0.5

# DF
# Create "/tmp/node.cfg"
#if [ -e "$NODE_LIST" ]
#then
#    rm $NODE_LIST
#    echo "${DF_SERVER_IP_ADDR_1}:1234" > $NODE_LIST
#    echo "${DF_SERVER_IP_ADDR_2}:1234" >> $NODE_LIST
#    echo "${DF_SERVER_IP_ADDR_2}:1234" >> $NODE_LIST
#    echo "${DF_SERVER_IP_ADDR_4}:1234" >> $NODE_LIST
#    echo "${DF_SERVER_IP_ADDR_5}:1234" >> $NODE_LIST
#else
#    echo "${DF_SERVER_IP_ADDR_1}:1234" > $NODE_LIST
#    echo "${DF_SERVER_IP_ADDR_2}:1234" >> $NODE_LIST
#    echo "${DF_SERVER_IP_ADDR_3}:1234" >> $NODE_LIST
#    echo "${DF_SERVER_IP_ADDR_4}:1234" >> $NODE_LIST
#    echo "${DF_SERVER_IP_ADDR_5}:1234" >> $NODE_LIST
#fi

#java -jar GenericNode.jar ts 1 1234

# U
java -jar GenericNode.jar ts 2 1234

# T centralized membership KV store
#java -jar GenericNode.jar ts 3 

# T member KV server
#java -jar GenericNode.jar ts 3 1234 $T_CENTRAL_IP_ADDR

