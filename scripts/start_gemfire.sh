#!/usr/bin/env bash


SAVED="`pwd`"
cd "`dirname \"$PRG\"`/.." >&-
APP_HOME="`pwd -P`"
cd "$SAVED" >&-

function waitForPort {

    (exec 6<>/dev/tcp/127.0.0.1/$1) &>/dev/null
    while [ $? -ne 0 ]
    do
        echo -n "."
        sleep 1
        (exec 6<>/dev/tcp/127.0.0.1/$1) &>/dev/null
    done
}

DEFAULT_LOCATOR_MEMORY="--initial-heap=128m --max-heap=128m"

DEFAULT_SERVER_MEMORY="--initial-heap=2g --max-heap=2g"

DEFAULT_JVM_OPTS=" --mcast-port=0"

LOCATOR_OPTS="${DEFAULT_LOCATOR_MEMORY} ${DEFAULT_JVM_OPTS}"
LOCATOR_OPTS="${LOCATOR_OPTS} --name=locator_`hostname`"
LOCATOR_OPTS="${LOCATOR_OPTS} --port=10334"
LOCATOR_OPTS="${LOCATOR_OPTS} --dir=${APP_HOME}/data/locator"

SERVER_OPTS="${DEFAULT_SERVER_MEMORY} ${DEFAULT_JVM_OPTS}"
SERVER_OPTS="${SERVER_OPTS} --locators=localhost[10334]"
SERVER_OPTS="${SERVER_OPTS} --server-port=0"


mkdir -p ${APP_HOME}/data/locator
mkdir -p ${APP_HOME}/data/server1
mkdir -p ${APP_HOME}/data/server2


gfsh --e "start locator ${LOCATOR_OPTS}"  &

waitForPort 10334

gfsh --e "start server  ${SERVER_OPTS} --name=server1 --dir=${APP_HOME}/data/server1" &
gfsh --e "start server  ${SERVER_OPTS} --name=server2 --dir=${APP_HOME}/data/server2" &

wait

wget https://repo1.maven.org/maven2/org/springframework/spring-context/5.2.10.RELEASE/spring-context-5.2.10.RELEASE.jar

gfsh << ENDGFSH
connect
   create region --name=test --type=PARTITION  --entry-time-to-live-expiration=60 --entry-time-to-live-expiration-action=destroy --enable-statistics=true
   deploy --jar=spring-context-5.2.11.RELEASE.jar
ENDGFSH

