#!/bin/bash
if [ "$TRAVIS_REPO_SLUG" == "xXAndrew28Xx/MoreStorage" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
    echo -e "Publishing Javadoc...\n"

    cp -R build/docs/javadoc $HOME/javadoc-latest
    cd $HOME

    git config --global user.email "travis@travis-ci.org"
    git config --global user.name "travis-ci"
    git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/xXAndrew28Xx/MoreStorage gh-pages > /dev/null

    cd gh-pages
    git rm -rf ./javadoc
    cp -Rf $HOME/javadoc-latest ./javadoc
    git add -f .
    git commit -m "Javadoc | Travis Build - $TRAVIS_BUILD_NUMBER"
    git push -fq origin gh-pages > /dev/null

    echo -e "Published Javadoc.\n"
fi