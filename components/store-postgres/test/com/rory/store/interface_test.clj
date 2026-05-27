(ns com.rory.store.interface-test
  (:require [clojure.test :refer :all]
            [com.rory.store.interface :as store])
  (:import [org.testcontainers.containers PostgreSQLContainer]))

(def ^:dynamic *db* nil)

(defn with-postgres [test-fn]
  (let [container (doto (PostgreSQLContainer. "postgres:16")
                    (.start))
        db        {:dbtype   "postgresql"
                   :host     (.getHost container)
                   :port     (.getMappedPort container 5432)
                   :dbname   (.getDatabaseName container)
                   :user     (.getUsername container)
                   :password (.getPassword container)}]
    (binding [*db* (store/create-store db)]
      (test-fn))
    (.stop container)))

(use-fixtures :once with-postgres)

(deftest add-todo-persists-to-postgres
  (testing "Given a valid title, when I add it, it appears in postgres as pending"
    (let [result (store/add-todo *db* "Buy milk")]
      (is (= "Buy milk" (:title result)))
      (is (= :pending (:status result)))
      (is (some? (:id result))))))
