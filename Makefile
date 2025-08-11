# Makefile para Microservicios
# Reemplaza run-services.ps1, stop-services.ps1, run-tests.sh, run-services.sh, build-services.ps1

# Variables
PRODUCTOS_SERVICE_DIR = productos-service
INVENTARIO_SERVICE_DIR = inventario-service
DOCKER_COMPOSE_FILE = docker-compose.yml

# Comandos principales
.PHONY: help run stop test build clean docker-run docker-stop

# Comando por defecto
help:
	@echo "=== Makefile para Microservicios ==="
	@echo "Comandos disponibles:"
	@echo "  make run        - Ejecutar servicios localmente"
	@echo "  make docker-run - Ejecutar servicios con Docker"
	@echo "  make stop       - Detener servicios"
	@echo "  make docker-stop- Detener servicios de Docker"
	@echo "  make test       - Ejecutar todas las pruebas"
	@echo "  make test-productos - Ejecutar pruebas de productos"
	@echo "  make test-inventario - Ejecutar pruebas de inventario"
	@echo "  make build      - Compilar servicios"
	@echo "  make clean      - Limpiar archivos generados"

	@echo "  make postman    - Abrir colección de Postman"
	@echo "  make help       - Mostrar esta ayuda"

# Ejecutar servicios localmente
run:
	@echo "Iniciando servicios localmente..."
	@echo "Asegurate de tener Java 17+ y Maven instalados"
	@echo "Compilando proyecto..."
	@mvn clean install -DskipTests=true -q
	@echo "Iniciando Productos Service..."
	@cd $(PRODUCTOS_SERVICE_DIR) && mvn spring-boot:run -DskipTests=true &
	@echo "Iniciando Inventario Service..."
	@cd $(INVENTARIO_SERVICE_DIR) && mvn spring-boot:run -DskipTests=true &
	@echo "Servicios iniciados localmente"
	@echo "Productos Service: http://localhost:8081"
	@echo "Inventario Service: http://localhost:8082"
	@echo "Swagger UI Productos: http://localhost:8081/swagger-ui.html"
	@echo "Swagger UI Inventario: http://localhost:8082/swagger-ui.html"
	@echo "Para detener los servicios, ejecuta: make stop"

# Ejecutar servicios con Docker
docker-run:
	@echo "Iniciando servicios con Docker..."
	@echo "Ejecutando servicios con Docker..."
	@docker-compose -f $(DOCKER_COMPOSE_FILE) up -d
	@echo "Servicios iniciados con Docker"
	@echo "Productos Service: http://localhost:8081"
	@echo "Inventario Service: http://localhost:8082"
	@echo "Health checks:"
	@echo "   curl http://localhost:8081/actuator/health"
	@echo "   curl http://localhost:8082/actuator/health"

# Detener servicios
stop:
	@echo "Deteniendo servicios..."
	@echo "Limpiando procesos de Maven..."
ifeq ($(OS),Windows_NT)
	@powershell -Command "Get-Process -Name 'java' -ErrorAction SilentlyContinue | Where-Object {$$_.ProcessName -eq 'java'} | Stop-Process -Force -ErrorAction SilentlyContinue"
else
	@pkill -f "spring-boot:run" || true
	@pkill -f "mvn.*spring-boot" || true
endif
	@echo "Servicios detenidos"

# Detener servicios de Docker
docker-stop:
	@echo "Deteniendo servicios de Docker..."
	@docker-compose -f $(DOCKER_COMPOSE_FILE) down
	@echo "Servicios de Docker detenidos"

# Ejecutar todas las pruebas
test: test-productos test-inventario
	@echo "=== Resumen de Cobertura ==="
	@echo "Los reportes de cobertura se encuentran en:"
	@echo "- $(PRODUCTOS_SERVICE_DIR)/target/site/jacoco/index.html"
	@echo "- $(INVENTARIO_SERVICE_DIR)/target/site/jacoco/index.html"
	@echo "=== Pruebas Completadas ==="

# Ejecutar pruebas del servicio de productos
test-productos:
	@echo "=== Ejecutando Pruebas del Microservicio de Productos ==="
	@echo "Ejecutando pruebas unitarias..."
	@cd $(PRODUCTOS_SERVICE_DIR) && mvn clean test
	@echo "Generando reporte de cobertura..."
	@cd $(PRODUCTOS_SERVICE_DIR) && mvn jacoco:report
	@echo "Reporte de cobertura generado en: $(PRODUCTOS_SERVICE_DIR)/target/site/jacoco/index.html"

# Ejecutar pruebas del servicio de inventario
test-inventario:
	@echo "=== Ejecutando Pruebas del Microservicio de Inventario ==="
	@echo "Ejecutando pruebas unitarias..."
	@cd $(INVENTARIO_SERVICE_DIR) && mvn clean test
	@echo "Generando reporte de cobertura..."
	@cd $(INVENTARIO_SERVICE_DIR) && mvn jacoco:report
	@echo "Reporte de cobertura generado en: $(INVENTARIO_SERVICE_DIR)/target/site/jacoco/index.html"

# Compilar servicios
build:
	@echo "Compilando servicios sin ejecutar pruebas..."
	@echo "Compilando productos-service..."
	@cd $(PRODUCTOS_SERVICE_DIR) && mvn -q -DskipTests clean package
	@echo "Compilando inventario-service..."
	@cd $(INVENTARIO_SERVICE_DIR) && mvn -q -DskipTests clean package
	@cd ..
	@echo "Compilacion completada exitosamente"
	@echo "JARs generados en:"
	@echo "   $(PRODUCTOS_SERVICE_DIR)/target/productos-service-*.jar"
	@echo "   $(INVENTARIO_SERVICE_DIR)/target/inventario-service-*.jar"

# Limpiar archivos generados
clean:
	@echo "Limpiando archivos generados..."
	@cd $(PRODUCTOS_SERVICE_DIR) && mvn clean
	@cd $(INVENTARIO_SERVICE_DIR) && mvn clean
	@cd ..
	@echo "Limpieza completada"



# Mostrar información de Postman
postman:
	@echo "=== Colección de Postman ==="
	@echo "Archivo: Microservicios-API.postman_collection.json"
	@echo "Variables: Microservicios-API.postman_environment.json"
	@echo ""
	@echo "Para usar la colección:"
	@echo "1. Abrir Postman"
	@echo "2. Importar: Microservicios-API.postman_collection.json"
	@echo "3. Importar: Microservicios-API.postman_environment.json"
	@echo "4. Seleccionar el environment 'Microservicios-API'"
	@echo "5. ¡Listo para probar los endpoints!"

# Comandos adicionales utiles
logs:
	@echo "Mostrando logs de Docker..."
	@docker-compose -f $(DOCKER_COMPOSE_FILE) logs -f

status:
	@echo "Estado de los servicios..."
	@docker-compose -f $(DOCKER_COMPOSE_FILE) ps

restart: stop run

docker-restart: docker-stop docker-run
