# Sistema de Autenticação Avançado - Microserviço

Este microserviço de autenticação foi desenvolvido com base na observação de problemas comuns em soluções ensinadas na maioria dos cursos. A ideia era criar algo que combinasse segurança, performance e simplicidade, sem recorrer à autenticação por sessão, que traz limitações de escalabilidade e complexidade na manutenção.

A principal inovação está na utilização de **três tokens distintos (Access, Refresh e ID)**, gerenciados automaticamente pelo servidor. Essa abordagem elimina a necessidade de intervenções diretas do front-end para a renovação de tokens, garantindo uma experiência fluida para o usuário.

---

## 🔑 Estrutura de Tokens

O microserviço divide claramente as responsabilidades entre os tokens, garantindo segurança e eficiência. **Os três tokens são interdependentes, sendo necessários para que todo o sistema funcione perfeitamente**:

1. **Access Token**  
   - Usado para autenticar requisições à API.  
   - Configurado como um cookie `HttpOnly` e `Secure`, protegido contra ataques XSS.  
   - Possui um tempo de vida curto para mitigar riscos em caso de comprometimento.  

2. **Refresh Token**  
   - Usado exclusivamente pelo servidor para renovar o Access Token quando ele expira.  
   - Configurado como um cookie `HttpOnly` e `Secure`.  
   - Não contém informações sensíveis além do identificador do usuário.  

3. **ID Token**  
   - Contém informações úteis para o front-end, como nome, ID, permissões, **roles** e **authorities**.  
   - Usado no processo de renovação dos tokens, tornando indispensável sua presença junto aos demais.  
   - Armazenado como um cookie `Secure`, acessível ao front-end para exibir dados do usuário.  
   - **Importante**: Não pode ser usado para autenticar requisições à API.  

---

## ⚙️ Por que Serverless?

A arquitetura **serverless** é ideal para este sistema por vários motivos:

- **Redução de Custos**:  
  A abordagem **stateless** elimina a necessidade de armazenamento de estado no servidor, reduzindo significativamente os custos operacionais.  

- **Escalabilidade Automática**:  
  Serviços serverless escalam automaticamente com base na demanda, garantindo performance consistente mesmo em picos de uso.  

- **Manutenção Simplificada**:  
  Tokens são gerenciados pelo cliente e validados pelo servidor, eliminando a complexidade de manutenção de sessões no servidor.  

- **Performance Otimizada**:  
  A validação de tokens JWT é rápida e leve, ideal para sistemas distribuídos.  

---

## 🔒 Segurança e Autorização

1. **Proteção Contra Ataques**  
   - Tokens **Access** e **Refresh** são configurados como `HttpOnly` e `Secure`, protegendo contra acessos indevidos via JavaScript.  
   - O **ID Token**, acessível ao front-end, é projetado para exibir informações sem comprometer a segurança do sistema.  

2. **Validação de Tokens**  
   - Tokens são assinados com **RSA** (chaves pública e privada), garantindo autenticidade e proteção contra falsificação.  
   - O sistema valida claims específicas, como `role` e `authorities`, antes de autorizar operações.  

3. **Autorização por Níveis de Acesso**  
   - O sistema implementa controle granular com base em **roles** (perfis de usuário) e **authorities** (permissões específicas).  
   - Cada endpoint pode ser configurado para exigir um nível de acesso específico.  

4. **CORS e Headers**  
   - Configuração de CORS controla origens permitidas.  
   - Cookies utilizam o atributo `SameSite=Lax` para evitar envio automático em requisições de terceiros.  

---

## 📂 Fluxo de Funcionamento

### 1. Login
- O usuário envia credenciais para o endpoint `/auth/login`.  
- Após validação, o microserviço gera os três tokens:  
  - **Access Token** e **Refresh Token** são armazenados como cookies protegidos.  
  - **ID Token** é acessível ao front-end.  
- Cookies são enviados automaticamente pelo navegador em requisições subsequentes.  

### 2. Validação e Renovação
- O filtro de segurança verifica:  
  - Se o **Access Token** é válido: o usuário é autenticado diretamente.  
  - Se o **Access Token** expirou, mas o **Refresh Token** e o **ID Token** ainda são válidos: o servidor gera novos tokens (Access, Refresh e ID) e os envia como cookies atualizados.  

### 3. Logout
- Todos os cookies são removidos, encerrando a autenticação do usuário.  

### 4. Proteção de Endpoints
- Endpoints sensíveis exigem autenticação com **Access Token** e verificações de **roles** e **authorities** para autorização.  
- Endpoints públicos, como `/auth/login`, são configurados para acesso sem autenticação.  

---

## 📋 Integração com Swagger

A documentação da API é fornecida com **Swagger**, facilitando o desenvolvimento e integração.  

- **Endpoints disponíveis**:
  - `POST /auth/login`: Realiza autenticação e gera tokens.  
  - `POST /auth/logout`: Remove os cookies e encerra a sessão.  
  - `GET /users`: Lista usuários com filtros dinâmicos.  
  - `POST /users`: Cria novos usuários.  
  - `PUT /users/{id}`: Atualiza informações de um usuário.  
  - `DELETE /users/{id}`: Exclui um usuário.  

- **URL do Swagger**:  
  [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)  

---

## 🚀 Tecnologias Utilizadas

- **Spring Boot**: Framework principal do microserviço.  
- **JWT (JSON Web Token)**: Para autenticação **stateless**.  
- **RSA**: Para assinatura e validação de tokens.  
- **Swagger/OpenAPI**: Documentação e teste de APIs.  
- **Lombok**: Para redução de boilerplate no código.  
- **PostgreSQL**: Banco de dados principal.  

---

## 🛡️ Benefícios do Sistema

1. **Desempenho Elevado**  
   A validação de tokens é rápida e **stateless**, ideal para sistemas modernos.  

2. **Segurança Avançada**  
   A separação entre **Access**, **Refresh** e **ID Tokens** reduz significativamente a superfície de ataque.  

3. **Controle Granular de Acesso**  
   - Permite a definição de níveis de acesso com base em **roles** e **authorities**.  
   - Protege endpoints sensíveis com verificações específicas.  

4. **Escalabilidade Simples e Econômica**  
   A arquitetura **serverless** e o uso de JWT reduzem custos operacionais e permitem que o sistema cresça sem complexidade adicional.  

5. **Experiência do Usuário Melhorada**  
   - O **ID Token** fornece ao front-end as informações necessárias sem comprometer a segurança.  
   - A renovação automática pelo servidor mantém a experiência do usuário fluida.  
