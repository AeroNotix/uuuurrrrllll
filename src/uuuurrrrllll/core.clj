(ns uuuurrrrllll.core
  (:require [clojurewerkz.welle.buckets :as wb]
            [clojurewerkz.welle.core :as wc]
            [clojurewerkz.welle.kv :as kv]
            [compojure.core :refer [defroutes routes GET POST]]
            [hiccup.util :refer [escape-html]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [uuuurrrrllll.riak :refer [coalesce-entries
                                       get-entry
                                       add-entry!]])
  (:use [hiccup.core]))


(defn handle-post [request]
  (if (map? (:body request))
    (let [body    (:body request)
          url     (body "url")
          channel (body "channel")
          nick    (body "nick")
          body    {:url     url
                   :channel channel
                   :nick    nick}]
      (if (every? (complement nil?) [url channel nick])
        {:status 201 :body {:short (add-entry! body)}}
        {:status 400}))
    {:status 400}))

(defn handle-get [request]
  (if-let [url (-> request
                   (get-in [:params :url])
                   (get-entry)
                   :url)]
    {:status 301 :headers {"location" url}}
    {:status 404}))

(defn list-all [request]
  (wc/connect!)
  {:status 200
   :body (html [:body
                [:ul
                 (for [[channel urls] (coalesce-entries)]
                   [:li channel
                    [:ul (for [url urls]
                           (let [v (escape-html url)]
                             [:li [:a {:href v} v]]))]])]])})

(defroutes app
  (POST "/" request
        handle-post)
  (GET "/list/" request
       list-all)
  (GET "/:url/" request
       handle-get))

(def wrapp
  (-> app
      wrap-json-body
      wrap-json-response
      wrap-stacktrace))

(defn -main [& args]
  (wc/connect!)
  (jetty/run-jetty wrapp {:port 8080}))
