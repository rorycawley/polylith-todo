(ns com.rory.store.interface-test
  (:require [clojure.test :refer :all]
            [com.rory.store.interface :as store]
            [next.jdbc :as jdbc])
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

(defn truncate-todos [f]
  (jdbc/execute! *db* ["TRUNCATE TABLE todos"])
  (f))

(use-fixtures :once with-postgres)
(use-fixtures :each truncate-todos)

(deftest add-todo-persists-to-postgres
  (testing "Given a valid title, when I add it, it appears in postgres as pending"
    (let [result (store/add-todo *db* "Buy milk")]
      (is (= "Buy milk" (:title result)))
      (is (= :pending (:status result)))
      (is (some? (:id result))))))

(deftest list-todos-returns-all-todos
  (testing "Given some todos in the store, when I list them, I see all of them"
    (store/add-todo *db* "Walk dog")
    (store/add-todo *db* "Buy bread")
    (is (= 2 (count (store/list-todos *db*))))))

(deftest complete-todo-sets-status-to-done
  (testing "Given a pending todo, when I complete it, its status becomes done"
    (let [todo (store/add-todo *db* "Read book")]
      (store/complete-todo *db* (:id todo))
      (let [result (first (filter #(= (:id todo) (:id %))
                                  (store/list-todos *db*)))]
        (is (= :done (:status result)))))))

(deftest complete-non-existent-todo-throws
  (testing "Given a non-existent todo id, when I complete it, it throws"
    (is (thrown? Exception (store/complete-todo *db* (random-uuid))))))

(deftest delete-todo-removes-it-from-store
  (testing "Given an existing todo, when I delete it, it no longer appears"
    (let [todo (store/add-todo *db* "Clean house")]
      (store/delete-todo *db* (:id todo))
      (is (zero? (count (filter #(= (:id todo) (:id %)) (store/list-todos *db*))))))))

(deftest delete-non-existent-todo-throws
  (testing "Given a non-existent todo id, when I delete it, it throws"
    (is (thrown? Exception (store/delete-todo *db* (random-uuid))))))
