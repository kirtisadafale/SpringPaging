# Makefile for common developer tasks
.PHONY: build docker-build docker-up docker-down test

BUILD_IMAGE_NAME = paging:local

build:
	@echo "Building project (maven)..."
	./mvnw.cmd -DskipTests clean package

docker-build:
	@echo "Building Docker image..."
	docker build -t $(BUILD_IMAGE_NAME) .

docker-up:
	@echo "Starting docker-compose stack..."
	docker compose up -d --build

docker-down:
	@echo "Stopping docker-compose stack..."
	docker compose down

test:
	@echo "Run unit tests"
	./mvnw.cmd test
