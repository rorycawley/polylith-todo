(ns com.rory.todo.interface-test
  (:require [clojure.test :refer :all]
            [com.rory.todo.interface :as todo]
            [com.rory.store.interface :as store]))

(defn make-store [] (store/create-store))

(deftest add-todo-with-valid-title
  (testing "Given a valid title, when I add it, it appears as pending"
    (let [s      (make-store)
          result (todo/add-todo s "Buy milk")]
      (is (= "Buy milk" (:title result)))
      (is (= :pending (:status result))))))

(deftest add-todo-with-blank-title
  (testing "Given a blank title, when I add it, it is rejected with an error"
    (is (thrown? Exception (todo/add-todo (make-store) "")))))

(deftest complete-a-pending-todo
  (testing "Given a pending todo, when I complete it, its status becomes done"
    (let [s    (make-store)
          todo (todo/add-todo s "Buy milk")]
      (todo/complete-todo s (:id todo))
      (is (= :done (:status (first (todo/list-todos s))))))))

(deftest completing-a-done-todo-is-idempotent
  (testing "Given a done todo, when I complete it again, nothing changes"
    (let [s    (make-store)
          todo (todo/add-todo s "Buy milk")]
      (todo/complete-todo s (:id todo))
      (todo/complete-todo s (:id todo))
      (is (= :done (:status (first (todo/list-todos s))))))))

(deftest list-todos-when-empty
  (testing "Given no todos, when I list them, I get an empty list"
    (is (= [] (todo/list-todos (make-store))))))

(deftest list-todos-returns-all-regardless-of-status
  (testing "Given some todos, when I list them, I see all of them"
    (let [s (make-store)]
      (todo/add-todo s "Buy milk")
      (todo/add-todo s "Walk dog")
      (is (= 2 (count (todo/list-todos s)))))))

(deftest delete-an-existing-todo
  (testing "Given an existing todo, when I delete it, it no longer appears"
    (let [s    (make-store)
          todo (todo/add-todo s "Buy milk")]
      (todo/delete-todo s (:id todo))
      (is (= 0 (count (todo/list-todos s)))))))

(deftest delete-a-non-existent-todo
  (testing "Given a non-existent todo id, when I delete it, it returns an error"
    (let [s (make-store)]
      (is (thrown? Exception (todo/delete-todo s (random-uuid)))))))
