(ns uuuurrrrllll.core
  (:require [clojurewerkz.welle.core :as wc]
            [compojure.core :refer [defroutes routes GET POST]]
            [hiccup.util :refer [escape-html]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [uuuurrrrllll.riak :refer [get-all-entries
                                       get-entry
                                       add-entry!]]
            [uuuurrrrllll.cassandra :as cass]
            [uuuurrrrllll.util :refer [coalesce-entries]])
  (:use [clojurewerkz.cassaforte.client :as client]
        [clojurewerkz.cassaforte.cql]
        [hiccup.core]))


(defmacro with-map-or-400 [request & body]
  `(if (map? (:body ~request))
     ~@body
    {:status 400}))

(defn handle-text [request]
  (with-map-or-400 request
    (let [body (:body request)
          text (body "text")]
      (if text
        {:status 201 :body {:url (cass/add-text! text)}}
        {:status 400}))))

(defn handle-post [request]
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

(defn handle-text-get [request]
  (if-let [message (-> request
                     (get-in [:params :code])
                     (cass/get-text-entry)
                     :message)]
    {:status 200 :body message}
    nil))

(defn handle-get [request]
  (if-let [url (-> request
                   (get-in [:params :url])
                   (cass/get-entry)
                   :url)]
    {:status 301 :headers {"location" url}}
    ;; returning nil invokes the jetty 404 handler.
    nil))

(defroutes app
  (POST "/t/" request
    handle-text)
  (POST "/" request
        handle-post)
  (GET "/t/:code/" request
       handle-text-get)
  (GET "/:url/" request
       handle-get))

(def wrapp
  (-> app
      wrap-json-body
      wrap-json-response
      wrap-stacktrace))

(defn -main [& args]
  (wc/connect!)
  (client/connect! ["localhost"])
  (use-keyspace "uuuurrrrllll")
  (jetty/run-jetty wrapp {:port 8080}))
