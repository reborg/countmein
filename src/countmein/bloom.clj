(ns countmein.bloom
  (:import (org.apache.hadoop.util.bloom CountingBloomFilter Key)))

(defn empty-bloom?
  "True if the bloom was never added any element. Emptyness check is done
  by printing the bloom, creating a list of string numbers out of it,
  converting the strings to numbers, reducing on them. Only if the result
  is zero there was never any hash executed."
  [bloom]
  (= 0 (reduce + (map #(Integer. %) (re-seq #"\d" (. bloom toString))))))

(defn build-bloom
  "Generates a new bloom filter given a list strings. Type hints and
  side effects need to be used for Java interop."
  [coll]
  (let [bloom (CountingBloomFilter. 10 10 1)]
    (do
      (doall (map #(. bloom add (Key. (. % getBytes))) coll))
      bloom)))

(defn bloom-check
  "Check for the presence of the given item in the bloom filter"
  [item bloom]
  (. bloom membershipTest (Key. (. item getBytes))))

(defn item-count
  "Check for the presence of the given item in the bloom filter"
  [item bloom]
  (let [item-key (Key. (. item getBytes))]
    (. bloom approximateCount item-key)))

(defn bloom-in
  "Generates a new bloom filter from a collection of maps. The value
  corresponding to the key k is added to the bloom for each map in coll"
  [k coll]
  (build-bloom (remove nil? (map #(k %) coll))))

(defn empty-bloom
  "Creates a brand new empty bloom filter"
  ([]
    (empty-bloom 10 10 1))
  ([size hash-n hash-t]
    (CountingBloomFilter. size hash-n hash-t)))

(defn bloom-add
  "Add the given key to a clone of the given bloom and returns it."
  ([item bloom]
   (bloom-add item bloom 10 10 1))
  ([item bloom size hash-n hash-t]
   (let [item-key (Key. (. item getBytes))
         clone (CountingBloomFilter. size hash-n hash-t)]
     (do 
       (. clone or bloom) ;; current bloom state copied over the clone
       (. clone add item-key) 
       clone))))

(defn confidence-filter
  "Creates a predicate that can be uses to filter collections of votes."
  [threshold bloom]
    (fn [vote] 
      (let [item (:user vote)
            side-effect (. bloom add (Key. (. item getBytes)))
            c (item-count item bloom)]
        (<= c threshold))))
