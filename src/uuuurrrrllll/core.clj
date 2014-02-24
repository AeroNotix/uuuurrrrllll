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
  (clojure.string/join
   (take n (repeatedly #(rand-nth char-seq)))))

(defn add-url! [url]
  (let [short-url (gen-short-url 10)]
    (kv/store bucket short-url url)
    short-url))

(defn get-url [short]
  (if-let [value (-> (kv/fetch-one bucket short)
                     :result
                     :value)]
    (String. value)
    nil))

(defn handle-post [request]
  (if-let [url (get-in request [:body "url"])]
    {:status 201 :body {:short (add-url! url)}}
    {:status 400}))

(defn handle-get [request]
  (if-let [url (-> request
                   (get-in [:params :url])
                   (get-url))]
    {:status 301 :headers {"location" url}}
    {:status 404}))

(defn list-all [request]
  {:status 200
   :body (html [:body
                [:ul
                 (for [k (seq (wb/keys-in bucket))]
                   (let [v (escape-html (get-url k))]
                     [:li [:a {:href v} v]]))]])})

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
