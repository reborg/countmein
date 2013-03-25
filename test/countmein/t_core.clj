(ns countmein.t-core
  (:use midje.sweet)
  (:use [countmein.core])
  (:use [countmein.bloom])
  (:use [countmein.generation]))

(facts "preparation of the input for later reduction"
       (fact "a count of 1 is assigned to each candidate"
             (let [counted (add-count '({:u "a" :candidate "A"} {:u "b" :candidate "B"}))]
                   (filter #(nil? (:count %)) counted) => '()))
       (fact "same candidate is counted as '1' twice"
             (let [counted (add-count '({:candidate "A"} {:candidate "A"}))]
                   (vals (first counted)) => '(1 "A")
                   (vals (last counted)) => '(1 "A")))
       (fact "should not remove other attributes"
             (let [counted (add-count '({:u "a" :candidate "A"} {:u "a" :candidate "B"}))]
                   (:u (first counted)) => "a")))

(facts "counting candidates without filters"
       (fact "only one candidate"
             ((count-candidates (make-votes 4)) "candidate-1") => 4)
       (fact "pareto candidates"
             (let [votes (make-votes 10 {"candidate-1" 80 "candidate-2" 20})]
               ((count-candidates votes) "candidate-1") => 8
               ((count-candidates votes) "candidate-2") => 2)))

(facts "applying multiple filters to a list of maps"
       (let [maps (repeat 2 {:a "a" :b "b"})
             filters-1 [#(contains? % :x)]
             filters-2 [#(contains? % :a) #(contains? % :z)]
             filters-3 [#(not (contains? (set (vals %)) "a"))]]
         (fact "identity filter gives just the same map"
               (apply-filters [identity] maps) => maps)
         (fact "a filter that is not filtering anything in the map"
               (apply-filters filters-1 maps) => '())
         (fact "filter out all maps which contain at list one 'b' value"
               (apply-filters filters-3 maps) => '())
         (fact "a true fact following a non true fact"
               (apply-filters filters-2 maps) => '())))
             
(facts "filtering of invalid users"
       (let [filters [invalid-user-filter]]
         (fact "no invalid users returns all candidates back"
               (let [votes (make-votes 10 {"candidate-1" 100} {:invalid 0 :honeypot 100})]
                 ((count-candidates votes filters) "candidate-1") => 10))
         (fact "all invalid users should return an empty list"
               (let [votes (make-votes 10 {"candidate-1" 100} {:invalid 100 :honeypot 100})]
                 (count (count-candidates votes filters)) => 0))
         (fact "expecting a total count of 1"
               (let [votes (make-votes 10 {"candidate-1" 80 "candidate-2" 20} {:invalid 90 :honeypot 100})]
                 (reduce #(+ %1 (last %2)) 0 (count-candidates votes filters)) => 1))
         (fact "expecting a total count of 55"
               (let [votes (make-votes 100 {"candidate-1" 10 "candidate-2" 90} {:invalid 45 :honeypot 100})]
                 (reduce #(+ %1 (last %2)) 0 (count-candidates votes filters)) => 55))))

(facts "filtering of honeypotted votes"
       (let [filters [honeypot-filter]]
         (fact "expecting a total count of 80"
               (let [votes (make-votes 100 {"candidate-1" 10 "candidate-2" 90} {:invalid 0 :honeypot 20})]
                 (reduce #(+ %1 (last %2)) 0 (count-candidates votes filters)) => 80))))

;;(facts "filter out votes above the throshold"
;;       (let [filters [(confidence-filter 2 (empty-bloom 1000 10 0))]]
;;         (fact "it should remove all votes above threshold"
;;               (let [votes (make-votes 10 {"candidate-1" 100} {:invalid 0})]
;;                 ((count-candidates votes filters) "candidate-1") => 2))))
