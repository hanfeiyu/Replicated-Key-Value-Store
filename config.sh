#! /bin/bash


DOCKER_CLIENT_PATH="./a2_dockerfiles/docker_client"

DOCKER_SERVER_PATH="./a2_dockerfiles/docker_server"

DOCKER_SERVER_TYPE=("tcss558server_df" "tcss558server_u" "tcss558server_t_central" "tcss558server_t_member")

JAR_TARGET_PATH="./tcss558_assignment_2/target/GenericNode-0.0.1-SNAPSHOT.jar"

JAR_FILE="GenericNode.jar"

#
# Show usage
#

show_usage() {
    echo "Usage: "
    echo "      --df_test <number of servers> | prepare DF test, 5 DF servers and 1 client will be created"
    echo "      --u_test <number of servers> | prepare DF test, 5 U servers and 1 client will be created"
    echo "      --t_test_central | prepare T test, 1 T central server will be created"
    echo "      --t_test_member <number of member servers> | prepare T test, 5 T member servers will be created. *** NOTICE! This parameter will only take effect after <--t_test_central> is executed ***"

    exit
}

#
# Show docker images and all ps 
#
show_docker() {
    echo -e "\ndocker images: \n"
    sudo docker images
        
    echo -e "\ndocker ps -a: \n"
    sudo docker ps -a
} 

#
# Clear dangling images if any exists
#

clear_dangling() {
    docker_dangling_images=`sudo docker images | grep "none" | awk '{print $3}'`
        
    if [ -n "$docker_dangling_images" ]
    then
        echo ""
        echo "Remove dangling images"
        echo $docker_dangling_images | xargs sudo docker rmi
    fi
}

#
# Clean all docker images and ps related to assignment 2
#

clean() {
     # Stop old containers
    echo -e "\n********** Stopping old docker containers **********\n"
    
    docker_pid=`sudo docker ps -a | grep -E '.*((tcss558server+)|(tcss558client+))+.*' | awk '{print $1}'`
        
    if [ ! -n "$docker_pid" ]
    then
        echo "No related container is running"
    else
        for pid in $docker_pid
        do
            sudo docker stop $pid
            echo "Container $pid stopped"
        done
    fi
        
    # Remove old images
    echo -e "\n********** Removing old docker images **********\n"
        
    docker_image_id=`sudo docker images | grep -E '.*((tcss558server+)|(tcss558client+))+.*' | awk '{print $1}'`
        
    if [ ! -n "$docker_image_id" ]
    then
        echo "No related image created"
    else
        for image in $docker_image_id
        do
            sudo docker rmi $image
            echo "Image $image removed"
        done
    fi
    
    echo -e "\n********** All clear  **********"

    # Clear dangling images
    clear_dangling
        
    show_docker 
}


#
# Main process
#

#
# If the input is none or wrong, show usage
#
    
if [[ "$#" != "1" ]] && [[ "$#" != "2" ]]
then
    echo "Please specify parameters from usage below"
    show_usage
fi

if [[ "$1" != "--df_test" ]] && [[ "$1" != "--u_test" ]] && [[ "$1" != "--t_test_central" ]] && [[ "$1" != "--t_test_member" ]] && [[ "$1" != "--clean" ]]
then
    echo "Please designate a correct parameter from usage below"
    show_usage
fi

#
# Check if it's the last part (i.e. preparing member servers) of T test preparation
#

if [ "$1" == "--t_test_member" ]
then
     # Stop old T member server containers
    echo -e "\n********** Stopping old docker containers **********\n"
    
    member_server_pid=`sudo docker ps -a | grep -E '.*(tcss558server_t_member+)+.*' | awk '{print $1}'`
        
    if [ ! -n "$member_server_pid" ]
    then
        echo "No related container is running"
    else
        for pid in $member_server_pid
        do
            sudo docker stop $pid
            echo "Container $pid stopped"
        done
    fi
        
    # Remove old images
    echo -e "\n********** Removing old docker images **********\n"
        
    member_server_image_id=`sudo docker images | grep -E '.*(tcss558server_t_member+)+.*' | awk '{print $1}'`
        
    if [ ! -n "$member_server_image_id" ]
    then
        echo "No related image created"
    else
        for image in $member_server_image_id
        do
            sudo docker rmi $image
            echo "Image $image removed"
        done
    fi
    
    echo -e "\n********** All clear  **********"

    # Clear dangling images
    clear_dangling

    # Check if the central server is ready
    central_server_pid=`sudo docker ps -a | grep "tcss558server_t_central" | awk '{print $1}'`

    if [ ! -n "$central_server_pid" ]
    then
        echo "No central server found!"
        echo "Please set up centralized membership KV server first before setting up member servers!"
        echo ""
        show_usage
    else
        # Build and run each docker_server 
        echo -e "\n********** Preparing T test: build member KV servers **********\n"
        
        # Member KV servers
        for i in `seq 1 $2`
        do
            cd $DOCKER_SERVER_PATH/tcss558server_t_member
            
            sudo docker build -t tcss558server_t_member_${i} .
            echo "tcss558server_t_member_${i} image created"

            sudo docker run -d --rm tcss558server_t_member_${i}
            echo "tcss558server_t_member_${i} is running"
        done
        cd ../../../ 
    fi
    
    show_docker

    exit
