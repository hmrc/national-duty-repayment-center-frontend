#!/usr/bin/env bash

sbt clean scalafmtCheckAll coverage test coverageReport
