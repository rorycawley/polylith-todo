(ns com.rory.todo.interface
  (:require [clojure.string :as str]))

(defn add-todo [title]
  (when (clojure.string/blank? title)
    (throw (Exception. "Title cannot be blank")))
  {:title title
   :status :pending})
