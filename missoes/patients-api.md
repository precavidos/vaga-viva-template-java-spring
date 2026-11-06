# Missão `patients-api`: cadastro de pacientes

Crie a API REST para cadastrar, buscar, alterar e remover pacientes.

Testes: `src/test/java/io/github/precavidos/clinic/missions/PatientsApiTest.java`. Para rodar só esta missão: `./mvnw test -Dgroups=patients-api`.

## Paciente

| Campo | Tipo JSON | Exemplo |
| --- | --- | --- |
| `id` | número, gerado pela API | `1` |
| `name` | texto | `"Paciente Teste"` |
| `email` | texto | `"paciente@example.com"` |
| `phone` | texto | `"+55 11 90000-0000"` |
| `birthDate` | texto, data ISO (`AAAA-MM-DD`) | `"1990-05-17"` |

## Endpoints

| Método e caminho | Corpo da requisição | Resposta de sucesso | Outras respostas |
| --- | --- | --- | --- |
| `POST /patients` | paciente sem `id` | `201 Created`, cabeçalho `Location: .../patients/{id}` e o paciente com `id` | |
| `GET /patients/{id}` | | `200 OK` e o paciente | `404 Not Found` se o paciente não existe |
| `PUT /patients/{id}` | paciente completo, sem `id` | `200 OK` e o paciente alterado | `404 Not Found` se o paciente não existe |
| `DELETE /patients/{id}` | | `204 No Content` | `404 Not Found` se o paciente não existe |

As requisições e respostas usam `Content-Type: application/json`.

Os testes desta missão só enviam dados válidos. A validação é assunto da missão `input-validation`.
