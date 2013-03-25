(ns countmein.core
  (:use [countmein.generation])
  (:use [countmein.bloom]))

(def invalid-user-filter #(:valid %))
(def honeypot-filter #(not (contains? (set (vals %)) "scarybot")))

(defn reduce-by
  "Given an associative structure coll,
  reduce over the collection of values obatined by grouping key-fn
  over coll. Init is passed down to reduce as the seed of the left fold."
  [key-fn f init coll]
  (reduce (fn [summaries x] 
            (let [k (key-fn x)]
              (assoc summaries k (f (summaries k init) x))))
          {} coll))

(defn add-count
  "append :count 1 key-value to each element of the map"
  [votes]
  (map #(assoc % :count 1) votes))

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
   (let [counted (add-count votes)
         filtered (apply-filters filters counted)]
     (reduce-by :candidate #(+ %1 (:count %2)) 0 filtered))))

(defn print-info
  "Pretty printing all data about the current computation."
  [votes total distrib options threshold]
  (println 
    (str "Processing " total " votes. \nDistribution " distrib ".\n" 
         (:invalid options) "% invalid users.\n" 
         (:honeypot options) "% honeypotted votes\n" 
         threshold " max votes per user.\n"))
  (println (pprint-map (user-frequencies votes threshold))))

(defn -main [& args]
  (let [total 5000000
        distrib {"candidate-1" 40 "candidate-2" 30 "candidate-3" 10 "candidate-4" 10 "candidate-5" 10}
        options {:invalid 10 :honeypot 2}
        threshold 5
        votes (make-votes total distrib options)]
    (print-info votes total distrib options threshold)
    (time 
      (count-candidates votes 
                        [
                         invalid-user-filter 
                         honeypot-filter
                         (confidence-filter threshold (empty-bloom (* 100 total) 60 0))]))))
