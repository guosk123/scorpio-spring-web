#!/bin/bash

block0="\n\t<modules>\n\t\t<module>../fpc-common<\/module>\n\t\t<module>..\/fpc-manager<\/module>\n\t<\/modules>"

block1="\n\t<build>\n\t\t<plugins>\n\t\t\t<plugin>\n\t\t\t\t<groupId>com.fortify.ps.maven.plugin<\/groupId>\n\t\t\t\t<artifactId>sca-maven-plugin<\/artifactId>\n\t\t\t\t<version>4.20<\/version>\n\t\t\t<\/plugin>\n\t\t<\/plugins>\n\t<\/build>"

block2="\n\t\t\t<plugin>\n\t\t\t\t<groupId>com.fortify.ps.maven.plugin</groupId>\n\t\t\t\t<artifactId>sca-maven-plugin</artifactId>\n\t\t\t\t<version>4.20</version>\n\t\t\t</plugin>"

sed -i "/<\/parent>/a\\$block0" $1
sed -i "/<\/dependencies>/a\\$block1" $1
sed -i "/<\/plugins>/i\\$block2" $2
sed -i "/<\/plugins>/i\\$block2" $3