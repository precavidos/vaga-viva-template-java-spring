# Missão `pagination`: listagem paginada de consultas

Liste as consultas de um profissional com paginação e ordenação por data.

Testes: `src/test/java/io/github/precavidos/clinic/missions/PaginationTest.java`. Para rodar só esta missão: `./mvnw test -Dgroups=pagination`.

## Endpoint

`GET /professionals/{id}/appointments?page=0&size=20&sort=start,asc`

| Parâmetro | Significado | Padrão |
| --- | --- | --- |
| `page` | número da página, começando em 0 | `0` |
| `size` | itens por página | `20` |
| `sort` | `start,asc` ou `start,desc` | `start,asc` (da consulta mais antiga para a mais recente) |

Só entram as consultas do profissional `{id}`, lidas da tabela `appointments`.

## Formato da resposta

`200 OK`:

```json
{
  "content": [
    {"id": 3, "professionalId": 1, "patientId": 7, "start": "2030-03-04T09:00:00", "end": "2030-03-04T09:30:00", "durationMinutes": 30}
  ],
  "page": {"size": 20, "number": 0, "totalElements": 1, "totalPages": 1}
}
```

Um profissional sem consultas recebe `200 OK` com `content` vazio, `totalElements` 0 e `totalPages` 0.
