#!/bin/bash

export PROJ_NAME=ecuacion-tool-code-generator/ecuacion-tool-code-generator-web

../../../ecuacion-internal-utils/ecuacion-util-bootstrap/script/command.sh `pwd`/custom.scss

mv ../../../ecuacion-internal-utils/ecuacion-util-bootstrap/script/bootstrap* .
cp bootstrap* ../../../${PROJ_NAME}/src/main/resources/static/css/
rm bootstrap*
