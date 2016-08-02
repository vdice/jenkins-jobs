SHELLCHECK_CMD := shellcheck bash/scripts/*
BATS_CMD := bats --tap bash/tests
TEST_ENV_PREFIX := docker run --rm -v ${CURDIR}:/workdir -w /workdir quay.io/deis/shell-dev

test-style:
	${SHELLCHECK_CMD}

test-scripts:
	${BATS_CMD}

test-dsl:
	./gradlew test

test: test-style test-scripts test-dsl

docker-test-style:
	${TEST_ENV_PREFIX} ${SHELLCHECK_CMD}

docker-test-scripts:
	${TEST_ENV_PREFIX} ${BATS_CMD}

docker-test-dsl:
	( docker start gradle && docker logs -f --tail 0 gradle ) \
	|| docker run --name gradle -v ${CURDIR}:/workdir -w /workdir frekele/gradle:2.14.1-jdk8 ./gradlew test

docker-test: docker-test-style docker-test-scripts docker-test-dsl

open-test-results:
	open build/reports/tests/index.html

.PHONY: test-style test-scripts test-dsl test \
	docker-test-style docker-test-scripts docker-test-dsl docker-test
