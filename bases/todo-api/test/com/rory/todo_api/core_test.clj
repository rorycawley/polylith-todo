(ns com.rory.todo-api.core-test
  (:require [clojure.test :refer :all]
            [com.rory.todo-api.core :as core]
            [com.rory.store.interface :as store]))

(defn test-app []
  (core/app (store/create-store)))

(deftest list-todos-returns-200
  (testing "GET /api/todos returns 200"
    (let [resp ((test-app) {:request-method :get :uri "/api/todos"})]
      (is (= 200 (:status resp))))))

(deftest add-todo-with-valid-title-returns-201
  (testing "POST /api/todos with a valid title returns 201"
    (let [resp ((test-app) {:request-method :post
                            :uri            "/api/todos"
                            :body-params    {:title "Buy milk"}})]
      (is (= 201 (:status resp))))))

(deftest add-todo-with-blank-title-returns-400
  (testing "POST /api/todos with a blank title returns 400"
    (let [resp ((test-app) {:request-method :post
                            :uri            "/api/todos"
                            :body-params    {:title ""}})]
      (is (= 400 (:status resp))))))

(deftest complete-todo-returns-200
  (testing "PUT /api/todos/:id on an existing todo returns 200"
    (let [s    (store/create-store)
          todo (store/add-todo s "Buy milk")
          resp ((core/app s) {:request-method :put
                              :uri            (str "/api/todos/" (:id todo))})]
      (is (= 200 (:status resp))))))

(deftest complete-missing-todo-returns-404
  (testing "PUT /api/todos/:id on a missing todo returns 404"
    (let [resp ((test-app) {:request-method :put
                            :uri            (str "/api/todos/" (random-uuid))})]
      (is (= 404 (:status resp))))))

(deftest delete-todo-returns-204
  (testing "DELETE /api/todos/:id on an existing todo returns 204"
    (let [s    (store/create-store)
          todo (store/add-todo s "Buy milk")
          resp ((core/app s) {:request-method :delete
                              :uri            (str "/api/todos/" (:id todo))})]
      (is (= 204 (:status resp))))))

(deftest delete-missing-todo-returns-404
  (testing "DELETE /api/todos/:id on a missing todo returns 404"
    (let [resp ((test-app) {:request-method :delete
                            :uri            (str "/api/todos/" (random-uuid))})]
      (is (= 404 (:status resp))))))
