#!/bin/bash
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

echo "Change version in root pom.xml ===>"
sed "/<project /,/<name/ s/<version>.*<\/version>/<version>$1<\/version>/" pom.xml
sed "s/<rpc.version>.*<\/rpc.version>/<rpc.version>$1<\/rpc.version>/" pom.xml

echo "Change version in sofa-rpc-bom ===>"
sed "/<project /,/<name/ s/<version>.*<\/version>/<version>$1<\/version>/" sofa-rpc-bom/pom.xml
sed "s/<rpc.version>.*<\/rpc.version>/<rpc.version>$1<\/rpc.version>/" sofa-rpc-bom/pom.xml

echo "Change version in subproject pom ===>"
for filename in `find . -name "pom.xml" -mindepth 2`;do
  if [ $filename == "sofa-rpc-bom/pom.xml" ]; then
     continue
  fi
	echo "Deal with $filename"
	sed "/<parent>/,/<\/parent>/ s/<version>.*<\/version>/<version>$1<\/version>/" $filename
done

echo "Change version in MANIFEST.MF"
echo "Deal with MANIFEST.MF"
sed "s/Plugin-Version.*/Plugin-Version: $1.common/g" sofa-rpc-plugin/src/main/resources/META-INF/MANIFEST.MF
