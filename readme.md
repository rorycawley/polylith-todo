# polylith-todo

A todo REST API built as a [Polylith](https://polylith.gitbook.io/polylith) monorepo in Clojure. The codebase demonstrates how Polylith separates business logic, infrastructure, and deployment concerns into independently-testable bricks that are composed into deployable projects.

## Architecture

### Polylith concepts

Polylith organises code into three kinds of artefact:

| Kind | Role |
|------|------|
| **Component** | A library-like unit with a public `interface` namespace and an optional implementation. Components depend only on other components' interfaces, never on their internals. |
| **Base** | An entry-point brick — the outermost layer that wires components together and exposes them to the outside world (HTTP, CLI, queue consumer, etc.). |
| **Project** | A deployable artefact that selects which components and bases to include. The same component can appear in many projects. |

### Workspace layout

```
polylith-todo/
├── bases/
│   └── todo-api/          Ring/Reitit HTTP server — routes, request parsing,
│                          error mapping, and the -main entry point
├── components/
│   ├── todo/              Business-logic layer — validates input, enforces
│   │                      the todo state machine, delegates to store
│   ├── store-memory/      Store implementation backed by an in-memory atom
│   └── store-postgres/    Store implementation backed by PostgreSQL (next.jdbc)
├── projects/
│   └── todo-service/      Production uberjar: todo-api + todo + store-postgres
└── development/           REPL / dev classpath root (no production code here)
```

### Overview

```
  HTTP client
      │  ▲
      │  │
      ▼  │
  ┌─── base ─────────────────────────────────────────────────────────────────┐
  │  todo-api                                                                │
  │                                                                          │
  │  Jetty ──► Reitit router ──► Muuntaja ──► route handler                │
  │                                                                          │
  │  GET /api/todos  ·  POST /api/todos                                     │
  │  PUT /api/todos/:id  ·  DELETE /api/todos/:id                          │
  └───────────────────────────────────┬─────────────────────────────────────┘
                                      │
  ┌─── component ─────────────────────▼─────────────────────────────────────┐
  │  todo                                                                    │
  │                                                                          │
  │  add-todo  (validates blank title)                                       │
  │  complete-todo  (idempotent)  ·  list-todos  ·  delete-todo             │
  └───────────────────────────────────┬─────────────────────────────────────┘
                                      │
                        com.rory.store/interface
                                      │
                        ┌─────────────┴─────────────┐
                        │                           │
                        ▼                           ▼
  ┌─── component ────────────────────┐  ┌─── component ────────────────────┐
  │  store-memory                    │  │  store-postgres                  │
  │                                  │  │                                  │
  │  (atom [])                       │  │  next.jdbc ──► PostgreSQL 16    │
  │                                  │  │                                  │
  │  profile: :+default              │  │  profile: :+postgres             │
  │  dev · unit tests                │  │  int. tests · production         │
  └──────────────────────────────────┘  └──────────────────────────────────┘
```

`todo-api` only knows about `todo`'s interface. `todo` only knows about `store`'s interface. Neither knows which store implementation is wired in — that decision is made by the Polylith profile at the point of composition.

### The store interface

Both `store-memory` and `store-postgres` live in the same Clojure namespace (`com.rory.store.interface`) and expose identical function signatures:

```clojure
(create-store db-spec)  ; → store handle
(add-todo     store title)
(list-todos   store)
(complete-todo store id)
(delete-todo  store id)
```

Swapping implementations is a classpath concern, not a code change. The `:+default` profile loads `store-memory`; the `:+postgres` profile loads `store-postgres`. The production project hardwires `store-postgres`.

## Technology stack

| Concern | Library |
|---------|---------|
| HTTP server | [Ring](https://github.com/ring-clojure/ring) + Jetty adapter |
| Routing | [Reitit](https://github.com/metosin/reitit) |
| Content negotiation / JSON | [Muuntaja](https://github.com/metosin/muuntaja) |
| Database access | [next.jdbc](https://github.com/seancorfield/next-jdbc) |
| Database | PostgreSQL 16 |
| Configuration | [Aero](https://github.com/juxt/aero) |
| Build / uberjar | [tools.build](https://github.com/clojure/tools.build) |
| Integration-test database | [Testcontainers](https://testcontainers.com) |
| Task runner | [Babashka](https://babashka.org) |
| Tool version manager | [mise](https://mise.jdx.dev) |
| Monorepo tooling | [Polylith](https://polylith.gitbook.io/polylith) |

## API

| Method | Path | Body / params | Success | Error |
|--------|------|---------------|---------|-------|
| `GET` | `/api/todos` | — | `200` list of todos | — |
| `POST` | `/api/todos` | `{"title": "…"}` | `201` created todo | `400` blank title |
| `PUT` | `/api/todos/:id` | — | `200` `{"ok":true}` | `400` bad UUID · `404` not found |
| `DELETE` | `/api/todos/:id` | — | `204` no content | `400` bad UUID · `404` not found |

Todo shape:

```json
{ "id": "uuid", "title": "Buy milk", "status": "pending" }
```

`status` is either `"pending"` or `"done"`. Completing a `done` todo is idempotent.

## Development setup

**Prerequisites:** [Babashka](https://babashka.org) and [mise](https://mise.jdx.dev) must be installed before anything else.

```sh
bb install   # install Clojure, Java, and poly via mise
bb all       # clean → down → test (memory) → test (postgres)
bb up        # build uberjar and start the full Docker stack
bb down      # stop and remove containers + volumes
bb clean     # remove build artefacts
```

`bb all` is the canonical CI command. It tears down any running stack, then runs both test suites from a known-clean state.

After `bb up` the API is available at `http://localhost:8080/api/todos`.

### REPL

The VS Code / Calva config in `.vscode/settings.json` connects with aliases `[:dev :test :+default]`, which puts the memory store, `todo`, and `todo-api` on the classpath alongside all dependencies.

### Polylith CLI

```sh
poly info        # workspace overview — bricks, projects, profiles
poly check       # lint interfaces for consistency
poly test :dev   # run tests for the development project (memory store)
poly test :dev +postgres   # run tests with the postgres store
```

## Testing strategy

Tests are split across three bricks and two Polylith profiles:

| Profile | Bricks tested | Store | Isolation |
|---------|--------------|-------|-----------|
| `:+default` | `store-memory`, `todo`, `todo-api` | In-memory atom | Each test creates a fresh atom |
| `:+postgres` | `store-postgres` | PostgreSQL via Testcontainers | Container started once per run; table truncated before each test |

The `todo` and `todo-api` tests run only in the `:+default` profile. They test business logic and HTTP routing — concerns that do not vary by store. The `:+postgres` profile tests what only postgres can test: SQL correctness, UUID handling, transaction isolation, and not-found semantics against a real database engine.

Testcontainers pulls a `postgres:16` image, starts a throwaway container, and stops it when the test run completes. The database is always brand new on each `bb test:postgres` invocation.

## Configuration

The app reads config at startup via [Aero](https://github.com/juxt/aero) from `bases/todo-api/resources/config.edn`:

```edn
{:database-url #or [#env DATABASE_URL nil]
 :port         #or [#env PORT "8080"]}
```

| Variable | Default | Notes |
|----------|---------|-------|
| `DATABASE_URL` | `nil` | JDBC URL (`jdbc:postgresql://…`). When absent the server falls back to the in-memory store — useful for local smoke-testing without Docker. |
| `PORT` | `8080` | TCP port the Jetty server binds to. |

## Rancher Desktop users

One-time setup required for `bb test:postgres` (testcontainers needs to find the Docker socket):

```sh
# ~/.docker-java.properties
api.version=1.44

# ~/.testcontainers.properties
docker.host=unix:///Users/$USER/.rd/docker.sock
ryuk.disabled=true
checks.disable=true
```
