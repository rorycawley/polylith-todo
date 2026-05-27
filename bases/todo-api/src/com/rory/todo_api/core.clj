(ns com.rory.todo-api.core
  (:require [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [com.rory.todo.interface :as todo]
            [com.rory.store.interface :as store])
  (:gen-class))

(defn make-store []
  (let [db-url (System/getenv "DATABASE_URL")]
    (if db-url
      (store/create-store {:jdbcUrl db-url})
      (throw (ex-info "DATABASE_URL environment variable is required" {})))))

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
               (let [id (parse-uuid (get-in req [:path-params :id]))]
                 (todo/complete-todo s id)
                 {:status 200
                  :body   {:ok true}}))
     :delete (fn [req]
               (let [id (parse-uuid (get-in req [:path-params :id]))]
                 (try
                   (todo/delete-todo s id)
                   {:status 200 :body {:ok true}}
                   (catch Exception e
                     {:status 404
                      :body {:error (.getMessage e)}}))))}]])

(defn app [s]
  (ring/ring-handler
   (ring/router
    (routes s)
    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}})))

(defn -main [& _]
  (let [s    (make-store)
        port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (println (str "Starting server on port " port))
    (jetty/run-jetty (app s) {:port port :join? true})))
