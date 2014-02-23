(ns uuuurrrrllll.core
  (:require [compojure.core :refer [defroutes routes GET POST]]
            [clojure.core.cache :as cache]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [ring.adapter.jetty :as jetty])
  (:use [hiccup.core]))


;; One day in ms.
(def one-day 86400000)
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
  (if-let [url (-> request
                   (get-in [:params :url])
                   ((fn [u] (cache/lookup @url-map u))))]
    {:status 301 :headers {"location" url}}
    {:status 404}))

(defn list-all [request]
  {:status 200
   :body (html [:body
                [:ul
                 (for [[_ v] (seq @url-map)]
                   [:li [:a {:href v} v]])]])})

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
