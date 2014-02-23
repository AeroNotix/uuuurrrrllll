(ns uuuurrrrllll.core
  (:require [compojure.core :refer [defroutes routes GET POST]]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [ring.adapter.jetty :as jetty]
            [clojure.pprint :as pp]))


(def url-map (atom {}))

(def char-seq (doall
               (for [c (range 65 91)]
                 (char c))))

(defn gen-short-url [n]
  (clojure.string/join
   (take n (repeatedly #(rand-nth char-seq)))))

(defn add-url! [url]
  (let [short-url (gen-short-url 10)]
    (swap! url-map assoc short-url url)
    short-url))

(defn handle-post [request]
  (if-let [url (get-in request [:body "url"])]
    {:status 201 :body {:short (add-url! url)}}
    {:status 400}))

(defn handle-get [request]
  (if-let [url (get-in request [:params :url])]
    {:status 301 :headers {"location" (@url-map url)}}
    {:status 404}))

(defroutes app
  (POST "/" request
        handle-post)
  (GET "/:url/" request
       handle-get))

(def wrapp
  (-> app
      wrap-json-body
      wrap-json-response))

(defn -main [& args]
  (jetty/run-jetty wrapp {:port 8080}))
