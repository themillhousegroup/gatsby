#!/bin/sh

# This script, if invoked with any argument (e.g. -f) first ensures that the latest Gatsby code has been compiled and built into a JAR
# on the classpath, before then calling gatling.sh.

# This is useful when developing new Gatsby features and wanting to test them out with real user-tests.

# You should invoke this from the project root, i.e. ./bin/gatsby.sh

if [ $# -gt 0 ]
then
  echo "Forcing an SBT stage..."
  sbt stage
  SBT_STAGE_RESULT=$?
fi

if [ $SBT_STAGE_RESULT -eq 0 ]
then
  echo "Now launching Gatling..."
  ./bin/gatling.sh
fi
