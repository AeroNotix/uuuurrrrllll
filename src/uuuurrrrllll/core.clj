(ns uuuurrrrllll.core
  (:require [clojurewerkz.welle.buckets :as wb]
            [clojurewerkz.welle.core :as wc]
            [clojurewerkz.welle.kv :as kv]
            [compojure.core :refer [defroutes routes GET POST]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [hiccup.util :refer [escape-html]])
  (:use [hiccup.core]))


(def bucket "links")

(def char-seq (doall
               (for [c (range 65 91)]
                 (char c))))

(defn gen-short-url [n]
  (->> #(rand-nth char-seq)
       repeatedly
       (take n)
       clojure.string/join))

(defn add-entry! [body]
  (let [short-url (gen-short-url 10)]
    (kv/store bucket short-url body
              :content-type "application/json"
              :indexes {:all #{"all"}})
    short-url))

(defn read-entry [entry]
  (-> entry
      :result
      :value))

(defn get-entry [short]
  (read-entry (kv/fetch-one bucket short)))

(defn merge-urls [grouped-urls]
  (for [[k v] grouped-urls]
    [k (map :url v)]))

(defn get-all-entries []
  (let [keys (wb/keys-in bucket)]
    (->> "all"
         (kv/index-query bucket :all)
         (pmap get-entry)
         (group-by :channel)
         (merge-urls))))

(defn handle-post [request]
  (let [body    (:body request)
        url     (body "url")
        channel (body "channel")
        nick    (body "nick")
        body    {:url     url
                 :channel channel
                 :nick    nick}]
    (if (every? (complement nil?) [url channel nick])
      {:status 201 :body {:short (add-entry! body)}}
      {:status 400})))

(defn handle-get [request]
  (if-let [url (-> request
                   (get-in [:params :url])
                   (get-entry)
                   :url)]
    {:status 301 :headers {"location" url}}
    {:status 404}))

(defn list-all [request]
  {:status 200
   :body (html [:body
                [:ul
                 (for [[channel urls] (get-all-entries)]
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
      wrap-json-response))

(defn -main [& args]
  (wc/connect!)
  (if-not (contains? (wb/list) bucket)
    (wb/create bucket))
  (jetty/run-jetty wrapp {:port 8080}))
