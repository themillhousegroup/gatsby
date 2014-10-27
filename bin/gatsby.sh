#!/bin/sh

# This script, if invoked with a -f argument first ensures that the latest Gatsby code has been compiled and built into a JAR
# on the classpath, before then calling gatling.sh.

# This is useful when developing new Gatsby features and wanting to test them out with real user-tests.

# You should invoke this from the project root, i.e. ./bin/gatsby.sh

# To run non-interactively (i.e. skip prompting of the simulation to be run) use:

# ./bin/gatsby.sh [-f] -s <simulation.class>

# e.g. ./bin/gatsby.sh -f -s gatsbyexamples.BasicGatsbySimulation


if [ $# -gt 0 ] && [ "$1" == "-f" ]
then
  echo "Forcing an SBT stage..."
  sbt stage
  shift
  SBT_STAGE_RESULT=$?
else 
  SBT_STAGE_RESULT=0
fi

if [ $SBT_STAGE_RESULT -eq 0 ]
then
  echo "Now launching Gatling..."
  ./bin/gatling.sh "$@"
fi
