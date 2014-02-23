(ns uuuurrrrllll.core
  (:require [compojure.core :refer [defroutes routes GET POST]]
            [clojure.core.cache :as cache]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [ring.adapter.jetty :as jetty])
  (:use [hiccup.core]))


;; One day in ms.
(def one-day (* (* (* 60 60) 24) 1000))
(def url-map (atom (cache/ttl-cache-factory {} :ttl one-day)))

(defn expire-entries! [c]
  (cache/ttl-cache-factory
   (into {} (filter #((complement nil?) (second %))
                    (map #(vector % (get c %)) (keys c))))
   :ttl (.ttl-ms c)))

(def char-seq (doall
               (for [c (range 65 91)]
                 (char c))))

(defn gen-short-url [n]
  (clojure.string/join
   (take n (repeatedly #(rand-nth char-seq)))))

(defn add-url! [url]
  (swap! url-map expire-entries!)
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

(defn list-all [request]
  {:status 200
   :body (html [:body
                [:ul
                 (for [[k v] (seq @url-map)]
                   [:li [:a {:href v} k]])]])})

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
  (jetty/run-jetty wrapp {:port 8080}))
