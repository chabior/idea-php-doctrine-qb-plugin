#!/bin/bash

ideaVersion="2017.1"

travisCache=".cache"

function download {
  url=$1
  basename=${url##*[/|\\]}
  cachefile=${travisCache}/${basename}

  if [ ! -f ${cachefile} ]; then
      wget $url -P ${travisCache};
    else
      echo "Cached file `ls -sh $cachefile` - `date -r $cachefile +'%Y-%m-%d %H:%M:%S'`"
  fi

  if [ ! -f ${cachefile} ]; then
    echo "Failed to download: $url"
    exit 1
  fi
}

if [ ! -d ${travisCache} ]; then
    echo "Create cache" ${travisCache}
    mkdir ${travisCache}
fi

# Download main idea folder
download "http://download.jetbrains.com/idea/ideaIU-${ideaVersion}.tar.gz"
tar zxf ${travisCache}/ideaIU-${ideaVersion}.tar.gz -C .

# Move the versioned IDEA folder to a known location
ideaPath=$(find . -name 'idea-IU*' | head -n 1)
rm -Rf ./idea
mkdir idea
mv ${ideaPath}/* ./idea

rm -rf plugins
mkdir plugins

#php
download "http://phpstorm.espend.de/files/proxy/phpstorm-2017.1-php.zip"
unzip -qo $travisCache/phpstorm-2017.1-php.zip -d ./plugins

ant test

stat=$?

exit ${stat}