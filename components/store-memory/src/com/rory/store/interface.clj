(ns com.rory.store.interface)

(defn create-store []
  (atom []))

(defn add-todo [store title]
  (let [todo {:id     (random-uuid)
              :title  title
              :status :pending}]
    (swap! store conj todo)
    todo))
