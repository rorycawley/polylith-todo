(ns com.rory.todo.interface
  (:require [clojure.string :as str]
            [com.rory.store.interface :as store]))

(defn add-todo [s title]
  (when (str/blank? title)
    (throw (Exception. "Title cannot be blank")))
  (store/add-todo s title))

(defn complete-todo [s id]
  (store/complete-todo s id))

(defn list-todos [s]
  (store/list-todos s))

(defn delete-todo [s id]
  (store/delete-todo s id))
