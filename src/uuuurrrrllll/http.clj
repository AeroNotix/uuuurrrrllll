(ns uuuurrrrllll.http
  (:require [compojure.core :refer [routes GET POST]]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]))


(defmacro with-map-or-400 [request & body]
  `(if (map? (:body ~request))
     ~@body
     {:status 400}))

(defn insert-message [request]
  (with-map-or-400 request
    (let [db      (::db request)
          body    (:body request)
          url     (body "url")
          channel (body "channel")
          nick    (body "nick")
          body    {:url     url
                   :channel channel
                   :nick    nick}]
      (if (every? (complement nil?) [url channel nick])
        {:status 201 :body {:short (.add-entry! db body)}}
        {:status 400}))))

(defn retrieve-message [request]
  (let [db (::db request)]
    (if-let [url (:url
                  (.get-entry db
                    (get-in request
                      [:params :url])))]
      {:status 301 :headers {"location" url}}
      nil)))

(defn wrap-database [f database]
  (fn [req]
    (f (assoc req ::db database))))

(defrecord HTTPServer [server database]
  component/Lifecycle
  (start [http-server]
    (let [app (-> (routes
                    (POST "/" request
                      insert-message)
                    (GET "/:url/" request
                      retrieve-message))
                (wrap-database database)
                wrap-json-body
                wrap-json-response
                wrap-stacktrace)]
      (assoc http-server :server
             (jetty/run-jetty app {:port 8080 :join? false}))))
  (stop [http-server]
    (.stop (:server http-server))))
