(ns com.rory.todo-api.core
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [com.rory.todo.interface :as todo]
            [com.rory.store.interface :as store])
  (:gen-class))

(defn read-config []
  (aero/read-config (io/resource "config.edn")))

(defn make-store [{:keys [database-url]}]
  (if database-url
    (store/create-store {:jdbcUrl database-url})
    (store/create-store)))

(defn routes [s]
  ["/api"
   ["/todos"
    {:get  (fn [_]
             {:status 200
              :body   (vec (todo/list-todos s))})
     :post (fn [req]
             (let [title (get-in req [:body-params :title])]
               (try
                 {:status 201
                  :body   (todo/add-todo s title)}
                 (catch Exception e
                   {:status 400
                    :body   {:error (.getMessage e)}}))))}]
   ["/todos/:id"
    {:put    (fn [req]
               (try
                 (let [id (parse-uuid (get-in req [:path-params :id]))]
                   (todo/complete-todo s id)
                   {:status 200 :body {:ok true}})
                 (catch IllegalArgumentException _
                   {:status 400 :body {:error "Invalid id"}})
                 (catch Exception e
                   {:status 404 :body {:error (.getMessage e)}})))
     :delete (fn [req]
               (try
                 (let [id (parse-uuid (get-in req [:path-params :id]))]
                   (todo/delete-todo s id)
                   {:status 204})
                 (catch IllegalArgumentException _
                   {:status 400 :body {:error "Invalid id"}})
                 (catch Exception e
                   {:status 404 :body {:error (.getMessage e)}})))}]])

(defn app [s]
  (ring/ring-handler
   (ring/router
    (routes s)
    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}})))

(defn -main [& _]
  (let [config (read-config)
        s      (make-store config)
        port   (Integer/parseInt (:port config))]
    (println (str "Starting server on port " port))
    (jetty/run-jetty (app s) {:port port :join? true})))
