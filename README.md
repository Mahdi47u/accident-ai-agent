# Accident AI — Spring AI test project

A deliberately small Java 21 demo of an accident guidance chatbot using Spring Boot, Spring AI, OpenRouter tool calling, PostgreSQL, and pgvector.

## Included

- Browser chat at `http://localhost:8088`
- `POST /api/chat` with Spring AI tool calling
- `POST /api/knowledge/seed` to embed five reviewed guidance notes into pgvector
- Urgency-screening, vector-search, and road-weather tools
- Free OpenRouter chat and embedding models by default (rate limited)
- Docker Compose live-development stack

This is an educational demo, not a medical device. It does not diagnose injuries or replace emergency services.

## Run

1. Create a free OpenRouter account and API key, then create a local environment file:

   ```bash
   cp .env.example .env
   ```

2. Start the app and database:

   ```bash
   docker compose up
   ```

3. Open `http://localhost:8088`, click **Index guidance** once, then chat.

The default `openrouter/free` chat router and Liquid embedding model have zero token pricing, but OpenRouter still requires an API key and applies free-tier limits. Java source and static files are mounted into the Maven container; Spring Boot DevTools restarts the application after compiled changes. VS Code `Ctrl+Shift+B` starts the same stack.

## REST examples

```bash
curl http://localhost:8088/api/health
curl -X POST http://localhost:8088/api/knowledge/seed
curl -X POST http://localhost:8088/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"A person fell and is not responding. What should I do?"}'
```

## Tests

```bash
docker compose run --rm app mvn test
```

The API key stays server-side in `.env`. Never put it in JavaScript or commit it.