fi 

#
# Stop all containers and remove images of assignment 2
#

if [ "$1" == "--clean" ]
then
    clean
    exit
fi

#
# Maven clean and install 
#

echo -e "\n********** Maven clean and install **********\n"

cd ./tcss558_assignment_2/
mvn clean install
cd ../

#
# Clean and enable docker test
#

echo -e "\n********** Generating jar file... **********\n"

# Client
if [ -f "$DOCKER_CLIENT_PATH/$JAR_FILE" ]
then
    rm $DOCKER_CLIENT_PATH/$JAR_FILE
    cp $JAR_TARGET_PATH $DOCKER_CLIENT_PATH/$JAR_FILE
else
    cp $JAR_TARGET_PATH $DOCKER_CLIENT_PATH/$JAR_FILE
fi

echo "$DOCKER_CLIENT_PATH/$JAR_FILE created"

# Server
for server in ${DOCKER_SERVER_TYPE[*]}
do
    if [ -f "$DOCKER_SERVER_PATH/$server/$JAR_FILE" ]
    then
        rm $DOCKER_SERVER_PATH/$server/$JAR_FILE
        cp $JAR_TARGET_PATH $DOCKER_SERVER_PATH/$server/$JAR_FILE
    else
        cp $JAR_TARGET_PATH $DOCKER_SERVER_PATH/$server/$JAR_FILE
    fi

    echo "$DOCKER_SERVER_PATH/$server/$JAR_FILE created"
done

#
# Stop old docker containers
#

clean

#
# Build docker_client
#

echo -e "\n********** Build new images and containers for client **********\n"

# Client
cd $DOCKER_CLIENT_PATH
sudo docker build -t tcss558client .
echo "tcss558client image created"
cd ../../

clear_dangling 

#
# Run one client
#

echo -e "\n********** Run client **********\n"

# Client
sudo docker run -d --rm tcss558client
echo "tcss558client is running"

#
# Run corresponding test based on the input
#

if [ "$1" == "--df_test" ]
then
    # Build and run each docker_server 
    echo -e "\n********** Preparing DF test **********\n"
    
    # Server
    for i in `seq 1 $2`
    do
        cd $DOCKER_SERVER_PATH/tcss558server_df
        
        sudo docker build -t tcss558server_df_${i} .
        echo "tcss558server_df_${i} image created"
        
        sudo docker run -d --rm tcss558server_df_${i}
        echo "tcss558server_df_${i} is running"
    done
    cd ../../../
elif [ "$1" == "--u_test" ]
then
    # Build and run each docker_server 
    echo -e "\n********** Preparing U test **********\n"
    
    # Server
    for i in `seq 1 $2`
    do
        cd $DOCKER_SERVER_PATH/tcss558server_u
        
        sudo docker build -t tcss558server_u_${i} .
        echo "tcss558server_u_${i} image created"
        
        sudo docker run -d --rm tcss558server_u_${i}
        echo "tcss558server_u_${i} is running"
    done
    cd ../../../
elif [ "$1" == "--t_test_central" ]
then
    # Build and run docker_server 
    echo -e "\n********** Preparing T test: build centralized membership KV server **********\n"
    
    # Centralized membership KV server
    cd $DOCKER_SERVER_PATH/tcss558server_t_central
    
    sudo docker build -t tcss558server_t_central .
    echo "tcss558server_t_central image created"
    
    sudo docker run -d --rm tcss558server_t_central
    echo "tcss558server_t_central is running"
    
    cd ../../../

    # Member KV server
    echo "Instruction:"
    echo "1. To prepare member servers, first you need to observe IP address of central server, please use [ifconfig] command inside central server container"
    echo "2. Please change the central server ip in the ${DOCKER_SERVER_PATH}/tcss558server_t_member/runserver.sh"
    echo "3. Then please run the config.sh again with command [./config.sh --t_test_member] to finalize the preparation for T test"
    echo "See ya soon!"
    echo ""
else 
    echo ""
    echo "Error! Incorrect parameter!"
    show_usage
fi

clear_dangling 

echo -e "\n********** Ready to test **********"
show_docker

