# Red Team Report — V3

## Resumo das Vulnerabilidades Encontradas

### ATAQUE 01 — SQL Injection
- **Payload:** `joao' OR '1'='1`
- **Resultado:** BLOQUEADO
- **Motivo:** Spring Data JPA usa prepared statements automaticamente
- **Risco:** BAIXO
- **Correção v4:** Nenhuma necessária, JPA já protege

---

### ATAQUE 02 — Impersonation
- **Payload:** senderId=1 sem token ou senha
- **Resultado:** SUCESSO — mensagem enviada como outro usuário
- **Motivo:** WebSocket sem autenticação
- **Risco:** CRÍTICO
- **Correção v4:** JWT + autenticação no WebSocket

---

### ATAQUE 03 — Enumeração de Usuários
- **Payload:** GET /api/users/list e GET /api/users/{id}
- **Resultado:** SUCESSO — lista todos os usuários sem autenticação
- **Motivo:** Endpoints públicos sem proteção
- **Risco:** ALTO
- **Correção v4:** Spring Security protegendo os endpoints

---

### ATAQUE 04 — Flood no WebSocket
- **Payload:** 500 requisições simultâneas
- **Resultado:** Servidor sobrecarregado sem rate limit
- **Motivo:** Sem limitação de requisições
- **Risco:** ALTO
- **Correção v4:** Rate limit nas requisições

---

## Resumo Geral

| Ataque | Resultado | Risco |
|---|---|---|
| SQL Injection | Bloqueado | Baixo |
| Impersonation | Vulnerável | Crítico |
| Enumeração de usuários | Vulnerável | Alto |
| Flood WebSocket | Vulnerável | Alto |

## Conclusão
O sistema na v2 possui vulnerabilidades críticas de autenticação.
Todas serão corrigidas na v4 com Spring Security e JWT.