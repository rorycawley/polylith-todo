(ns com.rory.store.interface)

(defn create-store
  ([] (atom []))
  ([db-spec] (atom [])))

(defn add-todo [store title]
  (let [todo {:id     (random-uuid)
              :title  title
              :status :pending}]
    (swap! store conj todo)
    todo))

(defn list-todos [store]
  (vec @store))

(defn complete-todo [store id]
  (when (not-any? #(= id (:id %)) @store)
    (throw (ex-info (str "Todo not found: " id) {:id id})))
  (swap! store (fn [todos]
                 (mapv #(if (= id (:id %))
                          (assoc % :status :done)
                          %)
                       todos))))

(defn delete-todo [store id]
  (when (not-any? #(= id (:id %)) @store)
    (throw (ex-info (str "Todo not found: " id) {:id id})))
  (swap! store (fn [todos]
                 (filterv #(not= id (:id %)) todos))))
