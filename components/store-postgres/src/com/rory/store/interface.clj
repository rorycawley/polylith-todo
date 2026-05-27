(ns com.rory.store.interface
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn create-store
  ([] (throw (ex-info "PostgreSQL store requires connection config — call (create-store db-spec)" {})))
  ([db-spec]
   (let [ds (jdbc/get-datasource db-spec)]
     (jdbc/execute! ds ["CREATE TABLE IF NOT EXISTS todos (
                          id     UUID PRIMARY KEY,
                          title  TEXT NOT NULL,
                          status TEXT NOT NULL)"])
     ds)))

(defn add-todo [store title]
  (let [id (random-uuid)]
    (jdbc/execute! store
                   ["INSERT INTO todos (id, title, status) VALUES (?, ?, ?)"
                    id title "pending"])
    {:id id :title title :status :pending}))

(defn list-todos [store]
  (mapv (fn [row]
          {:id     (:id row)
           :title  (:title row)
           :status (keyword (:status row))})
        (jdbc/execute! store ["SELECT * FROM todos"]
                       {:builder-fn rs/as-unqualified-maps})))

(defn complete-todo [store id]
  (let [result (jdbc/execute-one! store
                                  ["UPDATE todos SET status = 'done' WHERE id = ? RETURNING id" id]
                                  {:builder-fn rs/as-unqualified-maps})]
    (when-not result
      (throw (ex-info (str "Todo not found: " id) {:id id})))))

(defn delete-todo [store id]
  (let [result (jdbc/execute-one! store
                                  ["DELETE FROM todos WHERE id = ? RETURNING id" id]
                                  {:builder-fn rs/as-unqualified-maps})]
    (when-not result
      (throw (ex-info (str "Todo not found: " id) {:id id})))))
