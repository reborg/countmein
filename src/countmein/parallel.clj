(ns countmein.parallel
  (:use [countmein.core])
  (:use [countmein.generation])
  (:use [countmein.bloom])
  (:require [clojure.core.reducers :as r]))

(def combine-fn
  "Creates a combine function that can be used as a seed for fold"
  (r/monoid (partial merge-with +) hash-map))

(defn fold-by
  "Given an associative and reducible structure col fold over coll 
  using the given reducing-fn and combining back as grouping with count."
  [reducef coll]
    (r/fold combine-fn reducef coll))

(defn -main [& args]
  (let [total 50000
        distrib {"candidate-1" 40 "candidate-2" 30 "candidate-3" 10 "candidate-4" 10 "candidate-5" 10}
        options {:invalid 10 :honeypot 2}
        threshold 5
        bloom (confidence-filter threshold (empty-bloom (* 100 total) 60 0))
        votes (make-votes total distrib options)
        filtered (r/filter invalid-user-filter (r/filter honeypot-filter (r/filter bloom votes)))]
    (print-info votes total distrib options threshold)
    (time 
      (fold-by (reduce-fn :candidate) filtered))))
