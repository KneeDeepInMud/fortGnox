#!/bin/bash

here=$(dirname $0)
here=$(cd $here/.. && pwd -P)


lasttag=$(git tag | tail -1)

[[ -z $lasttag ]] && {
    echo Cannot determin tag
    exit 1
}

jar=$(ls -1rt $here/target/fortgnox*jar | tail -1)

[[ -z $jar ]] && {
    echo No jar file found
    exit 1
}

bjar=$(basename $jar)
njar=$(echo $bjar | sed "s,\(fortgnox-\)\([a-z0-9]\+.jar\),\1${lasttag}-\2,")

cp -v $here/target/$bjar $here/target/$njar