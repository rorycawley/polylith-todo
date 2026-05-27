(ns com.rory.todo.interface)

(defn add-todo [title]
  {:title title
   :status :pending})
