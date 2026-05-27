(ns com.rory.store.interface
  (:require [clojure.string :as str]))

(defn create-store
  ([] (atom []))
  ([db-spec] (atom [])))

(defn add-todo [store title]
  (when (str/blank? title)
    (throw (Exception. "Title cannot be blank")))
  (let [todo {:id     (random-uuid)
              :title  title
              :status :pending}]
    (swap! store conj todo)
    todo))

(defn list-todos [store]
  @store)

(defn complete-todo [store id]
  (swap! store (fn [todos]
                 (map #(if (= id (:id %))
                         (assoc % :status :done)
                         %)
                      todos))))

(defn delete-todo [store id]
  (swap! store (fn [todos]
                 (remove #(= id (:id %)) todos))))
