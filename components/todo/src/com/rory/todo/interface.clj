(ns com.rory.todo.interface
  (:require [clojure.string :as str]))

(defn add-todo [title]
  (when (clojure.string/blank? title)
    (throw (Exception. "Title cannot be blank")))
  {:title title
   :status :pending})

(defn complete-todo [todo]
  (assoc todo :status :done))

(defn list-todos [todos]
  todos)


(defn delete-todo [id todos]
  (when (not-any? #(= id (:id %)) todos)
    (throw (Exception. (str "Todo not found: " id))))
  (remove #(= id (:id %)) todos))
