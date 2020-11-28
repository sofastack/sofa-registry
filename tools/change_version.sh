#!/bin/bash
shellDir=$(cd "$(dirname "$0")"; pwd)

shopt -s expand_aliases
if [ ! -n "$1" ] ;then
	echo "Please enter a version"
 	exit 1	
else
  	echo "The version is $1 !"
fi

if [ `uname` == "Darwin" ] ;then
 	echo "This is OS X"
 	alias sed='sed -i ""'
else
 	echo "This is Linux"
 	alias sed='sed -i'
fi

cd $shellDir/..
echo "Change version in registry-parent ===>"
sed "/<project /,/<name>/ s/<version>[^\$].*<\/version>/<version>$1<\/version>/" ./pom.xml

echo "Change version in registry-client-all ===>"
sed "/<project /,/<dependencies/ s/<version>[^\$].*<\/version>/<version>$1<\/version>/" ./client/all/pom.xml

echo "Change version in subproject pom ===>"
for filename in `find . -name "pom.xml" -maxdepth 4`;do
  if [ $filename == "./client/all/pom.xml" ]; then
     continue
  fi
	echo "Deal with $filename"
	sed "/<parent>/,/<\/parent>/ s/<version>[^\$].*<\/version>/<version>$1<\/version>/" $filename
done

#TODO
#echo "Change version in server shell ===>"