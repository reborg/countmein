(ns countmein.t-generation
  (:use midje.sweet)
  (:use [countmein.generation]))

(facts "calculating counts based on percentage"
       (fact "one partition"
             (relative-counts 10 {"blah" 100}) => {"blah" 10})
       (fact "one partition"
             (relative-counts 100 {:valid 100}) => {:valid 100})
       (fact "two equal partitions"
             (relative-counts 10 {"blah" 50 "doh" 50}) => {"blah" 5 "doh" 5})
       (fact "4 equal partitions"
             (relative-counts 16 {:a 25 :b 25 :c 25 :d 25}) => {:a 4 :b 4 :c 4 :d 4})
       (fact "pareto"
             (relative-counts 10 {:a 80 :b 20}) => {:a 8 :b 2})
       (fact "candidate distribution"
             (relative-counts 1000 {:a 50 :b 30 :c 20}) => {:a 500 :b 300 :c 200})
       (fact "abnormal distribution"
             (relative-counts 10 {:a 50 :b 90}) => {:a 5 :b 9}))

(facts "partitioning"
       (fact "it gives me all candidate-1 by default"
             (distinct (map #(:candidate %) (make-votes 2))) => '("candidate-1"))
       (fact "splitting 50%"
             (let [partitioning {"candidate-1" 50 "candidate-2" 50}]
               (sort (map #(:candidate %) 
                          (make-votes 2 partitioning))) => '("candidate-1" "candidate-2")))
       (fact "splitting 80% 20%"
             (let [partitioning {"candidate-1" 80 "candidate-2" 20}]
               (vals (frequencies (map #(:candidate %) (make-votes 10 partitioning)))) => '( 8 2 ))))

(facts "creating invalid votes"
       (fact "all users are transformed to invalid"
             (make-invalid [{:a "a" :user "1111" :valid true}]) => '({:a "a" :user "1111" :valid false}))
       (fact "just the given number of votes is marked as invalid"
             (let [actual (make-invalid (repeat 4 {:a "a" :user "blah" :valid true}) 3)]
               (count (remove #(:valid %) actual)) => 3)))

(facts "marking invalid users"
       (fact "no invalid users, maps should stay the same"
             (let [actual (make-votes 2 {"candidate-1" 100} {:invalid 0 :honeypot 100})]
               (= "invalid" (first (distinct (map #(:user %) actual)))) => falsey))
       (fact "got all invalid, all user values should change"
             (let [actual (make-votes 2 {"candidate-1" 100} {:invalid 100 :honeypot 100})]
               (distinct (map #(:valid %) actual)) => '(false))))

(facts "adding honeypot"
       (fact "all empty honey-pots"
             (let [actual (make-votes 2 {"candidate-1" 100} {:invalid 0 :honeypot 0})]
               (= "" (first (distinct (map #(:honeypot %) actual)))) => truthy))
       (fact "all honey-pots are filled"
             (let [actual (make-votes 2 {"candidate-1" 100} {:invalid 0 :honeypot 100})]
               (distinct (map #(:honeypot %) actual)) => '("scarybot"))))

(facts "printing statistics about the current distribution of duplicates"
       (fact "return the frequencies for users in the map"
             (let [votes [{:user "1"} {:user "1"} {:user "1"} {:user "2"}]]
               (user-frequencies votes 0) => {"1" 3, "2" 1}))
       (fact "only returns those user above a certain threshold"
             (let [votes [{:user "1"} {:user "1"} {:user "1"} {:user "2"}]]
               (user-frequencies votes 2) => {"1" 3})))
