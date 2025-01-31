DATE_SETUP='2019-04-19 14:10:00'

PORT=''            # port the jetty server will listen to, very often 1999
LOG_LEVEL=''       # the logging level of the root logger. From DEBUG to ERROR
LOG_CONFIG_FILE='' # logback configuration, very often /logback-prod.xml or /logback.xml

BRANCH=''          # the branch to be deployed on this server, e.g. 'develop'
# COMMIT=''        # if a commit instead of a branch is deployed, e.g. '174db1b4f0'
GIT_REPO=''        # the git repo where the branch is founf, e.g. 'openroberta-lab'
GIT_UPTODATE=false # set to true ONLY, if you have a well-prepared, ready to use branch checked out. NEVER use the GIT_REPO for other server deployments. NEVER. See _gen.sh:30ff
# START_ARGS='-d key1=val1 -d key2=val2' # use to supply parameter when container is started
