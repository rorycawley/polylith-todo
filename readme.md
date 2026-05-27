# polylith-todo

A todo REST API built with [Polylith](https://polylith.gitbook.io/polylith) and Clojure.

## Architecture

```
bases/
  todo-api/          HTTP layer — Ring/Reitit routes, entry point
components/
  todo/              Business logic — validation, delegates to store interface
  store-memory/      In-memory store implementation (atom-backed, for dev/test)
  store-postgres/    PostgreSQL store implementation (next.jdbc)
projects/
  todo-service/      Production uberjar — wires todo-api + todo + store-postgres
```

## API

| Method | Path            | Description              |
|--------|-----------------|--------------------------|
| GET    | /api/todos      | List all todos           |
| POST   | /api/todos      | Create a todo `{title}`  |
| PUT    | /api/todos/:id  | Mark a todo as done      |
| DELETE | /api/todos/:id  | Delete a todo            |

## Running locally

**Prerequisites:** [mise](https://mise.jdx.dev) and [Babashka](https://babashka.org)

```sh
bb install          # install tool versions via mise
bb test             # run tests (in-memory store)
bb test:postgres    # run tests (postgres via testcontainers)
bb up               # build and start the full stack with Docker
bb down             # stop and remove containers
bb clean            # remove build artefacts
```

After `bb up` the API is available at `http://localhost:8080/api/todos`.

## Configuration

The app reads config via [Aero](https://github.com/juxt/aero) from `config.edn`:

| Env var        | Default | Description                  |
|----------------|---------|------------------------------|
| `DATABASE_URL` | —       | JDBC URL; omit for mem store |
| `PORT`         | `8080`  | HTTP port                    |

## Polylith docs

- [High-level documentation](https://polylith.gitbook.io/polylith)
- [poly tool documentation](https://cljdoc.org/d/polylith/clj-poly/CURRENT)
