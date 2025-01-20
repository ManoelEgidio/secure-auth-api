# Sistema de Autenticação Avançado

Este sistema de autenticação foi desenvolvido com base na observação de problemas comuns em soluções ensinadas na maioria dos cursos. A ideia era criar algo que **combinasse segurança, performance e simplicidade**, sem recorrer à autenticação por sessão, que traz limitações de escalabilidade e complexidade na manutenção.

A principal inovação está no uso de **três tokens distintos (Access, Refresh e ID)**, gerenciados automaticamente pelo servidor, sem necessidade de intervenções diretas do front-end para renovação de tokens.

---

## 🔑 A Estrutura dos Três Tokens

A divisão clara de responsabilidades entre os tokens torna o sistema seguro e eficiente:

1. **Access Token**:
    - Usado para autenticar requisições à API.
    - É armazenado como um cookie `HttpOnly` e `Secure`, protegido contra acessos JavaScript e ataques XSS.
    - Tem tempo de vida curto para minimizar riscos em caso de comprometimento.

2. **Refresh Token**:
    - Usado exclusivamente pelo servidor para renovar o Access Token quando ele expira.
    - Também é armazenado como um cookie `HttpOnly` e `Secure`.
    - Não contém informações sensíveis além do login do usuário.

3. **ID Token**:
    - Contém informações úteis para o front-end, como nome, ID, e permissões.
    - É armazenado como um cookie apenas `Secure`, permitindo que o front-end o utilize para exibir dados do usuário.
    - **Importante**: Não pode ser usado para autenticar requisições à API.

---

## ⚙️ Por Que Não Sessões?

Autenticação por sessão pode ser problemática porque:

- **Escalabilidade**: Sessões exigem que o servidor armazene o estado do usuário, o que complica sistemas distribuídos.
- **Performance**: Verificar e manter sessões pode aumentar a latência em sistemas de alta demanda.
- **Manutenção**: Lidar com tempo de vida e limpeza de sessões adiciona complexidade.

Com a abordagem baseada em tokens, a autenticação é **stateless**. O servidor valida os tokens recebidos, reduzindo a dependência de armazenamento e aumentando a performance.

---

## 🔒 Segurança Incorporada no Sistema

1. **Proteção Contra Ataques**:
    - Tokens Access e Refresh são configurados como `HttpOnly` e `Secure`, protegendo contra acessos indevidos via JavaScript.
    - O ID Token, mesmo acessível ao front-end, não pode ser usado para chamadas à API.

2. **Validação de Tokens**:
    - Tokens são assinados com RSA (chaves pública e privada), garantindo autenticidade e proteção contra falsificação.
    - O sistema verifica claims específicas no token, como `role` e `authorities`, antes de autorizar operações.

3. **CORS e Headers**:
    - Configuração de CORS controla origens permitidas.
    - Headers como `SameSite=Lax` evitam que os cookies sejam enviados automaticamente em requisições de terceiros.

---

## 📂 Como o Sistema Funciona

### 1. Login
- O usuário envia suas credenciais para o endpoint `/auth/login`.
- Após validação, são gerados os três tokens:
    - **Access Token** e **Refresh Token** são armazenados como cookies protegidos.
    - **ID Token** é acessível ao front-end, mas sem comprometer a segurança.
- Esses cookies são automaticamente enviados pelo navegador em requisições subsequentes.

### 2. Validação e Renovação
- Para cada requisição protegida, o filtro `SecurityFilter` verifica:
    - Se o **Access Token** é válido: O usuário é autenticado diretamente.
    - Se o **Access Token** expirou, mas o **Refresh Token** ainda é válido: O servidor gera novos tokens (Access, Refresh e ID) e os envia como cookies atualizados.
- **Sem necessidade de ação do front-end** para renovação de tokens.

### 3. Logout
- Todos os cookies são removidos, encerrando a autenticação do usuário.

### 4. Proteção de Endpoints
- Endpoints sensíveis requerem autenticação com Access Token.
- Endpoints públicos (como `/auth/login` e `/auth/register`) são configurados como acessíveis sem autenticação.

---

## 📋 Integração com Swagger

O sistema conta com uma configuração robusta no Swagger para facilitar o desenvolvimento e integração. A documentação:

- **Inclui endpoints como**:
    - `POST /auth/login`: Realiza autenticação e gera os tokens.
    - `POST /auth/logout`: Encerra a sessão removendo os cookies.
    - `GET /users`: Lista usuários com filtros dinâmicos.
    - `POST /users`: Cria novos usuários.
    - `PUT /users/{id}`: Atualiza informações do usuário.
    - `DELETE /users/{id}`: Exclui um usuário.

- **URL do Swagger**:
  [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🚀 Tecnologias Utilizadas

- **Spring Boot**: Framework principal para desenvolvimento do back-end.
- **JWT (JSON Web Token)**: Para autenticação stateless.
- **RSA**: Para assinatura e validação de tokens.
- **Swagger/OpenAPI**: Para documentação e teste de APIs.
- **Lombok**: Para redução de boilerplate no código.
- **PostgreSQL**: Banco de dados principal.

---

## 🛡️ Benefícios do Sistema

1. **Desempenho Elevado**:
    - A validação de tokens é rápida e **stateless**, ideal para sistemas modernos.

2. **Segurança Avançada**:
    - A divisão clara entre Access, Refresh e ID Tokens reduz significativamente a superfície de ataque.

3. **Usabilidade Melhorada**:
    - O ID Token fornece ao front-end as informações necessárias sem expor tokens críticos.
    - A renovação automática pelo servidor mantém a experiência do usuário fluida.

4. **Fácil Integração e Escalabilidade**:
    - O uso de JWT permite fácil integração com sistemas distribuídos e escaláveis.

---

## 🛠️ Como Executar

### Pré-requisitos
- **Java 21**
- **Maven**
- **PostgreSQL** configurado e rodando