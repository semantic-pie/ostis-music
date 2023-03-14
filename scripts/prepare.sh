!#/bin/bash

cd ..

BASE_DIR=`pwd`

git clone https://github.com/semantic-pie/ostis-example-app

if [ -e ostis-example-app ]; then
    echo "ostis-example-app founded, moving kb "
    rm -rf $BASE_DIR/ostis-example-app/music-kb
    cp -r $BASE_DIR/kb $BASE_DIR/ostis-example-app/kb/music-kb
else
    cd ostis-example-app/scripts
    ./install_ostis.sh
    echo "Moving music-kb to ostis-exemaple-app kb"
    cp -r $BASE_DIR/kb $BASE_DIR/ostis-example-app/kb/music-kb
fi
