(ns com.rory.store.interface-test
  (:require [clojure.test :refer :all]
            [com.rory.store.interface :as store]))

(deftest add-todo-persists-to-store
  (testing "Given a valid title, when I add it, it appears in the store as pending"
    (let [s      (store/create-store)
          result (store/add-todo s "Buy milk")]
      (is (= "Buy milk" (:title result)))
      (is (= :pending (:status result)))
      (is (some? (:id result))))))


(deftest list-todos-returns-all-todos
  (testing "Given some todos in the store, when I list them, I see all of them"
    (let [s (store/create-store)]
      (store/add-todo s "Buy milk")
      (store/add-todo s "Walk dog")
      (is (= 2 (count (store/list-todos s)))))))

(deftest complete-todo-sets-status-to-done
  (testing "Given a pending todo in the store, when I complete it, its status becomes done"
    (let [s    (store/create-store)
          todo (store/add-todo s "Buy milk")]
      (store/complete-todo s (:id todo))
      (is (= :done (:status (first (store/list-todos s))))))))

(deftest delete-todo-removes-it-from-store
  (testing "Given an existing todo in the store, when I delete it, it no longer appears"
    (let [s    (store/create-store)
          todo (store/add-todo s "Buy milk")]
      (store/delete-todo s (:id todo))
      (is (= 0 (count (store/list-todos s)))))))
