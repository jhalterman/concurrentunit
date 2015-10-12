# Called by Travis CI to push latest javadoc
# From http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/

PROJECT=concurrentunit

if [ "$TRAVIS_REPO_SLUG" == "$PROJECT" ] && \
   [ "$TRAVIS_PULL_REQUEST" == "false" ] && \
   [ "$TRAVIS_BRANCH" == "master" ]; then
  echo -e "Publishing Javadoc...\n"
  
  mvn javadoc:javadoc
  TARGET="$(pwd)/target"

  cd $HOME
  git clone --quiet https://${GH_TOKEN}@github.com/jhalterman/$PROJECT gh-pages -b gh-pages > /dev/null
  
  cd gh-pages
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git rm -rf javadoc
  mkdir -p javadoc
  mv $TARGET/site/apidocs/* javadoc
  git add -A -f javadoc
  git commit -m "Travis generated Javadoc for $PROJECT build $TRAVIS_BUILD_NUMBER"
  git push -fq origin > /dev/null

  echo -e "Published Javadoc to atomix.github.io.\n"
fi
