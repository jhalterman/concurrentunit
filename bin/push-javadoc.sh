#!/bin/sh
# run from top level dir
git clone git@github.com:jhalterman/concurrentunit.git target/docs -b gh-pages
mvn -Pjavadoc javadoc:javadoc
cd target/docs
git add -A
git commit -m "Updated JavaDocs"
git push origin gh-pages