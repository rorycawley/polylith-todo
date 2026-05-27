# REPL Development

## Starting a REPL

```bash
clj -A:dev:+default
```

Loads `development/src` (dev namespaces) + all components (`store-memory`, `todo`) + `todo-api` base.

Then in the REPL:

```clojure
(require '[dev.server] :reload)
(in-ns 'dev.server)
(start!)       ; port 3000
(start! 8080)  ; custom port
```

## Using Components Directly

```clojure
(require '[com.rory.todo.interface :as todo]
         '[com.rory.store.interface :as store])

(def s (store/create-store))

(todo/add-todo s "Buy milk")
(todo/list-todos s)

(def item (first (todo/list-todos s)))
(todo/complete-todo s (:id item))
(todo/delete-todo s (:id item))
```

## Hot Reload / Restart

```clojure
(stop!)
; edit source files
(require '[dev.server] :reload)
(start!)
```

## Notes

- `dev/server.clj` lives at `development/src/dev/server.clj`
- `:+default` uses the in-memory store (atom); swap for `:+postgres` + Docker for Postgres
- Always require component `interface` namespaces, never internal namespaces
