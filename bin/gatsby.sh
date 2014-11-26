#!/bin/sh

# This script, if invoked with a -f argument first ensures that the latest Gatsby code has been compiled and built into a JAR
# on the classpath, before then calling gatling.sh.

# This is useful when developing new Gatsby features and wanting to test them out with real user-tests.

# To run non-interactively (i.e. skip prompting of the simulation to be run) use:

# <path-to-bin-dir>/gatsby.sh [-f] -s <simulation.class>

# For example, if invoking from the Gatsby project root: 
# ./bin/gatsby.sh -f -s gatsbyexamples.BasicGatsbySimulation

INITIAL_DIR=`pwd`
BIN_DIR=$(dirname $0)
cd ${BIN_DIR}/.." && PROJECT_ROOT=`pwd`

if [ $# -gt 0 ] && [ "$1" == "-f" ]
then
  echo "Forcing an SBT stage..."
  cd $PROJECT_ROOT
  sbt stage
  shift
  SBT_STAGE_RESULT=$?
else 
  SBT_STAGE_RESULT=0
fi

cd $INITIAL_DIR

if [ $SBT_STAGE_RESULT -eq 0 ]
then
  echo "Now launching Gatling..."
  $BIN_DIR/gatling.sh "$@"
fi
