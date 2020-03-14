#! /bin/bash

NODE_LIST="/tmp/nodes.cfg"

# Maven clean and install 
cd ..
mvn clean install
cd localTest

# Create "/tmp/node.cfg"
if [ -e "$NODE_LIST" ]
then
    rm $NODE_LIST
    echo "192.168.0.5:1234" > $NODE_LIST
    echo "192.168.0.5:1235" >> $NODE_LIST
    echo "192.168.0.5:1236" >> $NODE_LIST
else
    echo "192.168.0.5:1234" > $NODE_LIST
    echo "192.168.0.5:1235" >> $NODE_LIST
    echo "192.168.0.5:1236" >> $NODE_LIST
fi

# Enable local test
cp ../target/genericNode-0.0.1-SNAPSHOT.jar GenericNode.jar
cp ../target/genericNode-0.0.1-SNAPSHOT.jar GenericNode2.jar
cp ../target/genericNode-0.0.1-SNAPSHOT.jar GenericNode3.jar
cp ../target/genericNode-0.0.1-SNAPSHOT.jar GenericNodeClient.jar

