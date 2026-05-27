(ns dev.server
  (:require [ring.adapter.jetty :as jetty]
            [com.rory.todo-api.core :as core]))

(defonce server (atom nil))

(defn start!
  ([] (start! 3000))
  ([port]
   (when @server (.stop @server))
   (let [s       (core/make-store {})
         handler (core/app s)]
     (reset! server (jetty/run-jetty handler {:port port :join? false}))
     (println (str "Server started on port " port)))))

(defn stop! []
  (when @server
    (.stop @server)
    (reset! server nil)
    (println "Server stopped")))
