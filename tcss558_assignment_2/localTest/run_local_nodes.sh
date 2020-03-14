#! /bin/bash

port=1234
port2=1235
port3=1236
ipAddr="localhost"

# Display uage if input is null
if [ -z $1 ]
then 
    echo "usage:"
    echo -e "\t./run_nodes.sh <tcp/udp/rmi>"
    exit
fi


if [ $1 == "tcp" ]
then
    # Test TCP 
    echo "Testing TCP..."
    java -jar genericNodeServer.jar ts $port &

    echo -e "\nput a 123:"
    java -jar genericNodeClient.jar tc localhost $port put a 123
    echo -e "\nput b 456:"
    java -jar genericNodeClient.jar tc localhost $port put b 456
    echo -e "\nput c 789:"
    java -jar genericNodeClient.jar tc localhost $port put c 789
    echo -e "\nput a 123:"
    java -jar genericNodeClient.jar tc localhost $port put a 123
    echo -e "\nget a:"
    java -jar genericNodeClient.jar tc localhost $port get a
    echo -e "\ndel a:"
    java -jar genericNodeClient.jar tc localhost $port del a
    echo -e "\nget a:"
    java -jar genericNodeClient.jar tc localhost $port get a 
    echo -e "\nstore:"
    java -jar genericNodeClient.jar tc localhost $port store 
    echo -e "\nexit:"
    java -jar genericNodeClient.jar tc localhost $port exit
elif [ $1 == "udp" ] 
then
    # Test UDP
    echo "Testing UDP..."
    java -jar genericNodeServer.jar us $port &

    echo -e "\nput a 123:"
    java -jar genericNodeClient.jar uc localhost $port put a 123
    echo -e "\nput b 456:"
    java -jar genericNodeClient.jar uc localhost $port put b 456
    echo -e "\nput c 789:"
    java -jar genericNodeClient.jar uc localhost $port put c 789
    echo -e "\nput a 123:"
    java -jar genericNodeClient.jar uc localhost $port put a 123
    echo -e "\nget a:"
    java -jar genericNodeClient.jar uc localhost $port get a
    echo -e "\ndel a:"
    java -jar genericNodeClient.jar uc localhost $port del a
    echo -e "\nget a:"
    java -jar genericNodeClient.jar uc localhost $port get a 
    echo -e "\nstore:"
    java -jar genericNodeClient.jar uc localhost $port store 
    echo -e "\nexit:"
    java -jar genericNodeClient.jar uc localhost $port exit
elif [ $1 == "rmi" ]
then
    # Test RMI
    echo "Testing RMI..."
    java -jar genericNodeServer.jar rmis $port $port2 &

    echo -e "\nput a 123:"
    java -jar genericNodeClient.jar rmic localhost $port put a 123
    echo -e "\nput b 456:"
    java -jar genericNodeClient.jar rmic localhost $port put b 456
    echo -e "\nput c 789:"
    java -jar genericNodeClient.jar rmic localhost $port put c 789
    echo -e "\nput a 123:"
    java -jar genericNodeClient.jar rmic localhost $port put a 123
    echo -e "\nget a:"
    java -jar genericNodeClient.jar rmic localhost $port get a
    echo -e "\ndel a:"
    java -jar genericNodeClient.jar rmic localhost $port del a
    echo -e "\nget a:"
    java -jar genericNodeClient.jar rmic localhost $port get a 
    echo -e "\nstore:"
    java -jar genericNodeClient.jar rmic localhost $port store 
    echo -e "\nexit:"
    java -jar genericNodeClient.jar rmic localhost $port exit
elif [ $1 == "tus" ]
then
    # Test TCP/UDP
    echo "Testing TCP/UDP..."
    java -jar genericNodeServer.jar tus $port $port2 &

    echo -e "\nput a 123:"
    java -jar genericNodeClient.jar tc localhost $port put a 123
    echo -e "\nput b 456:"
    java -jar genericNodeClient.jar tc localhost $port put b 456
    echo -e "\nput c 789:"
    java -jar genericNodeClient.jar tc localhost $port put c 789
    echo -e "\nput a 123:"
    java -jar genericNodeClient.jar tc localhost $port put a 123
    echo -e "\nget a:"
    java -jar genericNodeClient.jar tc localhost $port get a
    echo -e "\ndel a:"
    java -jar genericNodeClient.jar tc localhost $port del a
    echo -e "\nstore:"
    java -jar genericNodeClient.jar tc localhost $port store 
    echo -e "\nexit:"
    java -jar genericNodeClient.jar tc localhost $port exit

    echo -e "\nput a 123:"
    java -jar genericNodeClient.jar uc localhost $port2 put a 123
    echo -e "\nput b 456:"
    java -jar genericNodeClient.jar uc localhost $port2 put b 456
    echo -e "\nput c 789:"
    java -jar genericNodeClient.jar uc localhost $port2 put c 789
    echo -e "\nput a 123:"
    java -jar genericNodeClient.jar uc localhost $port2 put a 123
    echo -e "\nget a:"
    java -jar genericNodeClient.jar uc localhost $port2 get a
    echo -e "\ndel a:"
    java -jar genericNodeClient.jar uc localhost $port2 del a
    echo -e "\nstore:"
    java -jar genericNodeClient.jar uc localhost $port2 store 
    echo -e "\nexit:"
    java -jar genericNodeClient.jar uc localhost $port2 exit
fi


