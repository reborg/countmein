(ns countmein.core
  (:use [countmein.generation])
  (:use [countmein.bloom]))

(def invalid-user-filter #(:valid %))
(def honeypot-filter #(not (contains? (set (vals %)) "scarybot")))

(defn reduce-fn
  "Creates a incrementing reduce for associative items. Use as the
  reducing function for reduce-like operations on list of maps, 
  where you need to count +1 each time a key is found 
  in the associative structure."
  [key-fn]
  (fn [summaries x] 
    (let [k (key-fn x)]
      (assoc summaries k (inc (summaries k 0))))))

(defn reduce-by
  "Given an associative structure coll,
  reduce over the collection of values obatined by grouping key-fn
  over coll."
  [key-fn coll]
  (reduce (reduce-fn key-fn) {} coll))

(defn apply-filters
  "Apply a list of filter functions on a collection"
  [filters coll]
  (filter (apply every-pred filters) coll))

(defn count-candidates
  "Given a collection of maps containing also a candidate vote
  it returns the total count of votes for each candidate"
  ([votes]
   (count-candidates votes [identity]))
  ([votes filters]
   (let [filtered (apply-filters filters votes)]
     (reduce-by :candidate filtered))))

(defn print-info
  "Pretty printing all data about the current computation."
  [votes total distrib options threshold]
  (println 
    (str "Processing " total " votes. \nDistribution " distrib ".\n" 
         (:invalid options) "% invalid users.\n" 
         (:honeypot options) "% honeypotted votes\n" 
         threshold " max votes per user.\n"))
  (println (pprint-map (user-frequencies votes threshold))))

;;(defn -main [& args]
;;  (let [total 5000000
;;        distrib {"candidate-1" 40 "candidate-2" 30 "candidate-3" 10 "candidate-4" 10 "candidate-5" 10}
;;        options {:invalid 10 :honeypot 2}
;;        threshold 5
;;        votes (make-votes total distrib options)]
;;    (print-info votes total distrib options threshold)
;;    (time 
;;      (count-candidates votes 
;;                        [invalid-user-filter 
;;                         honeypot-filter
;;                         (confidence-filter threshold (empty-bloom (* 100 total) 60 0))]))))
