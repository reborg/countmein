(ns countmein.t-parallel
  (:use midje.sweet)
  (:use [countmein.parallel])
  (:use [countmein.core])
  (:use [countmein.generation])
  (:require [clojure.core.reducers :as r]))

(facts "incrementing associative reducing function"
       (fact "no matching keys"
             (let [coll [{:z 1} {:z 1}]]
               (set (keys (reduce (reduce-fn :a) {} coll))) => #{nil}))
       (fact "one matching key"
             (let [coll [{:a "a"} {:a "a"} {:a "b"}]]
               (reduce (reduce-fn :a) {} coll) => {"a" 2 "b" 1}))
       (fact "one matching key and other keys"
             (let [coll [{:a "a"} {:a "a"} {:a "b"} {:b 1}]]
               ((reduce (reduce-fn :a) {} coll) "a") => 2)))

(facts "combine and seed function"
       (fact "returns hash-map without args"
             (combine-fn) => {})
       (fact "else sums up values for common keys in maps passed as arguments"
             (let [map1 {:a 1 :b 1}
                   map2 {:a 1 :c 1}]
               (:a (combine-fn map1 map2)) => 2)))

(facts "folding like a crazy horse"
      (fact "fold in the small"
            (let [coll [{:a "a"} {:a "a"} {:a "b"} {:b 1}]]
              ((fold-by (reduce-fn :a) coll) "a") => 2))
      (fact "fold in the medium"
            (let [coll (repeat 10000 {:a "a"})]
              ((fold-by (reduce-fn :a) coll) "a") => 10000)))

(facts "folding with a reducible collection"
       (fact "folding with honey-pot filter"
            (let [coll (r/filter honeypot-filter (apply merge (repeat 800 {:a "a"}) (repeat 200 {:b "scarybot"})))]
              ((fold-by (reduce-fn :a) coll) "scarybot") => nil)))
