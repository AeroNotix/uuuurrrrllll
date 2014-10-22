(ns uuuurrrrllll.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [uuuurrrrllll.cassandra :as cass])
  (:use [clojurewerkz.cassaforte.client :as client]
        [clojurewerkz.cassaforte.cql]))


(defmacro with-map-or-400 [request & body]
  `(if (map? (:body ~request))
     ~@body
     {:status 400}))

(defn insert-message [request]
  (with-map-or-400 request
    (let [body    (:body request)
          url     (body "url")
          channel (body "channel")
          nick    (body "nick")
          body    {:url     url
                   :channel channel
                   :nick    nick}]
      (if (every? (complement nil?) [url channel nick])
        {:status 201 :body {:short (cass/add-entry! body)}}
        {:status 400}))))

(defn retrieve-message [request]
  (if-let [url (-> request
                 (get-in [:params :url])
                 (cass/get-entry)
                 :url)]
    {:status 301 :headers {"location" url}}
    nil))

(defroutes app
  (POST "/" request
    insert-message)
  (GET "/:url/" request
    retrieve-message))

(def wrapp
  (-> app
    wrap-json-body
    wrap-json-response
    wrap-stacktrace))

(defn -main [& args]
  (client/connect ["localhost"])
  (use-keyspace "uuuurrrrllll")
  (jetty/run-jetty wrapp {:port 8080}))
