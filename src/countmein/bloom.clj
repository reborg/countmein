(ns countmein.bloom
  (:import (org.apache.hadoop.util.bloom CountingBloomFilter Key)))

(defrecord bloom-params [size hash-n hash-t])
(def test-bloom-params (bloom-params. 1000 10 1))

(defn item
  "Transforms a string element for the bloom into a Key object."
  [string]
  (Key. (. string getBytes)))

(defn ===
  "Compare two bloom filters considering them atom wrapped"
  [bloom1 bloom2]
  (= @bloom1 @bloom2))

(defn empty-bloom?
  "True if the bloom was never added any element. Emptyness check is done
  by printing the bloom, creating a list of string numbers out of it,
  converting the strings to numbers, reducing on them. Only if the result
  is zero there was never any hash executed."
  [bloom]
  (= 0 (reduce + (map #(Integer. %) (re-seq #"\d" (. @bloom toString))))))

(defn empty-bloom
  "Crates an empty bloom filter. If no arguments the Bloom filter is created
  with 1000 bit, 10 hashes of type Murmur suitable for testing. Use size,
  hash-n, hash-t to create a counting bloom filters for your need."
  ([]
    (apply empty-bloom (vals test-bloom-params)))
  ([size hash-n hash-t]
    (atom (CountingBloomFilter. size hash-n hash-t))))

(defn build-bloom-from
  "Creates a counting bloom filter which is initialized with the given
  collection of strings. If no arguments the Bloom filter is created
  with 1000 bit, 10 hashes of type Murmur suitable for testing. Use size,
  hash-n, hash-t to create a counting bloom filters for your need."
  ([coll]
   (apply build-bloom-from coll (vals test-bloom-params)))
  ([coll size hash-n hash-t] 
   (let [bloom (empty-bloom size hash-n hash-t)]
     (do
       (doall (map #(. @bloom add (Key. (. % getBytes))) coll))
       bloom))))

(defn bloom-check
  "Check for the presence of the given item in the bloom filter"
  [item bloom]
  (. @bloom membershipTest (Key. (. item getBytes))))

(defn item-count
  "Check for the presence of the given item in the bloom filter"
  [item bloom]
  (let [item-key (Key. (. item getBytes))]
    (. @bloom approximateCount item-key)))

(defn bloom-in
  "Generates a new bloom filter from a collection of maps. The value
  corresponding to the key k is added to the bloom for each map in coll"
  [k coll]
  (build-bloom-from (remove nil? (map #(k %) coll))))

(defn bloom-add
  "Add the given key to a clone of the given bloom and returns it."
  ([item bloom]
   (apply bloom-add item bloom (vals test-bloom-params)))
  ([item bloom size hash-n hash-t]
   (let [item-key (Key. (. item getBytes))
         clone (empty-bloom size hash-n hash-t)]
     (do 
       (. @clone or @bloom) ;; current bloom state copied over the clone
       (. @clone add item-key) 
       clone))))

(defn confidence-filter
  "Creates a predicate that can be uses to filter collections of votes."
  [threshold bloom]
    (fn [vote] 
      (let [item (:user vote)
            side-effect (. @bloom add (Key. (. item getBytes)))
            c (item-count item bloom)]
        (<= c threshold))))
