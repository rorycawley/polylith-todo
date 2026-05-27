(ns com.rory.store.interface)

(defn create-store []
  (atom []))

(defn add-todo [store title]
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
