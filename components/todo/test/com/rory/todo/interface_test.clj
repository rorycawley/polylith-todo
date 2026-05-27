(ns com.rory.todo.interface-test
  (:require [clojure.test :as test :refer :all]
            [com.rory.todo.interface :as todo]))

(deftest add-todo-with-valid-title
  (testing "Given a valid title, when I add it, it appears as pending"
    (let [result (todo/add-todo "Buy milk")]
      (is (= "Buy milk" (:title result)))
      (is (= :pending (:status result))))))
