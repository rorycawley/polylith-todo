(ns com.rory.todo.interface-test
  (:require [clojure.test :as test :refer :all]
            [com.rory.todo.interface :as todo]))

(deftest add-todo-with-valid-title
  (testing "Given a valid title, when I add it, it appears as pending"
    (let [result (todo/add-todo "Buy milk")]
      (is (= "Buy milk" (:title result)))
      (is (= :pending (:status result))))))

(deftest add-todo-with-blank-title
  (testing "Given a blank title, when I add it, it is rejected with an error"
    (is (thrown? Exception (todo/add-todo "")))))

(deftest complete-a-pending-todo
  (testing "Given a pending todo, when I complete it, its status becomes done"
    (let [todo   (todo/add-todo "Buy milk")
          result (todo/complete-todo todo)]
      (is (= :done (:status result))))))

(deftest completing-a-done-todo-is-idempotent
  (testing "Given a done todo, when I complete it again, nothing changes"
    (let [todo   (todo/add-todo "Buy milk")
          result (-> todo
                     todo/complete-todo
                     todo/complete-todo)]
      (is (= :done (:status result))))))

(deftest list-todos-when-empty
  (testing "Given no todos, when I list them, I get an empty list"
    (is (= [] (todo/list-todos [])))))

(deftest list-todos-returns-all-regardless-of-status
  (testing "Given some todos, when I list them, I see all of them regardless of status"
    (let [todo1  (todo/add-todo "Buy milk")
          todo2  (-> (todo/add-todo "Walk dog") todo/complete-todo)
          result (todo/list-todos [todo1 todo2])]
      (is (= 2 (count result)))
      (is (some #(= "Buy milk" (:title %)) result))
      (is (some #(= "Walk dog" (:title %)) result)))))
