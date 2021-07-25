#!/bin/bash

here=$(dirname $0)
here=$(cd $here/.. && pwd -P)

lasttag=$(git log  --decorate | grep tag: | sed 's/^.*tag: \([^,(]\+\)..*/\1/' | head -1)

# lasttag=$(git tag | tail -1)

[[ -z $lasttag ]] && {
    echo Cannot determine tag, skipped
    exit 0
}

jar=$(ls -1rt $here/target/fortgnox*jar | tail -1)

[[ -z $jar ]] && {
    echo No jar file found, skipped
    exit 0
}

bjar=$(basename $jar)
#njar=$(echo $bjar | sed "s,\(fortgnox-\)\([a-z0-9]\+.jar\),\1${lasttag}-\2,")
njar=$(echo $bjar | sed "s,\(fortgnox-\)\([a-z0-9]\+\)\(.jar\),\1${lasttag}\3,")

cp -v $here/target/$bjar $here/target/$njar
