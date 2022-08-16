build:
	./gradlew clean build
start:
	APP_ENV=development ./gradlew run
report:
	./gradlew jacocoTestReport
.PHONY: build