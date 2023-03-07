# Container image that runs your code
FROM babashka/babashka:1.2.175-SNAPSHOT-alpine

# Copies your code file from your action repository to the filesystem path `/` of the container
COPY link_for_one_branch.bb /link_for_one_branch.bb

# Code file to execute when the docker container starts up (`entrypoint.sh`)
ENTRYPOINT ["bb" "/link_for_one_branch"]
