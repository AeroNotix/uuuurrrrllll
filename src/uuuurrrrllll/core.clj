(ns uuuurrrrllll.core)


(def url-map (atom {}))

(def char-seq (doall
               (for [c (range 65 91)]
                 (char c))))

(defn gen-short-url [n]
  (clojure.string/join
   (take n (repeatedly #(rand-nth char-seq)))))

(defn add-url [url]
  (swap! url-map assoc (gen-short-url 10) url))

(defroutes
  (GET "/:url/" request
       (fn [request]
         (if-let [url (get-in request [:params :url])]
           {:status 301 :headers {"location" (@url-map url)}}
           {:status 404}))))
