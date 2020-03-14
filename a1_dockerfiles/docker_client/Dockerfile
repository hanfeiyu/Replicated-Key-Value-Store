FROM ubuntu
RUN apt-get update
RUN apt-get install -y default-jre 
RUN apt-get install -y net-tools
RUN apt-get install -y vim
COPY GenericNode.jar /
COPY entrypoint_client.sh /
COPY bigtest_tc.sh /
ENTRYPOINT ["/entrypoint_client.sh"]
CMD ["300"]


