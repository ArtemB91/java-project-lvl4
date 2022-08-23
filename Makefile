build:
	./gradlew clean build
start:
	APP_ENV=development ./gradlew run
install:
	./gradlew install
start-dist:
	APP_ENV=development ./build/install/app/bin/app
report:
	./gradlew jacocoTestReport
.PHONY: build