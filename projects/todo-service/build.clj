(ns build
  (:require [clojure.tools.build.api :as b]))

(def class-dir "target/classes")
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn uber [_]
  (b/delete {:path "target"})
  (b/copy-dir {:src-dirs   ["../../bases/todo-api/src"
                             "../../bases/todo-api/resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis     @basis
                  :ns-compile '[com.rory.todo-api.core]
                  :class-dir  class-dir})
  (b/uber {:class-dir class-dir
           :uber-file "target/app.jar"
           :basis     @basis
           :main      'com.rory.todo-api.core}))
