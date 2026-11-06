# Missão `api-security`: proteção da API

Exija autenticação e separe o que recepcionistas e profissionais podem fazer.

Testes: `src/test/java/io/github/precavidos/clinic/missions/ApiSecurityTest.java`. Para rodar só esta missão: `./mvnw test -Dgroups=api-security`.

## Usuários e papéis

A segurança lê os usuários da propriedade `clinic.security.users`, uma lista de itens com `username`, `password` (em texto puro) e `role`. Os papéis são `RECEPTIONIST` e `PROFESSIONAL`.

Os usuários dos testes estão em `src/test/resources/application-test.yaml` (perfil `test`). Use essa lista, sem alterá-la. Todos os testes do projeto se autenticam com esses usuários:

| Usuário | Senha | Papel |
| --- | --- | --- |
| `recepcao` | `recepcao-senha` | `RECEPTIONIST` |
| `dra.helena` | `profissional-senha` | `PROFESSIONAL` |

## Contrato

- Autenticação HTTP Basic (cabeçalho `Authorization: Basic ...`).
- Sem credenciais, com senha errada ou com usuário desconhecido: `401 Unauthorized`.
- `/actuator/health` continua público, sem credenciais.
- `RECEPTIONIST` pode usar toda a API.
- `PROFESSIONAL` só pode consultar (`GET`). Qualquer outro método, como `POST /patients`, `PUT /patients/{id}`, `DELETE /patients/{id}` ou `POST /appointments`, responde `403 Forbidden`.
- A API não guarda sessão: nenhuma resposta cria sessão nem envia cookie, e cada requisição se autentica de novo.
- Como não há sessão nem formulário de login, a proteção CSRF fica **desligada**. Sem isso, os `POST`, `PUT` e `DELETE` dos testes das outras missões passam a receber `403`.
