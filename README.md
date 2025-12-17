# 🛒 MS-Order - Microserviço de Gerenciamento de Pedidos

Sistema de gerenciamento de pedidos desenvolvido com Spring Boot, oferecendo APIs REST para criação e consulta de pedidos e produtos.

## 📋 Índice

- [Tecnologias](#-tecnologias)
- [Funcionalidades](#-funcionalidades)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Executando o Projeto](#-executando-o-projeto)
- [API Endpoints](#-api-endpoints)
  - [Produtos](#produtos)
  - [Pedidos](#pedidos)
- [Exemplos de Uso](#-exemplos-de-uso)
- [Cache](#-cache)
- [Validações](#-validações)
- [Tratamento de Erros](#-tratamento-de-erros)
- [Collection do Postman](#-collection-do-postman)
- [Swagger/OpenAPI](#-swaggeropenapi)

---

## 🚀 Tecnologias

- **Java 21**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **Spring Cache**
- **Spring Validation**
- **H2 In-Memory Database**
- **Lombok**
- **MapStruct 1.6.3** (mapeamento de DTOs)
- **SpringDoc OpenAPI 2.3.0** (documentação Swagger)
- **Log4j2** (logging)

---

## ✨ Funcionalidades

### Gerenciamento de Produtos
- ✅ Criar produtos
- ✅ Buscar produto por productId
- ✅ Listar todos os produtos (paginado)
- ✅ Validação de estoque
- ✅ Cache de produtos

### Gerenciamento de Pedidos
- ✅ Criar pedidos
- ✅ Buscar pedido por ID
- ✅ Buscar pedido por externalId
- ✅ Listar todos os pedidos (paginado)
- ✅ Filtrar pedidos por status
- ✅ Métricas de pedidos diários
- ✅ Atualizar status do pedido
- ✅ Validação de estoque ao criar pedido
- ✅ Processamento assíncrono
- ✅ Cache de pedidos

---

## 📦 Pré-requisitos

- **Java 21** ou superior
- **Maven 3.8+**
- **PostgreSQL 14+**
- **Postman** (opcional, para testes)

---

## 🔧 Instalação

### 1. Clone o repositório
```bash
git clone <repository-url>
cd ms-order
```

### 2. Configure o banco de dados PostgreSQL
```sql
CREATE DATABASE order_db;
CREATE USER order_user WITH PASSWORD 'order_password';
GRANT ALL PRIVILEGES ON DATABASE order_db TO order_user;
```

### 3. Configure as variáveis de ambiente (opcional)
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=order_db
export DB_USERNAME=order_user
export DB_PASSWORD=order_password
```

### 4. Compile o projeto
```bash
mvn clean install
```

---

## ⚙️ Configuração

O arquivo `application.yml` contém as configurações principais:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:orderdb
    username: sa
    password: password

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  cache:
    type: simple
```

---

## ▶️ Executando o Projeto

### Modo desenvolvimento
```bash
mvn spring-boot:run
```

### Executar JAR
```bash
mvn clean package
java -jar target/ms-order-0.0.1-SNAPSHOT.jar
```

A aplicação estará disponível em: `http://localhost:8080`

---

## 📡 API Endpoints

### Produtos

#### 1. Criar Produto
```http
POST /api/products
Content-Type: application/json

{
  "productId": "PROD-001",
  "productName": "Notebook Dell",
  "quantity": 10,
  "unitPrice": 3500.00
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "productId": "PROD-001",
  "productName": "Notebook Dell",
  "quantity": 10,
  "unitPrice": 3500.00
}
```

---

#### 2. Buscar Produto por ProductId
```http
GET /api/products/{productId}
```

**Exemplo:**
```http
GET /api/products/PROD-001
```

**Response (200 OK):**
```json
{
  "id": 1,
  "productId": "PROD-001",
  "productName": "Notebook Dell",
  "quantity": 10,
  "unitPrice": 3500.00
}
```

---

#### 3. Listar Todos os Produtos (Paginado)
```http
GET /api/products?page=0&size=10&sort=productName,asc
```

**Parâmetros:**
- `page`: Número da página (default: 0)
- `size`: Itens por página (default: 20)
- `sort`: Ordenação (ex: `productName,asc`, `unitPrice,desc`)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "productId": "PROD-001",
      "productName": "Notebook Dell",
      "quantity": 10,
      "unitPrice": 3500.00
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 25,
  "totalPages": 3
}
```

---

### Pedidos

#### 1. Criar Pedido
```http
POST /api/orders
Content-Type: application/json

{
  "externalId": "ORD-001",
  "customerId": "CUST-001",
  "items": [
    {
      "productId": "PROD-001",
      "quantity": 2
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "externalId": "ORD-001",
  "customerId": "CUST-001",
  "status": "CREATED",
  "totalAmount": 7000.00,
  "items": [
    {
      "productId": "PROD-001",
      "productName": "Notebook Dell",
      "quantity": 2,
      "unitPrice": 3500.00,
      "totalPrice": 7000.00
    }
  ],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

#### 2. Buscar Pedido por ID
```http
GET /api/orders/{id}
```

**Exemplo:**
```http
GET /api/orders/1
```

---

#### 3. Buscar Pedido por ExternalId
```http
GET /api/orders/external/{externalId}
```

**Exemplo:**
```http
GET /api/orders/external/ORD-001
```

---

#### 4. Listar Todos os Pedidos (Paginado)
```http
GET /api/orders?page=0&size=10&sort=createdAt,desc
```

---

#### 5. Filtrar Pedidos por Status
```http
GET /api/orders/status/{status}?page=0&size=10
```

**Status disponíveis:**
- `PROCESSING`
- `CREATED`
- `COMPLETED`
- `FAILED`

**Exemplo:**
```http
GET /api/orders/status/CREATED?page=0&size=10
```

---

#### 6. Obter Contagem de Pedidos de Hoje
```http
GET /api/orders/metrics/today
```

**Response (200 OK):**
```json
15
```

---

#### 7. Atualizar Status do Pedido
```http
PUT /api/orders/{id}/status/{status}
```

**Exemplo:**
```http
PUT /api/orders/1/status/COMPLETED
```

---

## 💡 Exemplos de Uso

### Fluxo Completo: Criar Produto e Pedido

#### Passo 1: Criar um produto
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "productName": "Notebook Dell",
    "quantity": 10,
    "unitPrice": 3500.00
  }'
```

#### Passo 2: Criar um pedido
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "ORD-001",
    "customerId": "CUST-001",
    "items": [
      {
        "productId": "PROD-001",
        "quantity": 2
      }
    ]
  }'
```

#### Passo 3: Consultar o pedido
```bash
curl http://localhost:8080/api/orders/external/ORD-001
```

---

## 🗄️ Cache

O sistema utiliza cache em memória (ConcurrentMapCache) para otimizar performance:

### Caches Configurados:
- **`orders`**: Cache de pedidos por ID
- **`ordersByExternalId`**: Cache de pedidos por externalId
- **`products`**: Cache de produtos por productId

### Comportamento:
- ✅ Primeira consulta: busca do banco de dados
- ✅ Consultas subsequentes: retorna do cache
- ✅ Atualização de status: limpa o cache automaticamente

---

## ✅ Validações

### Produto
- `productId`: obrigatório, não pode ser vazio
- `productName`: obrigatório, não pode ser vazio
- `quantity`: obrigatório, deve ser ≥ 0
- `unitPrice`: obrigatório, deve ser > 0.01
- Não permite produtos duplicados (mesmo `productId`)

### Pedido
- `externalId`: obrigatório, não pode ser vazio
- `customerId`: obrigatório, não pode ser vazio
- `items`: obrigatório, deve ter pelo menos 1 item
- Não permite pedidos duplicados (mesmo `externalId`)
- **Validação de estoque**: verifica se há quantidade suficiente do produto

---

## ⚠️ Tratamento de Erros

### Estoque Insuficiente (400 Bad Request)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Estoque insuficiente para o produto Notebook Dell. Disponível: 5, Solicitado: 10"
}
```

### Produto Não Encontrado (404 Not Found)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Produto não encontrado com productId: PROD-999"
}
```

### Pedido Duplicado (400 Bad Request)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Pedido com externalId ORD-001 já existe"
}
```

---

## 📮 Collection do Postman

Uma collection completa do Postman está disponível em:
```
postman/Order_API_Collection.postman_collection.json
```

### Como importar:
1. Abra o Postman
2. Clique em **Import**
3. Selecione o arquivo `Order_API_Collection.postman_collection.json`
4. A collection "Order API - Consultas" será importada

---

## 📚 Swagger/OpenAPI

A documentação interativa da API está disponível em:

```
http://localhost:8080/swagger-ui.html
```

Você pode testar todos os endpoints diretamente pela interface do Swagger.

---

## 🏗️ Arquitetura

```
ms-order/
├── src/main/java/br/com/order/
│   ├── config/          # Configurações (Cache, etc)
│   ├── controller/      # Controllers REST
│   ├── dto/             # Data Transfer Objects
│   │   ├── request/     # DTOs de entrada
│   │   └── response/    # DTOs de saída
│   ├── enums/           # Enumerações (OrderStatusEnum)
│   ├── exception/       # Exceções customizadas
│   ├── mapper/          # MapStruct mappers
│   ├── model/           # Entidades JPA
│   ├── repository/      # Repositórios Spring Data
│   └── service/         # Lógica de negócio
├── src/main/resources/
│   └── application.yml  # Configurações da aplicação
└── postman/             # Collections do Postman
```

---

## 🔄 Fluxo de Criação de Pedido

1. **Validação**: Verifica se o `externalId` já existe
2. **Busca de Produtos**: Para cada item, busca o produto (com cache)
3. **Validação de Estoque**: Verifica se há quantidade suficiente
4. **Atualização de Estoque**: Decrementa a quantidade do produto
5. **Cálculo de Valores**: Calcula `unitPrice` e `totalAmount`
6. **Criação do Pedido**: Salva o pedido com status `PROCESSING`
7. **Processamento Assíncrono**: Atualiza status para `CREATED` e notifica sistema externo
8. **Retorno**: Retorna o pedido criado

---

## 📊 Status do Pedido

| Status | Descrição |
|--------|-----------|
| `PROCESSING` | Pedido está sendo processado |
| `CREATED` | Pedido criado com sucesso |
| `COMPLETED` | Pedido concluído |
| `FAILED` | Pedido falhou no processamento |

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## 📝 Licença

Este projeto está sob a licença MIT.

---

## 👨‍💻 Autor

Desenvolvido com ❤️ usando Spring Boot