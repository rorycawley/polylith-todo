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

(deftest list-todos-returns-all-todos
  (testing "Given some todos in the store, when I list them, I see all of them"
    (store/add-todo *db* "Walk dog")
    (store/add-todo *db* "Buy bread")
    (is (>= (count (store/list-todos *db*)) 2))))

(deftest complete-todo-sets-status-to-done
  (testing "Given a pending todo, when I complete it, its status becomes done"
    (let [todo (store/add-todo *db* "Read book")]
      (store/complete-todo *db* (:id todo))
      (let [result (first (filter #(= (:id todo) (:id %))
                                  (store/list-todos *db*)))]
        (is (= :done (:status result)))))))

(deftest delete-todo-removes-it-from-store
  (testing "Given an existing todo, when I delete it, it no longer appears"
    (let [todo   (store/add-todo *db* "Clean house")
          before (count (store/list-todos *db*))]
      (store/delete-todo *db* (:id todo))
      (is (= (dec before) (count (store/list-todos *db*)))))))
