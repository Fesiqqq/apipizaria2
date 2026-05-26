<div align="center">
  <h1>🍕 API Bella Roza Pizzaria</h1>
  <p><i>Projeto Final - Curso de Desenvolvimento de APIs com Spring Boot</i></p>

</div>

---

## 🚀 Sobre o Projeto
Esta é uma API RESTful completa desenvolvida como projeto final de avaliação. O domínio escolhido foi um sistema robusto de gerenciamento para uma Pizzaria (Bella Roza), englobando todo o fluxo de retaguarda: desde o cadastro de clientes e seus endereços, até o cardápio (produtos e ingredientes) e o processamento de pedidos.

O projeto aplica estritamente os padrões REST, boas práticas da indústria e requisitos avançados de segurança e escalabilidade.

## 🛠️ Tecnologias e Arquitetura
- **Java 17** & **Spring Boot 3**
- **Spring Data JPA** com banco de dados em memória **H2**
- **Bean Validation** para integridade de dados (DTOs)
- **Spring HATEOAS** para navegabilidade
- **Springdoc OpenAPI (Swagger)** para documentação viva
- **Bucket4j** para controle de tráfego de rede

## 🏗️ Modelagem do Domínio (6 Entidades)
A arquitetura contempla relacionamentos complexos resolvidos via JPA:
- **`Cliente`** (1:1 com Endereço / 1:N com Pedido)
- **`Endereco`** (1:1 com Cliente)
- **`Pedido`** (1:N com ItemPedido)
- **`ItemPedido`** (N:1 com Produto)
- **`Produto`** (N:N com Ingrediente)
- **`Ingrediente`**
- **`ApiKey`** (Entidade de gerenciamento de segurança)

## 💎 Recursos Avançados Implementados

| Recurso | Como foi implementado | Header de Teste |
| :--- | :--- | :--- |
| **Segurança por Chave de API** | Proteção global via Filtro customizado (`ApiKeyFilter`). Apenas requisições com chave válida no banco são autorizadas. | `X-API-Key` |
| **Geração de Chaves Dinâmicas** | Endpoint público para gerar novas chaves (UUIDs) atreladas a um dono no banco de dados. | N/A |
| **Idempotência** | Previne duplicidade de criação de Pedidos e Clientes em caso de falha de rede do cliente, garantindo que o processamento ocorra apenas 1 vez. | `X-Idempotency-Key` |
| **Rate Limiting (Bucket4j)** | Proteção contra ataques de negação de serviço (DDoS) ou *spam*. Limite rígido (Intervally) de **20 requisições por minuto**. | Retorna `429 Too Many Requests` |
| **Versionamento Dinâmico** | Quebra de contrato controlada por versão de cabeçalho (V1 e V2) no mesmo mapeamento de URI para Produtos. | `X-API-Version` |
| **Paginação & HATEOAS** | Todas as listas (`GET`) são paginadas. Todo retorno JSON contém links dinâmicos (`_links`) para o próprio recurso ou recursos pai. | `?page=0&size=10` |
| **Tratamento Global de Erros** | Uso de `@ControllerAdvice` para interceptar validações e exceções (ex: `ResourceNotFound`), padronizando o JSON de resposta (Problema/Detalhes/Status HTTP). | N/A |

---

## ⚙️ Como Executar

**3. Acesse a Documentação (Swagger):**
Abra o seu navegador e acesse:
👉 **https://api-pizzaria-tgsy.onrender.com/swagger-ui/index.html#**

### 🔐 Como se autenticar no Swagger:
Como a API é blindada, você deve usar o botão verde **"Authorize"** no topo do Swagger para injetar a chave de testes:
> **Chave Mestre:** `api-pizzaria-secret-key-272`

---
<p align="center">
  <i>Desenvolvido com dedicação para a formação em APIs com Spring Boot. 🚀</i>
</p>
