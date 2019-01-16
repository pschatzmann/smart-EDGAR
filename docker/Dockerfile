FROM openjdk:alpine
MAINTAINER Phil Schatzmann <pschatzmann@gmail.com>
RUN mkdir -p /usr/local/bin/SmartEdgar/data
COPY *.* /usr/local/bin/SmartEdgar/
WORKDIR /usr/local/bin/SmartEdgar
RUN chmod +x start.sh
ENV indexFileName /usr/local/bin/SmartEdgar/data/index.csv
ENV destinationFolder /usr/local/bin/SmartEdgar/data
ENV formsRegex 10-K.*
ENV updateIndex true
ENV minPeriod 2005-04
ENV xmx 3000m 

EXPOSE 9997
VOLUME /usr/local/bin/SmartEdgar/data

CMD ./start.sh ch.pschatzmann.edgar.service.Main
 

