(ns uuuurrrrllll.riak
  (:require [clojurewerkz.welle.kv :as kv]
            [uuuurrrrllll.util :refer [gen-short-url]]))


(def ^:private bucket "links")

(defn read-entry [entry]
  (-> entry
      :result
      :value))

(defn get-entry [short]
  (read-entry (kv/fetch-one bucket short)))

(defn get-all-entries []
  (->> "all"
       (kv/index-query bucket :all)
       (pmap get-entry)))

(defn add-entry! [body]
  (let [short-url (gen-short-url 5)]
    (kv/store bucket short-url body
              :content-type "application/json"
              :indexes {:all #{"all"}})
    short-url))
