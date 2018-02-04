#!/bin/bash
echo -e "Starting Spigot Build...\n"
cd $HOME
mkdir -p ./spigot-build
cd ./spigot-build

if [ ! -f spigot-1.12.jar ]; then
    wget -O BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar > /dev/null
    echo -e "Actually Building Spigot...\n"
    java -jar BuildTools.jar --rev 1.12 > /dev/null
fi

mvn install:install-file -Dfile=spigot-1.12.jar -DgroupId=org.spigotmc -DartifactId=spigot -Dversion=1.12.2-R0.1-SNAPSHOT -Dpackaging=jar > /dev/null
echo -e "Done with Spigot...\n"