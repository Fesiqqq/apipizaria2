package com.fernando.apipizaria2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.LinkedHashMap;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.media.IntegerSchema;
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String description = """
                <div align="center">
                  <h2>🍕 Bella Roza Enterprise API</h2>
                  <p><i>Sistema corporativo robusto para gerenciamento avançado de franquias de pizzaria.</i></p>
                  
                  ![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)
                  ![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
                  ![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
                  <br><br>
                  [![GitHub](https://img.shields.io/badge/GitHub-Fesiqqq-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Fesiqqq)
                </div>
                
                ---
                
                ### 🚀 Visão Geral
                Esta documentação oficial descreve o contrato de integração da plataforma Bella Roza. Projetada sob arquitetura estritamente RESTful e com as melhores práticas de mercado, a API fornece alta previsibilidade, validações complexas e controle rigoroso de acesso e tráfego.
                
                ### ⚙️ Quadro de Recursos de Integração
                
                | Recurso Técnico | Descrição da Implementação | Header / Query Esperado |
                | :--- | :--- | :--- |
                | 🔐 **Autenticação** | Proteção global de endpoints baseada em chave estática. | `X-API-Key` |
                | 🛡️ **Idempotência** | Prevenção ativa contra duplicidade acidental de transações (POST). | `X-Idempotency-Key` |
                | 🚦 **Rate Limiting** | Controle de tráfego de borda limitando a 20 requisições por minuto. | Responde `Retry-After` |
                | 🔀 **Versionamento** | Roteamento dinâmico sem alteração de URL para quebra de contratos. | `X-API-Version` |
                | 📑 **Paginação** | Suporte Pageable (Spring Data) com navegação via HATEOAS. | `?page=0&size=10` |
                
                ---
                
                ### 💻 Exemplo de Consumo Corporativo (cURL)
                
                Para integrar corretamente com a API, certifique-se de passar os cabeçalhos de segurança. Abaixo um exemplo de chamada GET básica:
                
                ```bash
                curl -X 'GET' \\
                  'http://localhost:8080/api/produtos' \\
                  -H 'accept: application/json' \\
                  -H 'X-API-Key: api-pizzaria-secret-key-272' \\
                  -H 'X-API-Version: 1'
                ```
                """;

        return new OpenAPI()
                .info(new Info()
                        .title("API Bella Roza Pizzaria 🍕")
                        .version("v1.0.0")
                        .description(description))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("ApiKeyAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("ApiKeyAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY)
                                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("Insira a chave da API (Ex: api-pizzaria-secret-key-272)")));
    }

    /**
     * Remove os schemas internos do HATEOAS (Links, Link, RepresentationModel) da
     * documentação do Swagger, mantendo as respostas reais da API intactas com os _links.
     */
    @Bean
    public OpenApiCustomizer removeHateoasSchemasCustomizer() {
        List<String> hateoasSchemas = List.of(
                "Link", "Links", "RepresentationModel",
                "PagedModel", "CollectionModel", "EntityModel"
        );

        return openApi -> {
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                hateoasSchemas.forEach(schema -> openApi.getComponents().getSchemas().remove(schema));

                openApi.getComponents().getSchemas().entrySet()
                        .removeIf(entry -> {
                            String name = entry.getKey();
                            return name.equals("Links") || name.equals("Link")
                                    || name.startsWith("Representation")
                                    || (name.endsWith("Model") && (name.contains("Paged")
                                        || name.contains("Collection")
                                        || name.contains("Entity")));
                        });
            }
        };
    }


    @Bean
    public org.springdoc.core.customizers.OperationCustomizer addRateLimitResponse() {
        return (operation, handlerMethod) -> {
            // Check if this operation handles POST requests
            boolean isPost = handlerMethod.getMethod().isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class);
            if (!isPost) {
                org.springframework.web.bind.annotation.RequestMapping requestMapping =
                    handlerMethod.getMethod().getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
                if (requestMapping != null) {
                    for (org.springframework.web.bind.annotation.RequestMethod method : requestMapping.method()) {
                        if (method == org.springframework.web.bind.annotation.RequestMethod.POST) {
                            isPost = true;
                            break;
                        }
                    }
                }
            }
            if (isPost) {
                // Ensure responses map exists
                if (operation.getResponses() == null) {
                    operation.setResponses(new io.swagger.v3.oas.models.responses.ApiResponses());
                }
                // Define 429 response
                io.swagger.v3.oas.models.responses.ApiResponse response429 = new io.swagger.v3.oas.models.responses.ApiResponse()
                    .description("Too Many Requests – rate limit exceeded");
                // Add Retry-After header definition
                response429.addHeaderObject("Retry-After",
                    new io.swagger.v3.oas.models.headers.Header()
                        .description("Seconds to wait before retrying the request")
                        .schema(new io.swagger.v3.oas.models.media.IntegerSchema().example(30)));
                operation.getResponses().put("429", response429);
            }
            return operation;
        };
    }

    @Bean
    public org.springdoc.core.customizers.OperationCustomizer addIdempotencyKeyHeader() {
        return (operation, handlerMethod) -> {
            // Determine if the endpoint is a POST
            boolean isPost = handlerMethod.getMethod().isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class);
            if (!isPost) {
                org.springframework.web.bind.annotation.RequestMapping requestMapping = 
                    handlerMethod.getMethod().getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
                if (requestMapping != null) {
                    for (org.springframework.web.bind.annotation.RequestMethod method : requestMapping.method()) {
                        if (method == org.springframework.web.bind.annotation.RequestMethod.POST) {
                            isPost = true;
                            break;
                        }
                    }
                }
            }
            if (isPost) {
                if (operation.getParameters() == null) {
                    operation.setParameters(new java.util.ArrayList<>());
                }
                operation.addParametersItem(new io.swagger.v3.oas.models.parameters.Parameter()
                        .in("header")
                        .name("X-Idempotency-Key")
                        .description("Chave de idempotência obrigatória para requisições POST")
                        .required(true)
                        .schema(new io.swagger.v3.oas.models.media.StringSchema().example("idemp-" + java.util.UUID.randomUUID().toString())));
            }
            return operation;
        };
    }
}
